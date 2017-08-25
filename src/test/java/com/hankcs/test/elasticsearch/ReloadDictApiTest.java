//package com.hankcs.test.elasticsearch;
//
//import org.elasticsearch.client.transport.TransportClient;
//import org.elasticsearch.common.settings.Settings;
//import org.elasticsearch.common.transport.InetSocketTransportAddress;
//import org.elasticsearch.plugin.analysis.lc.LcAnalysisClientPlugin;
//import org.elasticsearch.plugin.analysis.lc.dict.reload.LcDictReloadAction;
//import org.elasticsearch.plugin.analysis.lc.dict.reload.LcDictReloadRequestBuilder;
//import org.elasticsearch.plugin.analysis.lc.dict.reload.LcDictReloadResponse;
//import org.elasticsearch.transport.client.PreBuiltTransportClient;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.net.InetAddress;
//
//public class ReloadDictApiTest {
//
//    public static TransportClient transportClient;
//
//    @Before
//    public void initClient() throws Exception {
//        if (transportClient == null) {
//            Settings settings = Settings.builder()
//                    .put("cluster.name", "lufax-asset-cluster")
//                    .put("client.transport.sniff", true)
//                    .build();
//
//            transportClient = new PreBuiltTransportClient(settings, LcAnalysisClientPlugin.class);
//            transportClient.addTransportAddresses(new InetSocketTransportAddress(InetAddress.getByName("172.29.30.118"), 9300));
//        }
//    }
//
//    @Test
//    public void test_reloadDict() throws Exception {
//        LcDictReloadRequestBuilder requestBuilder = new LcDictReloadRequestBuilder(transportClient, LcDictReloadAction.INSTANCE);
//        LcDictReloadResponse lcDictReloadResponse = requestBuilder.execute().actionGet();
//
//        System.out.println(lcDictReloadResponse.toString());
//    }
//}
