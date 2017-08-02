package com.hankcs.hanlp.dictionary.py;

import com.google.common.collect.Maps;
import com.hankcs.hanlp.api.HanLpGlobalSettings;
import com.hankcs.hanlp.collection.AhoCorasick.AhoCorasickDoubleArrayTrie;
import com.hankcs.hanlp.collection.trie.DoubleArrayTrie;
import com.hankcs.hanlp.corpus.dictionary.StringDictionary;
import com.hankcs.hanlp.dictionary.BaseSearcher;
import com.hankcs.hanlp.dictionary.searcher.DoubleArrayTrieSearcher;
import com.hankcs.hanlp.log.HanLpLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author hankcs
 */
public class PinyinDictionary {

    static AhoCorasickDoubleArrayTrie<Pinyin[]> trie = new AhoCorasickDoubleArrayTrie<Pinyin[]>();

    static {
        load(HanLpGlobalSettings.PinyinDictionaryPath);
    }

    /**
     * 读取词典
     */
    static boolean load(String... pathList) {
        StringDictionary dictionary = new StringDictionary("=");
        if (!dictionary.load(pathList)) {
            return false;
        }
        TreeMap<String, Pinyin[]> map = Maps.newTreeMap();
        for (Map.Entry<String, String> entry : dictionary.entrySet()) {
            String[] args = entry.getValue().split(",");
            Pinyin[] pinyinValue = new Pinyin[args.length];
            for (int i = 0; i < pinyinValue.length; ++i) {
                try {
                    Pinyin pinyin = Pinyin.valueOf(args[i]);
                    pinyinValue[i] = pinyin;
                }
                catch (IllegalArgumentException e) {
                    HanLpLogger.error(PinyinDictionary.class, "Failed to load pinyin dict", e);
                    return false;
                }
            }
            map.put(entry.getKey(), pinyinValue);
        }
        trie.build(map);

        return true;
    }

    public static Pinyin[] get(String key) {
        return trie.get(key);
    }

    /**
     * 转为拼音
     *
     * @return List形式的拼音，对应每一个字（所谓字，指的是任意字符）
     */
    public static List<Pinyin> convertToPinyin(String text) {
        return segLongest(text.toCharArray(), trie);
    }

    public static List<Pinyin> convertToPinyin(String text, boolean remainNone) {
        return segLongest(text.toCharArray(), trie, remainNone);
    }

    /**
     * 转为拼音
     *
     * @return 数组形式的拼音
     */
    public static Pinyin[] convertToPinyinArray(String text) {
        List<Pinyin> pinyinList = convertToPinyin(text);
        return pinyinList.toArray(new Pinyin[pinyinList.size()]);
    }

    public static BaseSearcher<Pinyin[]> getSearcher(char[] text, DoubleArrayTrie<Pinyin[]> trie) {
        return new DoubleArrayTrieSearcher<Pinyin[]>(String.valueOf(text), trie);
    }

    /**
     * 用最长分词算法匹配拼音
     */
    protected static List<Pinyin> segLongest(char[] charArray, AhoCorasickDoubleArrayTrie<Pinyin[]> trie) {
        return segLongest(charArray, trie, true);
    }

    protected static List<Pinyin> segLongest(char[] charArray, AhoCorasickDoubleArrayTrie<Pinyin[]> trie, boolean remainNone) {
        final Pinyin[][] wordNet = new Pinyin[charArray.length][];
        trie.parseText(charArray, new AhoCorasickDoubleArrayTrie.IHit<Pinyin[]>() {
            @Override
            public void hit(int begin, int end, Pinyin[] value) {
                int length = end - begin;
                if (wordNet[begin] == null || length > wordNet[begin].length) {
                    wordNet[begin] = length == 1 ? new Pinyin[]{value[0]} : value;
                }
            }
        });
        List<Pinyin> pinyinList = new ArrayList<Pinyin>(charArray.length);
        for (int offset = 0; offset < wordNet.length; ) {
            if (wordNet[offset] == null) {
                if (remainNone) {
                    pinyinList.add(Pinyin.none5);
                }
                ++offset;
                continue;
            }
            for (Pinyin pinyin : wordNet[offset]) {
                pinyinList.add(pinyin);
            }
            offset += wordNet[offset].length;
        }
        return pinyinList;
    }
}
