package org.elasticsearch.index.analysis;

import lc.lucene.analyzer.LcAnalyzer;
import lc.lucene.analyzer.LcAnalyzerConfig;
import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

public class LcAnalyzerProvider extends AbstractIndexAnalyzerProvider<Analyzer> {

    private Analyzer analyzer;

    public LcAnalyzerProvider(IndexSettings indexSettings, Environment env, String name, Settings settings, LcAnalyzerType lcAnalyzerType) {
        super(indexSettings, name, settings);
        analyzer = new LcAnalyzer(parseLuAnalyzerConfig(settings, lcAnalyzerType));
    }

    public static LcAnalyzerProvider getLcIndexAnalyzerProvider(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        return new LcAnalyzerProvider(indexSettings, env, name, settings, LcAnalyzerType.LC_INDEX);
    }

    public static LcAnalyzerProvider getLcSearchAnalyzerProvider(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        return new LcAnalyzerProvider(indexSettings, env, name, settings, LcAnalyzerType.LC_SEARCH);
    }

    @Override
    public Analyzer get() {
        return analyzer;
    }

    public static LcAnalyzerConfig parseLuAnalyzerConfig(Settings settings, LcAnalyzerType hanLPType) {
        LcAnalyzerConfig config = new LcAnalyzerConfig();

        if (hanLPType == LcAnalyzerType.LC_INDEX) {
            config.setIndexMode(true);

            config.setStopWordRecognize(false);
            config.setNamedEntityRecognize(true);
            config.setExtractFullPinyin(false);
            config.setExtractPinyinFirstLetter(false);
            config.setLowerCase(true);
            config.setSynonymRecognize(false);
        }

        if (hanLPType == LcAnalyzerType.LC_SEARCH) {
            config.setIndexMode(false);

            config.setStopWordRecognize(true);
            config.setNamedEntityRecognize(true);
            config.setExtractFullPinyin(false);
            config.setExtractPinyinFirstLetter(false);
            config.setLowerCase(true);
            config.setSynonymRecognize(false);
        }

        config.setStopWordRecognize(settings.getAsBoolean("stopword", config.isStopWordRecognize()));
        config.setNamedEntityRecognize(settings.getAsBoolean("named_entity", config.isNamedEntityRecognize()));
        config.setSynonymRecognize(settings.getAsBoolean("synonym", config.isSynonymRecognize()));
        config.setLowerCase(settings.getAsBoolean("lowercase", config.isLowerCase()));
        config.setKeepChinese(settings.getAsBoolean("keep_chinese", config.isKeepChinese()));


        String pinyinSetting = settings.get("pinyin");
        if (pinyinSetting != null && pinyinSetting.trim().length() > 0) {
            if ("all".equalsIgnoreCase(pinyinSetting)) {
                config.setExtractPinyinFirstLetter(true);
                config.setExtractFullPinyin(true);
            }

            if ("head".equalsIgnoreCase(pinyinSetting)) {
                config.setExtractPinyinFirstLetter(true);
                config.setExtractFullPinyin(false);
            }

            if ("full".equalsIgnoreCase(pinyinSetting)) {
                config.setExtractPinyinFirstLetter(false);
                config.setExtractFullPinyin(true);
            }
        }

        return config;
    }

}
