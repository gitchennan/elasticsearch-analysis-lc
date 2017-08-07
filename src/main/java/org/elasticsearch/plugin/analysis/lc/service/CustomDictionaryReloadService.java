package org.elasticsearch.plugin.analysis.lc.service;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.hankcs.hanlp.corpus.synonym.Synonym;
import com.hankcs.hanlp.dictionary.CoreSynonymDictionary;
import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.log.HanLpLogger;
import lc.lucene.domain.CustomWord;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.cluster.health.ClusterIndexHealth;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.plugin.analysis.lc.NodeDictReloadResult;
import org.elasticsearch.plugin.analysis.lc.NodeDictReloadTransportRequest;
import org.elasticsearch.plugin.analysis.lc.NodeDictReloadTransportResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.threadpool.ThreadPool;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CustomDictionaryReloadService extends AbstractLifecycleComponent {

    private NodeClient nodeClient;

    private ClusterService clusterService;

    private String customIdxName;

    private ScheduledExecutorService lcCustomDictReloadExecutor;

    @Inject
    public CustomDictionaryReloadService(NodeClient nodeClient, ClusterService clusterService, Settings settings) {
        this(".custom-dictionary", nodeClient, clusterService, settings);
    }

    public CustomDictionaryReloadService(String customIdxName, NodeClient nodeClient, ClusterService clusterService, Settings settings) {
        super(settings, CustomDictionaryReloadService.class);
        this.customIdxName = customIdxName;
        this.nodeClient = nodeClient;
        this.clusterService = clusterService;
    }

    @Override
    protected void doStart() {
        HanLpLogger.info(this, "custom_dict_reload_service, status[" + lifecycleState().name() + "].");
        lcCustomDictReloadExecutor = Executors.newScheduledThreadPool(2);

        lcCustomDictReloadExecutor.schedule(new CustomDictionaryIndexMonitorTask(), 10, TimeUnit.SECONDS);
        lcCustomDictReloadExecutor.schedule(new InitializeDictionaryReloadTask(), 10, TimeUnit.SECONDS);

        /**
         * pre load dictionary into java heap when elect a new master
         */
        clusterService.addListener(clusterChangedEvent -> {
            if (clusterChangedEvent.isNewCluster()) {
                try {
                    Class.forName("com.hankcs.hanlp.dictionary.CoreDictionary");
                    Class.forName("com.hankcs.hanlp.dictionary.CoreDictionaryTransformMatrixDictionary");
                    Class.forName("com.hankcs.hanlp.dictionary.CoreSynonymDictionary");
                    Class.forName("com.hankcs.hanlp.dictionary.stopword.CoreStopWordDictionary");
                    Class.forName("com.hankcs.hanlp.dictionary.CustomDictionary");
                    Class.forName("com.hankcs.hanlp.dictionary.CoreBiGramTableDictionary");
                    Class.forName("com.hankcs.hanlp.dictionary.other.CharType");

                    // named entity dictionary
                    Class.forName("com.hankcs.hanlp.dictionary.nr.PersonDictionary");
                    Class.forName("com.hankcs.hanlp.dictionary.nr.JapanesePersonDictionary");
                    Class.forName("com.hankcs.hanlp.dictionary.ns.PlaceDictionary");
                    Class.forName("com.hankcs.hanlp.dictionary.nt.OrganizationDictionary");
                }
                catch (Exception ex) {
                    HanLpLogger.error(CustomDictionaryReloadService.this, "preLoad dictionaries error.", ex);
                }
            }

        });
    }

    @Override
    protected void doStop() {
        HanLpLogger.info(this, "custom_dict_reload_service, status[" + lifecycleState().name() + "].");

        ThreadPool.terminate(lcCustomDictReloadExecutor, 0, TimeUnit.SECONDS);
    }

    @Override
    protected void doClose() throws IOException {
        HanLpLogger.info(this, "custom_dict_reload_service, status[" + lifecycleState().name() + "].");
    }

    public void createCustomDictionaryIndexIfNotExist() {
        if (!isCustomDictionaryIndexExist()) {
            int dataNodeCount = clusterService.state().nodes().getDataNodes().size();
            String indexReplicas = dataNodeCount > 1 ? "1" : "0";

            CreateIndexResponse createIdxResp = nodeClient.admin().indices().prepareCreate(customIdxName)
                    .setSettings(Settings.builder()
                            .put("number_of_shards", "1")
                            .put("number_of_replicas", indexReplicas)
                            .put("index.refresh_interval", "5s"))
                    .addMapping(CustomWord.type, CustomWord.mapping)
                    .setWaitForActiveShards(1).execute().actionGet();

            if (!createIdxResp.isShardsAcked()) {
                HanLpLogger.error(this,
                        String.format("Create custom dictionary index[%s] error, message[%s]",
                                customIdxName, createIdxResp.toString()));
            }
            else {
                HanLpLogger.info(this, String.format("Create custom dictionary index[%s]", customIdxName));
            }
        }
    }

    public String doPrivilegedReloadCustomDictionary() {
        return doPrivilegedReloadCustomDictionary(new NodeDictReloadTransportRequest()).toString();
    }

    public NodeDictReloadTransportResponse doPrivilegedReloadCustomDictionary(NodeDictReloadTransportRequest request) {
        NodeDictReloadTransportResponse response = AccessController.doPrivileged(
                (PrivilegedAction<NodeDictReloadTransportResponse>) () -> reloadLocalNodeDictionary(request));
        HanLpLogger.info(this, "Reloaded custom dict: " + response.toString());
        return response;
    }

    private NodeDictReloadTransportResponse reloadLocalNodeDictionary(NodeDictReloadTransportRequest request) {
        if (!isCustomDictionaryIndexExist()) {
            String resultMsg = "Index[" + customIdxName + "] missing, abort reload.";
            return new NodeDictReloadTransportResponse(new NodeDictReloadResult(clusterService.localNode().getName(), 0, resultMsg));
        }

        SearchResponse response = nodeClient.prepareSearch(customIdxName).setTypes(CustomWord.type)
                .setQuery(QueryBuilders.matchAllQuery()).setSize(0).execute().actionGet();

        beforeReloadCustomerDictionary();

        if (response.getHits().totalHits() == 0) {
            String resultMsg = "No custom word found in index[" + customIdxName + "], abort reload.";
            return new NodeDictReloadTransportResponse(new NodeDictReloadResult(clusterService.localNode().getName(), 0, resultMsg));
        }

        int wordCount = 0;
        String scrollId = null;
        try {
            response = nodeClient.prepareSearch(customIdxName).setTypes(CustomWord.type)
                    .setQuery(QueryBuilders.matchAllQuery())
                    .addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)
                    .setScroll(new TimeValue(10, TimeUnit.SECONDS)).setSize(500).execute().actionGet();
            scrollId = response.getScrollId();

            Gson gson = new Gson();
            while (response.getHits().getHits().length != 0) {
                for (SearchHit hit : response.getHits().getHits()) {
                    CustomWord word = gson.fromJson(hit.getSourceAsString(), CustomWord.class);
                    processCustomWord(word);
                    wordCount++;
                }
                response = nodeClient.prepareSearchScroll(scrollId).setScroll(new TimeValue(10, TimeUnit.SECONDS)).execute().actionGet();
                if (response.getScrollId() != null) {
                    scrollId = response.getScrollId();
                }
            }
        }
        catch (Exception ex) {
            HanLpLogger.error(this, "Scroll search error.", ex);
            return new NodeDictReloadTransportResponse(new NodeDictReloadResult(
                    clusterService.localNode().getName(), wordCount, "ERROR: " + ex.getMessage()));
        }
        finally {
            if (scrollId != null) {
                nodeClient.prepareClearScroll().addScrollId(scrollId).execute().actionGet();
            }
            afterReloadCustomerDictionary();
        }
        return new NodeDictReloadTransportResponse(new NodeDictReloadResult(clusterService.localNode().getName(), wordCount, "OK"));
    }

    private void beforeReloadCustomerDictionary() {
        // remove all custom dict words
        CustomDictionary.INSTANCE.cleanBinTrie();
        CoreSynonymDictionary.INSTANCE.cleanBinTrie();
    }

    private void afterReloadCustomerDictionary() {

    }

    private void processCustomWord(CustomWord word) {
        String wordAttr = word.getWordAttributeAsString();
        if (wordAttr.length() == 0) {
            CustomDictionary.INSTANCE.add(word.getWord());
        }
        else {
            CustomDictionary.INSTANCE.add(word.getWord(), wordAttr);
        }

        if (word.getSynonyms() != null && word.getSynonyms().size() > 0) {
            List<String> allWords = Lists.newArrayList(word.getSynonyms());
            allWords.add(word.getWord());

            CoreSynonymDictionary.INSTANCE.add(Synonym.Type.EQUAL, allWords);
            for (String tSynonym : word.getSynonyms()) {
                if (wordAttr.length() == 0) {
                    CustomDictionary.INSTANCE.add(tSynonym);
                }
                else {
                    CustomDictionary.INSTANCE.add(tSynonym, wordAttr);
                }
            }
        }
    }

    private boolean isCustomDictionaryIndexExist() {
        return nodeClient.admin().indices().prepareExists(customIdxName).execute().actionGet().isExists();
    }

    private boolean isCustomDictionaryIndexClosed() {
        ClusterStateResponse clusterStateResponse = nodeClient.admin().cluster().prepareState().setIndices(customIdxName).execute().actionGet();
        return clusterStateResponse.getState().getMetaData().indices().get(customIdxName).getState() == IndexMetaData.State.CLOSE;
    }

    private boolean openCustomDictionaryIndex() {
        return nodeClient.admin().indices().prepareOpen(customIdxName).execute().actionGet().isAcknowledged();
    }

    public boolean isCustomDictionaryIndexActive() {
        ClusterHealthResponse healthResponse = nodeClient.admin().cluster().prepareHealth(customIdxName).execute().actionGet();
        if (healthResponse.getIndices().containsKey(customIdxName)) {
            ClusterIndexHealth clusterIndexHealth = healthResponse.getIndices().get(customIdxName);
            HanLpLogger.info(this, "custom dictionary index[" + customIdxName + "] status[" + clusterIndexHealth.getStatus().name() + "]");
            if (clusterIndexHealth.getStatus() == ClusterHealthStatus.GREEN) {
                return true;
            }
        }
        return false;
    }

    private class InitializeDictionaryReloadTask implements Runnable {
        @Override
        public void run() {
            try {
                boolean checkPass = true;
                if (clusterService.state().blocks().hasGlobalBlock(ClusterBlockLevel.READ)) {
                    checkPass = false;
                    HanLpLogger.info(this, "cluster has global [read] block, retry again.");
                }
                if (checkPass && !isCustomDictionaryIndexActive()) {
                    checkPass = false;
                    HanLpLogger.info(this, "custom dict index[" + customIdxName + "] is inactive, retry again.");
                }

                if (checkPass) {
                    if (!isCustomDictionaryIndexExist()) {
                        HanLpLogger.info(this, "custom dict index[" + customIdxName + "] not exists, abort init dict.");
                        return;
                    }
                    if (isCustomDictionaryIndexClosed()) {
                        HanLpLogger.info(this, "custom dict index[" + customIdxName + "] is closed, abort init dict.");
                        return;
                    }
                }

                if (checkPass) {
                    String reloadResultMsg = doPrivilegedReloadCustomDictionary();
                    HanLpLogger.info(this, "after init load custom dictionary, message: " + reloadResultMsg);
                }
                else {
                    lcCustomDictReloadExecutor.schedule(this, 10, TimeUnit.SECONDS);
                }
            }
            catch (Exception ex) {
                HanLpLogger.error(this, "The error occurred while initiating customer dictionary.", ex);
            }
        }
    }

    private class CustomDictionaryIndexMonitorTask implements Runnable {
        @Override
        public void run() {
            try {
                if (clusterService.state().blocks().hasGlobalBlock(ClusterBlockLevel.WRITE)) {
                    HanLpLogger.info(this, "cluster has global [write] block, abort execute monitor task.");
                }
                else {
                    // master node only
                    if (clusterService.state().nodes().isLocalNodeElectedMaster()) {
                        /**
                         *  if custom dict index is not exists, create it
                         */
                        createCustomDictionaryIndexIfNotExist();

                        /**
                         *  if custom dict index is closed, just open it
                         */
                        if (isCustomDictionaryIndexClosed()) {
                            openCustomDictionaryIndex();
                        }
                    }
                }
            }
            catch (Exception ex) {
                HanLpLogger.error(this, "monitor task error, retry again", ex);
            }

            lcCustomDictReloadExecutor.schedule(this, 30, TimeUnit.SECONDS);
        }
    }
}
