/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/11/12 14:45</create-date>
 *
 * <copyright file="TranslatedPersonDictionary.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package com.hankcs.hanlp.dictionary.nr;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.hankcs.hanlp.api.HanLpGlobalSettings;
import com.hankcs.hanlp.collection.trie.DoubleArrayTrie;
import com.hankcs.hanlp.io.IOSafeHelper;
import com.hankcs.hanlp.io.InputStreamOperator;
import com.hankcs.hanlp.log.HanLpLogger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * 翻译人名词典，储存和识别翻译人名
 *
 * @author hankcs
 */
public class TranslatedPersonDictionary {
    static String path = HanLpGlobalSettings.TranslatedPersonDictionaryPath;
    static DoubleArrayTrie<Boolean> trie;

    static {
        Stopwatch stopwatch = Stopwatch.createStarted();
        if (!load()) {
            HanLpLogger.error(TranslatedPersonDictionary.class,
                    String.format("Failed to Load dictionary[%s], takes %sms, path[%s] ",
                            "TranslatedPersonDictionary", stopwatch.elapsed(TimeUnit.MILLISECONDS), HanLpGlobalSettings.TranslatedPersonDictionaryPath));
        }
        else {
            HanLpLogger.info(TranslatedPersonDictionary.class,
                    String.format("Load dictionary[%-25s], takes %sms, path[%s] ",
                            "TranslatedPersonDictionary", stopwatch.elapsed(TimeUnit.MILLISECONDS), HanLpGlobalSettings.TranslatedPersonDictionaryPath));
        }
        stopwatch.stop();
    }

    static boolean load() {
        trie = new DoubleArrayTrie<Boolean>();
        return IOSafeHelper.openAutoCloseableFileInputStream(path, new InputStreamOperator() {
            @Override
            public void process(InputStream input) throws Exception {
                BufferedReader br = new BufferedReader(new InputStreamReader(input, "UTF-8"));
                String line;
                TreeMap<String, Boolean> map = Maps.newTreeMap();
                TreeMap<Character, Integer> charFrequencyMap = Maps.newTreeMap();
                while ((line = br.readLine()) != null) {
                    map.put(line, true);
                    // 音译人名常用字词典自动生成
                    for (char c : line.toCharArray()) {
                        // 排除一些过于常用的字
                        if ("不赞".indexOf(c) >= 0) continue;
                        Integer f = charFrequencyMap.get(c);
                        if (f == null) f = 0;
                        charFrequencyMap.put(c, f + 1);
                    }
                }
                map.put(String.valueOf('·'), true);
                // 将常用字也加进去
                for (Map.Entry<Character, Integer> entry : charFrequencyMap.entrySet()) {
                    if (entry.getValue() < 10) continue;
                    map.put(String.valueOf(entry.getKey()), true);
                }
                trie.build(map);
            }
        });
    }

    public static boolean containsKey(String key) {
        return trie.containsKey(key);
    }

    /**
     * 时报包含key，且key至少长length
     */
    public static boolean containsKey(String key, int length) {
        if (!trie.containsKey(key)) {
            return false;
        }
        return key.length() >= length;
    }
}
