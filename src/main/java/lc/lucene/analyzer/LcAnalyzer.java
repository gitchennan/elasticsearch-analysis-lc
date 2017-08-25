package lc.lucene.analyzer;

import com.hankcs.hanlp.api.HanLP;
import com.hankcs.hanlp.seg.Segment;
import lc.lucene.filter.*;
import lc.lucene.tokenizer.LcTokenizer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.ngram.NGramTokenizer;


public class LcAnalyzer extends Analyzer {

    private LcAnalyzerConfig lcAnalyzerConfig;

    public LcAnalyzer(LcAnalyzerConfig lcAnalyzerConfig) {
        this.lcAnalyzerConfig = lcAnalyzerConfig;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = null;
        if(lcAnalyzerConfig.isSingleCharMode()) {
            tokenizer = new NGramTokenizer(1, 1);
        }
        else {
            Segment segment = HanLP.newViterbiSegment()
                    .enableOffset(true)
                    .enableCustomDictionary(true)
                    .enablePartOfSpeechTagging(true)
                    .enableNumberQuantifierRecognize(true);


            segment.enableIndexMode(lcAnalyzerConfig.isIndexMode());
            segment.enableAllNamedEntityRecognize(lcAnalyzerConfig.isNamedEntityRecognize());
            tokenizer = new LcTokenizer(segment);
        }

        TokenFilter filter = new WhitespaceTokenFilter(tokenizer);
        filter = new UselessCharFilter(filter);

        if (lcAnalyzerConfig.isLowerCase()) {
            filter = new LowerCaseFilter(filter);
        }

        if (lcAnalyzerConfig.isStopWordRecognize()) {
            filter = new StopWordTokenFilter(filter);
        }

        if (lcAnalyzerConfig.isSynonymRecognize()) {
            filter = new SynonymTokenFilter(filter);
        }

        if (lcAnalyzerConfig.isExtractFullPinyin() && lcAnalyzerConfig.isExtractPinyinFirstLetter()) {
            filter = new PinyinTokenFilter(filter, "all", lcAnalyzerConfig.isKeepChinese());
        }
        else if (lcAnalyzerConfig.isExtractFullPinyin()) {
            filter = new PinyinTokenFilter(filter, "full", lcAnalyzerConfig.isKeepChinese());
        }
        else if (lcAnalyzerConfig.isExtractPinyinFirstLetter()) {
            filter = new PinyinTokenFilter(filter, "head", lcAnalyzerConfig.isKeepChinese());
        }

        return new TokenStreamComponents(tokenizer, filter);
    }
}
