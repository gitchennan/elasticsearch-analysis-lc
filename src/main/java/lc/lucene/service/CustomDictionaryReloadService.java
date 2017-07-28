package lc.lucene.service;

import com.hankcs.hanlp.log.HanLpLogger;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.LocalNodeMasterListener;
import org.elasticsearch.common.settings.Settings;
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
        this.lcCustomDictionaryRefresher.schedule(new CustomDictionaryMonitorTask(), 30, TimeUnit.SECONDS);
    }

    public CustomDictionaryReloadService(Client client, ScheduledExecutorService lcCustomDictionaryRefresher) {
        this(".custom-dictionary", client, lcCustomDictionaryRefresher);
    }

    public void createCustomDictionaryIndexIfNotExist() {
        IndicesExistsResponse idxExistsResp = client.admin().indices().prepareExists(customIdxName).execute().actionGet();
        if (!idxExistsResp.isExists()) {
            CreateIndexResponse createIdxResp = client.admin().indices().prepareCreate(customIdxName)
                    .setSettings(Settings.builder()
                            .put("number_of_shards", "1")
                            .put("number_of_replicas", "0"))
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
                return ThreadPool.Names.MANAGEMENT;
            }
        };
    }

    private class CustomDictionaryMonitorTask implements Runnable {
        @Override
        public void run() {
            doMonitorTask();
            lcCustomDictionaryRefresher.schedule(this, 30, TimeUnit.SECONDS);
        }

        private void doMonitorTask() {
            if (localNodeIsMaster) {
                createCustomDictionaryIndexIfNotExist();
            }
            HanLpLogger.info(this, "do monitor task");
        }
    }
}
