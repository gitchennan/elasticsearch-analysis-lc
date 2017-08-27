package com.hankcs.test.analyzer;

import com.hankcs.hanlp.api.HanLP;
import com.hankcs.hanlp.corpus.synonym.Synonym;
import com.hankcs.hanlp.dictionary.CoreSynonymDictionary;
import com.hankcs.hanlp.seg.common.Term;
import lc.lucene.analyzer.LcAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class LcAnalyzerTest extends BaseAnalyzerTest {

    @Test
    public void test_standardSegment() {
        List<Term> terms = HanLP.segment("对上海搞基有限公司的帮助文档来说是对的");
        showWords(terms);
    }

    @Test
    public void test_indexSegment() {
        List<Term> terms = HanLP.newViterbiSegment()
                .enableIndexMode(true)
                .seg("对上海陆金所金融科技有限公司的稳盈e享计划是高收益产品");

        showWords(terms);
    }

    @Test
    public void test_standardSegment2() {
        List<Term> terms = HanLP.newViterbiSegment()
                .enablePlaceRecognize(true)
                .enableAllNamedEntityRecognize(false)
                .enableCustomDictionary(true)
                .enableNumberQuantifierRecognize(true)
                .enableOffset(true)
                .seg("对上海陆金所金融科技有限公司的稳盈e享计划是高收益产品");

        showWords(terms);
    }


    @Test
    public void testLuSearchAnalysis() throws IOException {
        LcAnalyzer.LcAnalyzerConfig config = new LcAnalyzer.LcAnalyzerConfig();
        config.setStopWordRecognize(false);
        config.setSynonymRecognize(false);
//        config.setExtractFullPinyin(false);
        config.setNamedEntityRecognize(true);

        Analyzer analyzer = new LcAnalyzer(config);
        TokenStream tokenStream = analyzer.tokenStream("lc", "陈可芯同志是党的第三代中央领导集体的核心");
        showWords(tokenStream);
        tokenStream.close();
    }

    @Test
    public void testLuIndexAnalysis() throws IOException {
        LcAnalyzer.LcAnalyzerConfig config = new LcAnalyzer.LcAnalyzerConfig();
        config.setIndexMode(true);
        config.setStopWordRecognize(false);

        Analyzer analyzer = new LcAnalyzer(config);
        TokenStream tokenStream = analyzer.tokenStream("lc", "您就可以邀请朋友一起来玩转陆金所的稳盈安e了");
        showWords(tokenStream);
        tokenStream.close();
    }

    @Test
    public void test_synonym() {
        System.out.println(CoreSynonymDictionary.INSTANCE.find("电脑"));

        CoreSynonymDictionary.INSTANCE.add(Synonym.Type.EQUAL, "1", "2", "3");
        System.out.println(CoreSynonymDictionary.INSTANCE.find("1"));
    }
}
