package lc.lucene.service;

import com.google.gson.Gson;
import com.hankcs.hanlp.log.HanLpLogger;
import lc.lucene.domain.CustomWord;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.LocalNodeMasterListener;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.threadpool.ThreadPool;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CustomDictionaryReloadService {

    /**
     * A client to make requests to the system
     */
    private Client client;

    private String customIdxName;

    private ScheduledExecutorService lcCustomDictionaryRefresher;

    private volatile boolean localNodeIsMaster = false;

    public CustomDictionaryReloadService(String customIdxName, Client client, ScheduledExecutorService lcCustomDictionaryRefresher) {
        this.customIdxName = customIdxName;
        this.client = client;
        this.lcCustomDictionaryRefresher = lcCustomDictionaryRefresher;
        this.lcCustomDictionaryRefresher.schedule(new CreateCustomDictionaryIndexMonitorTask(), 30, TimeUnit.SECONDS);
    }

    public CustomDictionaryReloadService(Client client, ScheduledExecutorService lcCustomDictionaryRefresher) {
        this(".custom-dictionary", client, lcCustomDictionaryRefresher);
    }

    private boolean isCustomDictionaryIndexExist() {
        return client.admin().indices().prepareExists(customIdxName)
                .execute().actionGet().isExists();
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

    public void reloadCustomDictionary() {
        if (!isCustomDictionaryIndexExist()) {
            HanLpLogger.warn(this,
                    String.format("custom dict index[%s] not exists, ignore reload.", customIdxName));
            return;
        }

        SearchResponse response = client.prepareSearch(customIdxName).setTypes(CustomWord.type)
                .setQuery(QueryBuilders.matchAllQuery()).setSize(0).execute().actionGet();

        if (response.getHits().totalHits() == 0) {
            HanLpLogger.info(this,
                    String.format("there's no any custom word found in index[%s], ignore reload.", customIdxName));
            return;
        }

        String scrollId = null;
        try {
            response = client.prepareSearch(customIdxName).setTypes(CustomWord.type)
                    .setQuery(QueryBuilders.matchAllQuery())
                    .addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)
                    .setScroll(new TimeValue(60000)).setSize(500).execute().actionGet();

            while (response.getHits().getHits().length != 0) {
                Gson gson = new Gson();
                for (SearchHit hit : response.getHits().getHits()) {
                    CustomWord word = gson.fromJson(hit.getSourceAsString(), CustomWord.class);
                    HanLpLogger.info(this, word.toString());
                }

                scrollId = response.getScrollId();
                response = client.prepareSearchScroll(scrollId)
                        .setScroll(new TimeValue(60000)).execute().actionGet();
            }
        }
        finally {
            client.prepareClearScroll().addScrollId(scrollId).execute().actionGet();
        }
    }

    public ScheduledExecutorService getLcCustomDictionaryRefresher() {
        return lcCustomDictionaryRefresher;
    }

    public LocalNodeMasterListener buildLocalNodeMasterListener() {
        return new LocalNodeMasterListener() {
            @Override
            public void onMaster() {
                localNodeIsMaster = true;
            }

            @Override
            public void offMaster() {
                localNodeIsMaster = false;
            }

            @Override
            public String executorName() {
                return ThreadPool.Names.SAME;
            }
        };
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
            if (localNodeIsMaster) {
                createCustomDictionaryIndexIfNotExist();
            }
        }
    }
}
