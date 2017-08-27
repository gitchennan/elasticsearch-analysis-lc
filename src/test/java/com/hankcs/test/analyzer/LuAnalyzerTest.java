package com.hankcs.test.analyzer;

import lc.lucene.analyzer.LcAnalyzer;
import lc.lucene.filter.PinyinTokenFilter;
import lc.lucene.filter.UselessCharFilter;
import lc.lucene.tokenizer.LcTokenizer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.charfilter.HTMLStripCharFilter;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;

public class LuAnalyzerTest extends BaseAnalyzerTest {

    public static final String text = "中华人民共和国的腊梅是在冬季开的";

    @Test
    public void test_lu_max_cn() throws IOException {
        LcAnalyzer.LcAnalyzerConfig config = new LcAnalyzer.LcAnalyzerConfig();
        config.setStopWordRecognize(false);
        config.setSynonymRecognize(true);
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
        config.setNamedEntityRecognize(true);
        config.setIndexMode(false);

        Analyzer analyzer = new LcAnalyzer(config);
        TokenStream tokenStream = analyzer.tokenStream("lc", text);
        showWords(tokenStream);
        tokenStream.close();
    }

    @Test
    public void test_lu_smart_ngram_pinyin() throws IOException {
        String text = "灵活宝18个月-00123456 <link rel=\"stylesheet\" type=\"text/css\" href=\"../../../../../stylesheet.css\" title=\"Style\" />";

        Analyzer analyzer = new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String s) {
                LcTokenizer.LcTokenizerConfig tokenizerConfig = new LcTokenizer.LcTokenizerConfig();
                tokenizerConfig.setIndexMode(true);
                tokenizerConfig.setNamedEntityRecognize(true);
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

            @Override
            protected Reader initReader(String fieldName, Reader reader) {
                HTMLStripCharFilter filter = new HTMLStripCharFilter(reader);
                return super.initReader(fieldName, filter);
            }
        };

        TokenStream tokenStream = analyzer.tokenStream("lc", text);
        showWords(tokenStream);
        tokenStream.close();
    }
}
