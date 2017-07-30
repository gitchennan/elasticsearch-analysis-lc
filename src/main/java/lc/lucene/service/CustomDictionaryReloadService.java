package lc.lucene.service;

import com.google.gson.Gson;
import com.hankcs.hanlp.corpus.synonym.Synonym;
import com.hankcs.hanlp.dictionary.CoreSynonymDictionary;
import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.log.HanLpLogger;
import lc.lucene.domain.CustomWord;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.concurrent.EsExecutors;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.threadpool.ThreadPool;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class CustomDictionaryReloadService extends AbstractLifecycleComponent {

    /**
     * A client to make requests to the system
     */
    private Client client;

    private ClusterService clusterService;

    private String customIdxName;

    private ScheduledExecutorService lcCustomDictionaryRefresher;

    @Inject
    public CustomDictionaryReloadService(Client client, ClusterService clusterService, Settings settings) {
        this(".custom-dictionary", client, clusterService, settings);
    }

    public CustomDictionaryReloadService(String customIdxName, Client client, ClusterService clusterService, Settings settings) {
        super(settings, CustomDictionaryReloadService.class);
        this.customIdxName = customIdxName;
        this.client = client;
        this.clusterService = clusterService;
    }

    @Override
    protected void doStart() {
        ThreadFactory threadFactory = EsExecutors.daemonThreadFactory(settings, "[lc_custom_dict_refresh]");
        lcCustomDictionaryRefresher = Executors.newSingleThreadScheduledExecutor(threadFactory);
        lcCustomDictionaryRefresher.schedule(new CreateCustomDictionaryIndexMonitorTask(), 30, TimeUnit.SECONDS);

        // todo: after cluster init
        reloadCustomDictionary();
    }

    @Override
    protected void doStop() {

    }

    @Override
    protected void doClose() throws IOException {
        ThreadPool.terminate(lcCustomDictionaryRefresher, 0, TimeUnit.SECONDS);
    }

    private boolean hasCustomDictionaryIndexBlocked() {
        return clusterService.state().blocks().indexBlocked(ClusterBlockLevel.WRITE, customIdxName);
    }

    public boolean isCustomDictionaryIndexExist() {
        return client.admin().indices().prepareExists(customIdxName).execute().actionGet().isExists();
    }

    public void createCustomDictionaryIndexIfNotExist() {
        if (!isCustomDictionaryIndexExist()) {
            CreateIndexResponse createIdxResp = client.admin().indices().prepareCreate(customIdxName)
                    .setSettings(Settings.builder()
                            .put("number_of_shards", "1")
                            .put("number_of_replicas", "0")
                            .put("index.refresh_interval", "30s"))
                    .addMapping(CustomWord.type, CustomWord.mapping)
                    .setWaitForActiveShards(1)
                    .execute().actionGet();

            if (!createIdxResp.isShardsAcked()) {
                HanLpLogger.error(this,
                        String.format("Create custom dictionary index[%s] error, message[%s]",
                                customIdxName, createIdxResp.toString()));
            }
            else {
                HanLpLogger.error(this, String.format("Create custom dictionary index[%s]", customIdxName));
            }
        }
    }

    public String reloadCustomDictionary() {
        if (!isCustomDictionaryIndexExist()) {
            String resultMsg = String.format("custom dict index[%s] not exists, ignore reload.", customIdxName);
            HanLpLogger.warn(this, resultMsg);
            return resultMsg;
        }

        SearchResponse response = client.prepareSearch(customIdxName).setTypes(CustomWord.type)
                .setQuery(QueryBuilders.matchAllQuery()).setSize(0).execute().actionGet();

        beforeReloadCustomerDictionary();

        if (response.getHits().totalHits() == 0) {
            String resultMsg = String.format("there's no any custom word found in index[%s], ignore reload.", customIdxName);
            HanLpLogger.info(this, resultMsg);
            return resultMsg;
        }

        int wordCount = 0;
        String scrollId = null;
        try {
            response = client.prepareSearch(customIdxName).setTypes(CustomWord.type)
                    .setQuery(QueryBuilders.matchAllQuery())
                    .addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)
                    .setScroll(new TimeValue(60000)).setSize(500).execute().actionGet();
            scrollId = response.getScrollId();

            Gson gson = new Gson();
            while (response.getHits().getHits().length != 0) {
                for (SearchHit hit : response.getHits().getHits()) {
                    CustomWord word = gson.fromJson(hit.getSourceAsString(), CustomWord.class);
                    processCustomWord(word);
                    wordCount++;
                }
                response = client.prepareSearchScroll(scrollId)
                        .setScroll(new TimeValue(60000)).execute().actionGet();
            }
        }
        finally {
            client.prepareClearScroll().addScrollId(scrollId).execute().actionGet();
        }

        return "Loaded custom dictionary, total_count: " + wordCount;
    }

    private void beforeReloadCustomerDictionary() {
        // remove all custom dict words
        CustomDictionary.INSTANCE.cleanBinTrie();
        CoreSynonymDictionary.INSTANCE.cleanBinTrie();
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
            CoreSynonymDictionary.INSTANCE.add(Synonym.Type.EQUAL, word.getSynonyms());
        }
    }

    private boolean localNodeIsMaster() {
        DiscoveryNode localNode = clusterService.localNode();
        DiscoveryNode masterNode = clusterService.state().nodes().getMasterNode();

        return masterNode != null && (localNode.getId().equals(masterNode.getId()));
    }

    private class CreateCustomDictionaryIndexMonitorTask implements Runnable {
        @Override
        public void run() {
            try {
                doMonitorTask();
            }
            catch (Exception ex) {
                HanLpLogger.error(this, "monitor task error", ex);
            }

            lcCustomDictionaryRefresher.schedule(this, 30, TimeUnit.SECONDS);
        }

        private void doMonitorTask() {
            if (clusterService.state().blocks().hasGlobalBlock(ClusterBlockLevel.WRITE)) {
                HanLpLogger.info(this, "cluster has global blocks, ignore do monitor task.");
                return;
            }

            if (localNodeIsMaster()) {
                createCustomDictionaryIndexIfNotExist();
            }
        }
    }
}
