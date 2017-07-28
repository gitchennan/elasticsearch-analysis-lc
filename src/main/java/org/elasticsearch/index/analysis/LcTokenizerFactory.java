package org.elasticsearch.index.analysis;

import com.hankcs.hanlp.api.HanLP;
import com.hankcs.hanlp.seg.Segment;
import lc.lucene.tokenizer.LcTokenizer;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

public class LcTokenizerFactory extends AbstractTokenizerFactory {

    public LcTokenizerFactory(IndexSettings indexSettings, String name, Settings settings) {
        super(indexSettings, name, settings);
    }

    public static LcTokenizerFactory getLcTokenizerFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        return new LcTokenizerFactory(indexSettings, name, settings);
    }

    @Override
    public Tokenizer create() {
        Segment segment = HanLP.newViterbiSegment()
                .enableOffset(true)
                .enableCustomDictionary(true)
                .enablePartOfSpeechTagging(true)
                .enableNumberQuantifierRecognize(true);

        return new LcTokenizer(segment);
    }
}
