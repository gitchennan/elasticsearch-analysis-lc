package com.hankcs.hanlp.dictionary;

import com.google.common.base.Stopwatch;
import com.hankcs.hanlp.api.HanLpGlobalSettings;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.log.HanLpLogger;

import java.util.concurrent.TimeUnit;

/**
 * 核心词典词性转移矩阵
 */
public class CoreDictionaryTransformMatrixDictionary {
    public static TransformMatrixDictionary<Nature> transformMatrixDictionary;

    static {
        Stopwatch stopwatch = Stopwatch.createStarted();
        transformMatrixDictionary = new TransformMatrixDictionary<Nature>(Nature.class);
        if (!transformMatrixDictionary.load(HanLpGlobalSettings.CoreDictionaryTransformMatrixDictionaryPath)) {
            HanLpLogger.error(CoreDictionaryTransformMatrixDictionary.class,
                    String.format("Failed to load dictionary[CoreDictTransformMatrix], takes %s ms, path[%s]",
                            stopwatch.elapsed(TimeUnit.MILLISECONDS), HanLpGlobalSettings.CoreDictionaryTransformMatrixDictionaryPath));
        }
        else {
            HanLpLogger.info(CoreDictionaryTransformMatrixDictionary.class,
                    String.format("Load dictionary[%s], takes %s ms, path[%s]",
                            "CoreDictTransformMatrix", stopwatch.elapsed(TimeUnit.MILLISECONDS), HanLpGlobalSettings.CoreDictionaryTransformMatrixDictionaryPath));
        }
    }
}
