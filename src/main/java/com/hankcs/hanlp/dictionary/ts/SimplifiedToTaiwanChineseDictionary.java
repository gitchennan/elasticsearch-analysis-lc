/*
 * <summary></summary>
 * <author>Hankcs</author>
 * <email>me@hankcs.com</email>
 * <create-date>2016-08-30 AM10:29</create-date>
 *
 * <copyright file="SimplifiedToTaiwanChineseDictionary.java" company="码农场">
 * Copyright (c) 2008-2016, 码农场. All Right Reserved, http://www.hankcs.com/
 * This source is subject to Hankcs. Please contact Hankcs to get more information.
 * </copyright>
 */
package com.hankcs.hanlp.dictionary.ts;

import com.google.common.collect.Maps;
import com.hankcs.hanlp.api.HanLP;
import com.hankcs.hanlp.api.HanLpGlobalSettings;
import com.hankcs.hanlp.collection.AhoCorasick.AhoCorasickDoubleArrayTrie;
import com.hankcs.hanlp.log.HanLpLogger;

import java.util.TreeMap;

/**
 * 简体转台湾繁体
 *
 * @author hankcs
 */
public class SimplifiedToTaiwanChineseDictionary extends BaseChineseDictionary {
    static AhoCorasickDoubleArrayTrie<String> trie = AhoCorasickDoubleArrayTrie.newAhoCorasickDoubleArrayTrie();

    static {
        long start = System.currentTimeMillis();

        TreeMap<String, String> s2t = Maps.newTreeMap();
        TreeMap<String, String> t2tw = Maps.newTreeMap();
        if (!load(s2t, false, HanLpGlobalSettings.tcDictionaryRoot + "s2t.txt") ||
                !load(t2tw, false, HanLpGlobalSettings.tcDictionaryRoot + "t2tw.txt")) {
            throw new IllegalArgumentException("简体转台湾繁体词典加载失败");
        }
        combineChain(s2t, t2tw);
        trie.build(s2t);

        HanLpLogger.info(SimplifiedToTaiwanChineseDictionary.class,
                "简体转台湾繁体词典加载成功，耗时" + (System.currentTimeMillis() - start) + "ms");
    }

    public static String convertToTraditionalTaiwanChinese(String simplifiedChineseString) {
        return segLongest(simplifiedChineseString.toCharArray(), trie);
    }

    public static String convertToTraditionalTaiwanChinese(char[] simplifiedChinese) {
        return segLongest(simplifiedChinese, trie);
    }
}
