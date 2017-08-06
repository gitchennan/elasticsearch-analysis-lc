package com.hankcs.test.elasticsearch;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.plugin.analysis.lc.LcAnalysisPlugin;
import org.elasticsearch.plugin.analysis.lc.LcDictReloadAction;
import org.elasticsearch.plugin.analysis.lc.LcDictReloadRequestBuilder;
import org.elasticsearch.plugin.analysis.lc.LcDictReloadResponse;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;

public class ReloadDictApiTest {

    public static TransportClient transportClient;

    @Before
    public void initClient() throws Exception {
        if (transportClient == null) {
            Settings settings = Settings.builder()
                    .put("cluster.name", "lu-search-cluster")
                    .put("client.transport.sniff", true)
                    .build();

            transportClient = new PreBuiltTransportClient(settings, LcAnalysisPlugin.class);
            transportClient.addTransportAddresses(new InetSocketTransportAddress(InetAddress.getByName("192.168.0.125"), 9300));
        }
    }

    @Test
    public void test_reloadDict() {

        LcDictReloadRequestBuilder requestBuilder = new LcDictReloadRequestBuilder(transportClient, LcDictReloadAction.INSTANCE);
        LcDictReloadResponse lcDictReloadResponse = requestBuilder.execute().actionGet();

        System.out.println(lcDictReloadResponse.toString());
    }
}
