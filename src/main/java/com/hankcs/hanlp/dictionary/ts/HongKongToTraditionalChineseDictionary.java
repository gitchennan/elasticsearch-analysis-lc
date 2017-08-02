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

import com.hankcs.hanlp.api.HanLpGlobalSettings;
import com.hankcs.hanlp.collection.AhoCorasick.AhoCorasickDoubleArrayTrie;
import com.hankcs.hanlp.log.HanLpLogger;

import java.util.TreeMap;

/**
 * 香港繁体转繁体
 *
 * @author hankcs
 */
public class HongKongToTraditionalChineseDictionary extends BaseChineseDictionary {
    static AhoCorasickDoubleArrayTrie<String> trie = AhoCorasickDoubleArrayTrie.newAhoCorasickDoubleArrayTrie();

    static {
        long start = System.currentTimeMillis();

        TreeMap<String, String> hk2t = new TreeMap<String, String>();
        if (!load(hk2t, true, HanLpGlobalSettings.tcDictionaryRoot + "t2hk.txt")) {
            throw new IllegalArgumentException("香港繁体转繁体加载失败");
        }
        trie.build(hk2t);

        HanLpLogger.info(HongKongToTraditionalChineseDictionary.class,
                "香港繁体转繁体加载成功，耗时" + (System.currentTimeMillis() - start) + "ms");
    }

    public static String convertToTraditionalChinese(String traditionalHongKongChineseString) {
        return segLongest(traditionalHongKongChineseString.toCharArray(), trie);
    }

    public static String convertToTraditionalChinese(char[] traditionalHongKongChineseString) {
        return segLongest(traditionalHongKongChineseString, trie);
    }
}
