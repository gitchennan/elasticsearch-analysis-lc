//package com.hankcs.test.elasticsearch;
//
//import com.google.common.base.Stopwatch;
//import lc.lucene.service.CustomDictionaryReloadService;
//import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
//import org.elasticsearch.client.transport.TransportClient;
//import org.elasticsearch.common.settings.Settings;
//import org.elasticsearch.common.transport.InetSocketTransportAddress;
//import org.elasticsearch.transport.client.PreBuiltTransportClient;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.net.InetAddress;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//
//public class AnalyzeApiTest {
//    String text = "陆金所-中国平安集团倾力打造的投资理财平台。在健全的风险管控体系基础上，为投资者提供专业的理财服务。荣获亚洲银行家“最佳线上私人财富管理平台''。";
//    int loopCount = 10000;
//
//    public static TransportClient transportClient;
//
//
//    @Before
//    public void initClient() throws Exception {
//        if (transportClient == null) {
//            Settings settings = Settings.builder()
//                    .put("cluster.name", "lu-search-cluster")
//                    .put("client.transport.sniff", true)
//                    .build();
//
//            transportClient = new PreBuiltTransportClient(settings);
//            transportClient.addTransportAddresses(new InetSocketTransportAddress(InetAddress.getByName("192.168.0.125"), 9300));
//        }
//
//    }
//
//    @Test
//    public void test_Analyzer() throws Exception {
//        doAnalyze("hanlp_index");
//        doAnalyze("hanlp_standard");
//        doAnalyze("hanlp_nlp");
//        doAnalyze("hanlp_speed");
//        doAnalyze("ik_smart");
//    }
//
//    private void doAnalyze(String analyzer) {
//        System.out.println(String.format("--------------------[ %s ]-----------------------", analyzer));
//        Stopwatch stopwatch = Stopwatch.createStarted();
//        for (int i = 0; i < loopCount; i++) {
//            transportClient.admin().indices().prepareAnalyze(text).setAnalyzer(analyzer).execute().actionGet();
//        }
//        System.out.println("speed: " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + " ms, qps: " + 20000.0 / stopwatch.elapsed(TimeUnit.MILLISECONDS) * 1000);
//
//        AnalyzeResponse response = transportClient.admin().indices().prepareAnalyze(text).setAnalyzer(analyzer).execute().actionGet();
//        System.out.print("Tokens: ");
//        for (AnalyzeResponse.AnalyzeToken token : response) {
//            System.out.print(token.getTerm() + "/" + token.getType() + " ");
//        }
//        stopwatch.stop();
//        System.out.println();
//        System.out.println();
//
//    }
//
//    @Test
//    public void test_reload() throws Exception {
//        CustomDictionaryReloadService reloadService = new CustomDictionaryReloadService(transportClient, Executors.newSingleThreadScheduledExecutor());
//        reloadService.reloadCustomDictionary();
//
//    }
//}
