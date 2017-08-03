package com.hankcs.hanlp.dictionary.py;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hankcs.hanlp.api.HanLpGlobalSettings;
import com.hankcs.hanlp.collection.AhoCorasick.AhoCorasickDoubleArrayTrie;
import com.hankcs.hanlp.corpus.dictionary.StringDictionary;
import com.hankcs.hanlp.dictionary.FileSystemTxtDictionary;
import com.hankcs.hanlp.log.HanLpLogger;

import java.util.List;
import java.util.TreeMap;

/**
 * @author hankcs
 */
public class PinyinDictionary extends FileSystemTxtDictionary {

    private volatile AhoCorasickDoubleArrayTrie<Pinyin[]> acDoubleArrayTrie;

    private TreeMap<String, Pinyin[]> pinyinSourceHolder;

    public static PinyinDictionary INSTANCE;

    static {
        INSTANCE = new PinyinDictionary();
        INSTANCE.load();
    }

    public PinyinDictionary() {
        super(HanLpGlobalSettings.PinyinDictionaryPath);
        acDoubleArrayTrie = AhoCorasickDoubleArrayTrie.newAhoCorasickDoubleArrayTrie();
        pinyinSourceHolder = Maps.newTreeMap();
    }

    @Override
    public String dictionaryName() {
        return "PinyinDictionary";
    }

    @Override
    protected void onDictionaryLoaded() {
        acDoubleArrayTrie.build(pinyinSourceHolder);

        pinyinSourceHolder.clear();
        pinyinSourceHolder = null;
    }

    @Override
    protected void onLoadLine(String line) {
        String[] paramArray = line.split("=", 2);
        if (paramArray.length != 2) {
            HanLpLogger.error(StringDictionary.class, "词典有一行读取错误： " + line);
            return;
        }

        String[] args = paramArray[1].split(",");
        Pinyin[] pinyinValue = new Pinyin[args.length];
        for (int i = 0; i < pinyinValue.length; ++i) {
            try {
                Pinyin pinyin = Pinyin.valueOf(args[i]);
                pinyinValue[i] = pinyin;
            }
            catch (IllegalArgumentException e) {
                HanLpLogger.error(PinyinDictionary.class, "Failed to load pinyin dict", e);
                return;
            }
        }
        pinyinSourceHolder.put(paramArray[0], pinyinValue);
    }

    public Pinyin[] get(String key) {
        return acDoubleArrayTrie.get(key);
    }

    /**
     * 转为拼音
     *
     * @return List形式的拼音，对应每一个字（所谓字，指的是任意字符）
     */
    public List<Pinyin> convertToPinyin(String text) {
        return segLongest(text.toCharArray(), acDoubleArrayTrie);
    }

    public List<Pinyin> convertToPinyin(String text, boolean remainNone) {
        return segLongest(text.toCharArray(), acDoubleArrayTrie, remainNone);
    }

    /**
     * 用最长分词算法匹配拼音
     */
    protected List<Pinyin> segLongest(char[] charArray, AhoCorasickDoubleArrayTrie<Pinyin[]> trie) {
        return segLongest(charArray, trie, true);
    }

    protected List<Pinyin> segLongest(char[] charArray, AhoCorasickDoubleArrayTrie<Pinyin[]> trie, boolean remainNone) {
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

        List<Pinyin> pinyinList = Lists.newArrayListWithCapacity(charArray.length);
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
