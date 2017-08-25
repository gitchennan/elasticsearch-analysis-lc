package com.hankcs.test.analyzer;

import lc.lucene.analyzer.LcAnalyzer;
import lc.lucene.analyzer.LcAnalyzerConfig;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.junit.Test;

import java.io.IOException;

public class LuAnalyzerTest extends BaseAnalyzerTest {

    public static final String text = "中华人民共和国的腊梅是在冬季开的";

    @Test
    public void test_lu_max_cn() throws IOException {
        LcAnalyzerConfig config = new LcAnalyzerConfig();
        config.setStopWordRecognize(false);
        config.setSynonymRecognize(true);
        config.setExtractFullPinyin(false);
        config.setNamedEntityRecognize(true);
        config.setIndexMode(true);

        Analyzer analyzer = new LcAnalyzer(config);
        TokenStream tokenStream = analyzer.tokenStream("lc", text);
        showWords(tokenStream);
        tokenStream.close();
    }

    @Test
    public void test_lu_smart_cn() throws IOException {
        LcAnalyzerConfig config = new LcAnalyzerConfig();
        config.setStopWordRecognize(false);
        config.setSynonymRecognize(true);
        config.setExtractFullPinyin(false);
        config.setNamedEntityRecognize(true);
        config.setIndexMode(false);

        Analyzer analyzer = new LcAnalyzer(config);
        TokenStream tokenStream = analyzer.tokenStream("lc", text);
        showWords(tokenStream);
        tokenStream.close();
    }

    @Test
    public void test_lu_single_cn() throws IOException {
        LcAnalyzerConfig config = new LcAnalyzerConfig();
        config.setStopWordRecognize(false);
        config.setSynonymRecognize(false);
        config.setExtractFullPinyin(false);
        config.setNamedEntityRecognize(false);
        config.setSingleCharMode(true);

        Analyzer analyzer = new LcAnalyzer(config);
        TokenStream tokenStream = analyzer.tokenStream("lc", text);
        showWords(tokenStream);
        tokenStream.close();
    }

    @Test
    public void test_lu_single_pinyin() throws IOException {
        LcAnalyzerConfig config = new LcAnalyzerConfig();
        config.setStopWordRecognize(false);
        config.setSynonymRecognize(false);
        config.setExtractFullPinyin(true);
        config.setNamedEntityRecognize(false);
        config.setSingleCharMode(true);
        config.setKeepChinese(false);

        Analyzer analyzer = new LcAnalyzer(config);
        TokenStream tokenStream = analyzer.tokenStream("lc", text);
        showWords(tokenStream);
        tokenStream.close();
    }

    @Test
    public void test_lu_single_pinyin_head() throws IOException {
        LcAnalyzerConfig config = new LcAnalyzerConfig();
        config.setStopWordRecognize(false);
        config.setSynonymRecognize(false);
        config.setExtractFullPinyin(false);
        config.setNamedEntityRecognize(false);
        config.setSingleCharMode(true);
        config.setKeepChinese(false);
        config.setExtractPinyinFirstLetter(true);

        Analyzer analyzer = new LcAnalyzer(config);
        TokenStream tokenStream = analyzer.tokenStream("lc", text);
        showWords(tokenStream);
        tokenStream.close();
    }


    @Test
    public void test_lu_max_pinyin() throws IOException {
        LcAnalyzerConfig config = new LcAnalyzerConfig();
        config.setStopWordRecognize(false);
        config.setSynonymRecognize(true);
        config.setExtractFullPinyin(false);
        config.setNamedEntityRecognize(true);
        config.setIndexMode(true);
        config.setKeepChinese(false);
        config.setExtractFullPinyin(true);
        config.setExtractPinyinFirstLetter(false);

        Analyzer analyzer = new LcAnalyzer(config);
        TokenStream tokenStream = analyzer.tokenStream("lc", text);
        showWords(tokenStream);
        tokenStream.close();
    }

    @Test
    public void test_lu_max_pinyin_head() throws IOException {
        LcAnalyzerConfig config = new LcAnalyzerConfig();
        config.setStopWordRecognize(false);
        config.setSynonymRecognize(true);
        config.setExtractFullPinyin(false);
        config.setNamedEntityRecognize(true);
        config.setIndexMode(true);
        config.setKeepChinese(false);
        config.setExtractFullPinyin(false);
        config.setExtractPinyinFirstLetter(true);

        Analyzer analyzer = new LcAnalyzer(config);
        TokenStream tokenStream = analyzer.tokenStream("lc", text);
        showWords(tokenStream);
        tokenStream.close();
    }

    @Test
    public void test_lu_smart_pinyin() throws IOException {
        LcAnalyzerConfig config = new LcAnalyzerConfig();
        config.setStopWordRecognize(false);
        config.setSynonymRecognize(true);
        config.setExtractFullPinyin(false);
        config.setNamedEntityRecognize(true);
        config.setIndexMode(false);
        config.setKeepChinese(false);
        config.setExtractFullPinyin(true);
        config.setExtractPinyinFirstLetter(false);

        Analyzer analyzer = new LcAnalyzer(config);
        TokenStream tokenStream = analyzer.tokenStream("lc", text);
        showWords(tokenStream);
        tokenStream.close();
    }

    @Test
    public void test_lu_smart_pinyin_head() throws IOException {
        LcAnalyzerConfig config = new LcAnalyzerConfig();
        config.setStopWordRecognize(false);
        config.setSynonymRecognize(true);
        config.setExtractFullPinyin(false);
        config.setNamedEntityRecognize(true);
        config.setIndexMode(false);
        config.setKeepChinese(false);
        config.setExtractFullPinyin(false);
        config.setExtractPinyinFirstLetter(true);

        Analyzer analyzer = new LcAnalyzer(config);
        TokenStream tokenStream = analyzer.tokenStream("lc", text);
        showWords(tokenStream);
        tokenStream.close();
    }
}
