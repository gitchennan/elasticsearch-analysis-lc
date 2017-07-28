/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/11/2 11:41</create-date>
 *
 * <copyright file="SYTDictionary.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package com.hankcs.hanlp.dictionary.py;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.hankcs.hanlp.api.HanLpGlobalSettings;
import com.hankcs.hanlp.collection.set.UnEmptyStringSet;
import com.hankcs.hanlp.corpus.dictionary.StringDictionary;
import com.hankcs.hanlp.log.HanLpLogger;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 声母韵母音调词典
 *
 * @author hankcs
 */
public class SYTDictionary {
    static Set<String> smSet = new UnEmptyStringSet();
    static Set<String> ymSet = new UnEmptyStringSet();
    static Set<String> ydSet = new UnEmptyStringSet();
    static Map<String, String[]> map = Maps.newTreeMap();

    static {
        Stopwatch stopwatch = Stopwatch.createStarted();
        StringDictionary dictionary = new StringDictionary();
        if (dictionary.load(HanLpGlobalSettings.SYTDictionaryPath)) {
            HanLpLogger.info(HanLpGlobalSettings.class,
                    String.format("Load dictionary[%s], takes %s ms, path[%s]",
                            "SYTDictionary", stopwatch.elapsed(TimeUnit.MILLISECONDS), HanLpGlobalSettings.SYTDictionaryPath));

            for (Map.Entry<String, String> entry : dictionary.entrySet()) {
                //      0  1 2
                // bai1=b,ai,1
                String[] args = entry.getValue().split(",");
                if (args[0].length() == 0) {
                    args[0] = "none";
                }
                smSet.add(args[0]);
                ymSet.add(args[1]);
                ydSet.add(args[2]);
                String[] valueArray = new String[4];
                System.arraycopy(args, 0, valueArray, 0, args.length);
                valueArray[3] = PinyinUtil.convertToneNumber2ToneMark(entry.getKey());
                map.put(entry.getKey(), valueArray);
            }
        }
        else {
            HanLpLogger.error(HanLpGlobalSettings.class,
                    String.format("Load dictionary[%s], takes %s ms, path[%s]",
                            "SYTDictionary", stopwatch.elapsed(TimeUnit.MILLISECONDS), HanLpGlobalSettings.SYTDictionaryPath));
        }
    }

}
