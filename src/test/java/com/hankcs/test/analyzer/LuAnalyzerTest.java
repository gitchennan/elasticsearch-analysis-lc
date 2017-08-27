package com.hankcs.test.analyzer;

import lc.lucene.analyzer.LcAnalyzer;
import lc.lucene.filter.PinyinTokenFilter;
import lc.lucene.filter.UselessCharFilter;
import lc.lucene.tokenizer.LcTokenizer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter;
import org.junit.Test;

import java.io.IOException;

public class LuAnalyzerTest extends BaseAnalyzerTest {

    public static final String text = "中华人民共和国的腊梅是在冬季开的";

    @Test
    public void test_lu_max_cn() throws IOException {
        LcAnalyzer.LcAnalyzerConfig config = new LcAnalyzer.LcAnalyzerConfig();
        config.setStopWordRecognize(false);
        config.setSynonymRecognize(true);
//        config.setExtractFullPinyin(false);
        config.setNamedEntityRecognize(true);
        config.setIndexMode(true);

        Analyzer analyzer = new LcAnalyzer(config);
        TokenStream tokenStream = analyzer.tokenStream("lc", text);
        showWords(tokenStream);
        tokenStream.close();
    }

    @Test
    public void test_lu_smart_cn() throws IOException {
        LcAnalyzer.LcAnalyzerConfig config = new LcAnalyzer.LcAnalyzerConfig();
        config.setStopWordRecognize(false);
        config.setSynonymRecognize(true);
//        config.setExtractFullPinyin(false);
        config.setNamedEntityRecognize(true);
        config.setIndexMode(false);

        Analyzer analyzer = new LcAnalyzer(config);
        TokenStream tokenStream = analyzer.tokenStream("lc", text);
        showWords(tokenStream);
        tokenStream.close();
    }


    @Test
    public void test_lu_max_pinyin() throws IOException {
        LcAnalyzer.LcAnalyzerConfig config = new LcAnalyzer.LcAnalyzerConfig();
        config.setStopWordRecognize(false);
        config.setSynonymRecognize(true);
//        config.setExtractFullPinyin(false);
        config.setNamedEntityRecognize(true);
        config.setIndexMode(true);
//        config.setKeepChinese(false);
//        config.setExtractFullPinyin(true);
//        config.setExtractPinyinFirstLetter(false);

        Analyzer analyzer = new LcAnalyzer(config);
        TokenStream tokenStream = analyzer.tokenStream("lc", text);
        showWords(tokenStream);
        tokenStream.close();
    }

    @Test
    public void test_lu_max_pinyin_head() throws IOException {
        LcAnalyzer.LcAnalyzerConfig config = new LcAnalyzer.LcAnalyzerConfig();
        config.setStopWordRecognize(false);
        config.setSynonymRecognize(true);
//        config.setExtractFullPinyin(false);
        config.setNamedEntityRecognize(true);
        config.setIndexMode(true);
//        config.setKeepChinese(false);
//        config.setExtractFullPinyin(false);
//        config.setExtractPinyinFirstLetter(true);

        Analyzer analyzer = new LcAnalyzer(config);
        TokenStream tokenStream = analyzer.tokenStream("lc", text);
        showWords(tokenStream);
        tokenStream.close();
    }

    @Test
    public void test_lu_smart_pinyin() throws IOException {
        LcAnalyzer.LcAnalyzerConfig config = new LcAnalyzer.LcAnalyzerConfig();
        config.setStopWordRecognize(false);
        config.setSynonymRecognize(true);
//        config.setExtractFullPinyin(false);
        config.setNamedEntityRecognize(true);
        config.setIndexMode(false);
//        config.setKeepChinese(false);
//        config.setExtractFullPinyin(true);
//        config.setExtractPinyinFirstLetter(false);

        Analyzer analyzer = new LcAnalyzer(config);
        TokenStream tokenStream = analyzer.tokenStream("lc", text);
        showWords(tokenStream);
        tokenStream.close();
    }

    @Test
    public void test_lu_smart_pinyin_head() throws IOException {
        LcAnalyzer.LcAnalyzerConfig config = new LcAnalyzer.LcAnalyzerConfig();
        config.setStopWordRecognize(false);
        config.setSynonymRecognize(true);
//        config.setExtractFullPinyin(false);
        config.setNamedEntityRecognize(true);
        config.setIndexMode(false);
//        config.setKeepChinese(false);
//        config.setExtractFullPinyin(false);
//        config.setExtractPinyinFirstLetter(true);

        Analyzer analyzer = new LcAnalyzer(config);
        TokenStream tokenStream = analyzer.tokenStream("lc", text);
        showWords(tokenStream);
        tokenStream.close();
    }

    @Test
    public void test_lu_smart_ngram_pinyin() throws IOException {
        String text = "零活宝-123456";

        Analyzer analyzer = new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String s) {
                LcTokenizer.LcTokenizerConfig tokenizerConfig = new LcTokenizer.LcTokenizerConfig();
                tokenizerConfig.setIndexMode(true);
                Tokenizer tokenizer = new LcTokenizer(tokenizerConfig);

                TokenFilter filter = new UselessCharFilter(tokenizer);

                PinyinTokenFilter.PinyinTokenFilterConfig pinyinTokenFilterConfig
                        = new PinyinTokenFilter.PinyinTokenFilterConfig();
                pinyinTokenFilterConfig.setKeepChinese(false);
                pinyinTokenFilterConfig.setPinyinMode("pinyin_all");
                filter = new PinyinTokenFilter(filter, pinyinTokenFilterConfig);
                filter = new EdgeNGramTokenFilter(filter, 1, 30);


                return new TokenStreamComponents(tokenizer, filter);
            }
        };

        TokenStream tokenStream = analyzer.tokenStream("lc", text);
        showWords(tokenStream);
        tokenStream.close();
    }
}
