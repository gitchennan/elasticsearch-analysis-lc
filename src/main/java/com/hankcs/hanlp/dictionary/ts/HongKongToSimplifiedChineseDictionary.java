/*
 * <summary></summary>
 * <author>Hankcs</author>
 * <email>me@hankcs.com</email>
 * <create-date>2016-08-30 AM10:29</create-date>
 *
 * <copyright file="SimplifiedToHongKongChineseDictionary.java" company="码农场">
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
 * 香港繁体转简体
 *
 * @author hankcs
 */
public class HongKongToSimplifiedChineseDictionary extends BaseChineseDictionary {
    static AhoCorasickDoubleArrayTrie<String> trie = new AhoCorasickDoubleArrayTrie<String>();

    static {
        long start = System.currentTimeMillis();

        TreeMap<String, String> t2s = Maps.newTreeMap();
        TreeMap<String, String> hk2t = Maps.newTreeMap();
        if (!load(t2s, false, HanLpGlobalSettings.tcDictionaryRoot + "t2s.txt") ||
                !load(hk2t, true, HanLpGlobalSettings.tcDictionaryRoot + "t2hk.txt")) {
            throw new IllegalArgumentException("香港繁体转简体加载失败");
        }
        combineReverseChain(t2s, hk2t, true);
        trie.build(t2s);

        HanLpLogger.info(HongKongToSimplifiedChineseDictionary.class,
                "香港繁体转简体加载成功，耗时" + (System.currentTimeMillis() - start) + "ms");
    }

    public static String convertToSimplifiedChinese(String traditionalHongKongChineseString) {
        return segLongest(traditionalHongKongChineseString.toCharArray(), trie);
    }

    public static String convertToSimplifiedChinese(char[] traditionalHongKongChineseString) {
        return segLongest(traditionalHongKongChineseString, trie);
    }
}
