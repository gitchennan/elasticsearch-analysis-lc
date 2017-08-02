
package com.hankcs.hanlp.dictionary.ts;

import com.google.common.collect.Maps;
import com.hankcs.hanlp.api.HanLpGlobalSettings;
import com.hankcs.hanlp.collection.AhoCorasick.AhoCorasickDoubleArrayTrie;
import com.hankcs.hanlp.log.HanLpLogger;

import java.util.TreeMap;

/**
 * 繁体转香港繁体
 *
 * @author hankcs
 */
public class TraditionalToHongKongChineseDictionary extends BaseChineseDictionary {
    static AhoCorasickDoubleArrayTrie<String> trie = AhoCorasickDoubleArrayTrie.newAhoCorasickDoubleArrayTrie();

    static {
        long start = System.currentTimeMillis();

        TreeMap<String, String> t2hk = Maps.newTreeMap();
        if (!load(t2hk, false, HanLpGlobalSettings.tcDictionaryRoot + "t2hk.txt")) {
            throw new IllegalArgumentException("繁体转香港繁体加载失败");
        }
        trie.build(t2hk);

        HanLpLogger.info(TraditionalToHongKongChineseDictionary.class,
                "繁体转香港繁体加载成功，耗时" + (System.currentTimeMillis() - start) + "ms");
    }

    public static String convertToHongKongTraditionalChinese(String traditionalChineseString) {
        return segLongest(traditionalChineseString.toCharArray(), trie);
    }

    public static String convertToHongKongTraditionalChinese(char[] traditionalHongKongChineseString) {
        return segLongest(traditionalHongKongChineseString, trie);
    }
}
