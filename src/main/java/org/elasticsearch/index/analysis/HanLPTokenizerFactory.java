package org.elasticsearch.index.analysis;

import com.hankcs.hanlp.api.HanLP;
import com.hankcs.hanlp.seg.Segment;
import lc.lucene.tokenizer.HanLPTokenizer;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

public class HanLPTokenizerFactory extends AbstractTokenizerFactory {

    private HanLPType hanLPType;

    private boolean enablePorterStemming;

    public HanLPTokenizerFactory(IndexSettings indexSettings, String name, Settings settings, HanLPType hanLPType) {
        super(indexSettings, name, settings);
        this.hanLPType = hanLPType;
        this.enablePorterStemming = settings.getAsBoolean("enablePorterStemming", false);
    }

    public static HanLPTokenizerFactory getHanLPTokenizerFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        return new HanLPTokenizerFactory(indexSettings, name, settings, HanLPType.HANLP);
    }

    public static HanLPTokenizerFactory getHanLPStandardTokenizerFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        return new HanLPTokenizerFactory(indexSettings, name, settings, HanLPType.STANDARD);
    }

    public static HanLPTokenizerFactory getHanLPIndexTokenizerFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        return new HanLPTokenizerFactory(indexSettings, name, settings, HanLPType.INDEX);
    }

    public static HanLPTokenizerFactory getHanLPNLPTokenizerFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        return new HanLPTokenizerFactory(indexSettings, name, settings, HanLPType.NLP);
    }

    public static HanLPTokenizerFactory getHanLPNShortTokenizerFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        return new HanLPTokenizerFactory(indexSettings, name, settings, HanLPType.N_SHORT);
    }

    public static HanLPTokenizerFactory getHanLPDijkstraTokenizerFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        return new HanLPTokenizerFactory(indexSettings, name, settings, HanLPType.DIJKSTRA);
    }

    public static HanLPTokenizerFactory getHanLPSpeedTokenizerFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        return new HanLPTokenizerFactory(indexSettings, name, settings, HanLPType.SPEED);
    }

    @Override
    public Tokenizer create() {
        Segment segment = null;
        switch (hanLPType) {
            case HANLP:
                segment = HanLP.newViterbiSegment()
                        .enableOffset(true)
                        .enablePlaceRecognize(true)
                        .enableOrganizationRecognize(true)
                        .enableCustomDictionary(true)
                        .enablePartOfSpeechTagging(true)
                        .enableNumberQuantifierRecognize(true);
                return new HanLPTokenizer(segment, null, enablePorterStemming);
            case STANDARD:
                segment = HanLP.newViterbiSegment()
                        .enableOffset(true)
                        .enablePlaceRecognize(true)
                        .enableOrganizationRecognize(true)
                        .enableCustomDictionary(true)
                        .enablePartOfSpeechTagging(true)
                        .enableNumberQuantifierRecognize(true);
                return new HanLPTokenizer(segment, null, enablePorterStemming);
            case INDEX:
                segment = HanLP.newViterbiSegment()
                        .enableOffset(true)
                        .enableAllNamedEntityRecognize(true)
                        .enableIndexMode(true);
                return new HanLPTokenizer(segment, null, enablePorterStemming);
            case NLP:
                segment = HanLP.newViterbiSegment()
                        .enableOffset(true)
                        .enableAllNamedEntityRecognize(true)
                        .enablePartOfSpeechTagging(true)
                        .enableCustomDictionary(true)
                        .enableNumberQuantifierRecognize(true);

                return new HanLPTokenizer(segment, null, enablePorterStemming);
            case N_SHORT:
                segment = HanLP.newNShortSegment()
                        .enableOffset(true)
                        .enableCustomDictionary(false)
                        .enablePlaceRecognize(true)
                        .enableOrganizationRecognize(true);

                return new HanLPTokenizer(segment, null, enablePorterStemming);
            case DIJKSTRA:
                segment = HanLP.newDijkstraSegment()
                        .enableOffset(true)
                        .enableCustomDictionary(false)
                        .enablePlaceRecognize(true)
                        .enableOrganizationRecognize(true);

                return new HanLPTokenizer(segment, null, enablePorterStemming);
            case SPEED:
                segment = HanLP.newDoubleArrayTrieSegment()
                        .enableOffset(true)
                        .enableCustomDictionary(true)
                        .enablePartOfSpeechTagging(true);

                return new HanLPTokenizer(segment, null, enablePorterStemming);
            default:
                return null;
        }
    }

}
