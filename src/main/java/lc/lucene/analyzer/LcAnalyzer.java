package lc.lucene.analyzer;

import lc.lucene.filter.StopWordTokenFilter;
import lc.lucene.filter.SynonymTokenFilter;
import lc.lucene.filter.UselessCharFilter;
import lc.lucene.tokenizer.LcTokenizer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.charfilter.HTMLStripCharFilter;
import org.apache.lucene.analysis.miscellaneous.UniqueTokenFilter;

import java.io.Reader;


public class LcAnalyzer extends Analyzer {

    private LcAnalyzerConfig lcAnalyzerConfig;

    public LcAnalyzer(LcAnalyzerConfig lcAnalyzerConfig) {
        this.lcAnalyzerConfig = lcAnalyzerConfig;
    }

    @Override
    protected Reader initReader(String fieldName, Reader reader) {
        if (lcAnalyzerConfig.isHtmlStrip()) {
            reader = new HTMLStripCharFilter(reader);
        }
        return reader;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        LcTokenizer.LcTokenizerConfig tokenizerConfig = new LcTokenizer.LcTokenizerConfig();
        tokenizerConfig.setIndexMode(lcAnalyzerConfig.isIndexMode());
        tokenizerConfig.setNamedEntityRecognize(lcAnalyzerConfig.isNamedEntityRecognize());

        Tokenizer tokenizer = new LcTokenizer(tokenizerConfig);
        TokenFilter filter = new UselessCharFilter(tokenizer);

        if (lcAnalyzerConfig.isLowerCase()) {
            filter = new LowerCaseFilter(filter);
        }

        if (lcAnalyzerConfig.isStopWordRecognize()) {
            filter = new StopWordTokenFilter(filter);
        }

        if (lcAnalyzerConfig.isSynonymRecognize()) {
            filter = new SynonymTokenFilter(filter);
        }
        filter = new UniqueTokenFilter(filter, true);
        return new TokenStreamComponents(tokenizer, filter);
    }

    public static class LcAnalyzerConfig {

        private boolean indexMode = false;

        private boolean htmlStrip = false;

        private boolean stopWordRecognize = true;

        private boolean synonymRecognize = false;

        private boolean namedEntityRecognize = true;

        private boolean lowerCase = true;

        public boolean isHtmlStrip() {
            return htmlStrip;
        }

        public void setHtmlStrip(boolean htmlStrip) {
            this.htmlStrip = htmlStrip;
        }

        public boolean isSynonymRecognize() {
            return synonymRecognize;
        }

        public void setSynonymRecognize(boolean synonymRecognize) {
            this.synonymRecognize = synonymRecognize;
        }

        public boolean isNamedEntityRecognize() {
            return namedEntityRecognize;
        }

        public void setNamedEntityRecognize(boolean namedEntityRecognize) {
            this.namedEntityRecognize = namedEntityRecognize;
        }

        public boolean isStopWordRecognize() {
            return stopWordRecognize;
        }

        public void setStopWordRecognize(boolean stopWordRecognize) {
            this.stopWordRecognize = stopWordRecognize;
        }

        public boolean isIndexMode() {
            return indexMode;
        }

        public void setIndexMode(boolean indexMode) {
            this.indexMode = indexMode;
        }

        public boolean isLowerCase() {
            return lowerCase;
        }

        public void setLowerCase(boolean lowerCase) {
            this.lowerCase = lowerCase;
        }
    }

}
