package com.hankcs.hanlp.dictionary;

import com.google.common.base.Stopwatch;
import com.hankcs.hanlp.api.HanLpGlobalSettings;
import com.hankcs.hanlp.dictionary.common.CommonSynonymDictionary;
import com.hankcs.hanlp.io.IOSafeHelper;
import com.hankcs.hanlp.io.InputStreamOperator;
import com.hankcs.hanlp.log.HanLpLogger;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * 核心同义词词典
 *
 * @author hankcs
 */
public class CoreSynonymDictionary extends CommonSynonymDictionary {

    public static final CoreSynonymDictionary INSTANCE;

    static {
        INSTANCE = new CoreSynonymDictionary();

        String dictPath = HanLpGlobalSettings.CoreSynonymDictionaryDictionaryPath;
        IOSafeHelper.openAutoCloseableFileInputStream(dictPath, new InputStreamOperator() {
            @Override
            public void process(InputStream inputStream) throws Exception {
                Stopwatch stopwatch = Stopwatch.createStarted();

                INSTANCE.load(inputStream);

                HanLpLogger.info(CoreSynonymDictionary.class,
                        String.format("Load dictionary[%s], takes %s ms, path[%s]", "CoreSynonymDictionary",
                                stopwatch.elapsed(TimeUnit.MILLISECONDS), HanLpGlobalSettings.CoreSynonymDictionaryDictionaryPath));
            }
        });
    }
}