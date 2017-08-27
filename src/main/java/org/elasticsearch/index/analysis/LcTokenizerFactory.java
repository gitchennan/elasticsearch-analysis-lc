package org.elasticsearch.index.analysis;

import lc.lucene.tokenizer.LcTokenizer;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

public class LcTokenizerFactory extends AbstractTokenizerFactory {

    private Settings settings;

    public LcTokenizerFactory(IndexSettings indexSettings, String name, Settings settings) {
        super(indexSettings, name, settings);
        this.settings = settings;
    }

    public static LcTokenizerFactory getLcTokenizerFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        return new LcTokenizerFactory(indexSettings, name, settings);
    }

    @Override
    public Tokenizer create() {
        LcTokenizer.LcTokenizerConfig tokenizerConfig = new LcTokenizer.LcTokenizerConfig();
        tokenizerConfig.setIndexMode(settings.getAsBoolean("index_mode", tokenizerConfig.isIndexMode()));
        tokenizerConfig.setNamedEntityRecognize(settings.getAsBoolean("named_entity", tokenizerConfig.isNamedEntityRecognize()));

        return new LcTokenizer(tokenizerConfig);
    }
}
