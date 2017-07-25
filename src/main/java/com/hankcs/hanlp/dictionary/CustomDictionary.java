/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/5/10 12:42</create-date>
 *
 * <copyright file="WordDictionary.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package com.hankcs.hanlp.dictionary;


import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hankcs.hanlp.api.HanLpGlobalSettings;
import com.hankcs.hanlp.collection.AhoCorasick.AhoCorasickDoubleArrayTrie;
import com.hankcs.hanlp.collection.trie.DoubleArrayTrie;
import com.hankcs.hanlp.collection.trie.bintrie.BinTrie;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.dictionary.other.CharTable;
import com.hankcs.hanlp.io.IOSafeHelper;
import com.hankcs.hanlp.io.LineOperator;
import com.hankcs.hanlp.log.HanLpLogger;
import com.hankcs.hanlp.utility.LexiconUtility;
import com.hankcs.hanlp.utility.Predefine;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 用户自定义词典
 *
 * @author He Han
 */
public class CustomDictionary {
    /**
     * 用于储存用户动态插入词条的二分trie树
     */
    public static BinTrie<WordAttribute> trie;
    public static DoubleArrayTrie<WordAttribute> datTrie;
    /**
     * 第一个是主词典，其他是副词典
     */
    public final static String[] CUSTOM_DICTIONARY_PATHS = HanLpGlobalSettings.CustomDictionaryPath;

    // 自动加载词典
    static {
        if (!loadMainDictionary()) {
            HanLpLogger.error(CustomDictionary.class,
                    String.format("Failed to load dictionary[CustomDictionary] path[%s]", Arrays.toString(CUSTOM_DICTIONARY_PATHS)));
        }
    }

    private static boolean loadMainDictionary() {
        HanLpLogger.debug(CustomDictionary.class, "Begin to load custom dictionaries.");
        datTrie = DoubleArrayTrie.newDoubleArrayTrie();

        TreeMap<String, WordAttribute> map = Maps.newTreeMap();
        LinkedHashSet<Nature> customNatureCollector = Sets.newLinkedHashSet();

        int preLoadWordCount = 0;
        for (String p : CUSTOM_DICTIONARY_PATHS) {
            Stopwatch stopwatch = Stopwatch.createStarted();
            Nature defaultNature = Nature.n;
            int cut = p.indexOf(' ');
            if (cut > 0) {
                // 有默认词性
                String nature = p.substring(cut + 1);
                p = p.substring(0, cut);
                try {
                    defaultNature = LexiconUtility.convertStringToNature(nature, customNatureCollector);
                }
                catch (Exception e) {
                    HanLpLogger.error(CustomDictionary.class,
                            String.format("Failed to convert nature[%s] to string, path[%s]", nature, p), e);
                    continue;
                }
            }

            boolean success = load(p, defaultNature, map, customNatureCollector);
            if (!success) {
                HanLpLogger.error(CustomDictionary.class,
                        String.format("Failed to load dictionary[CustomDictionary], invalid file name, path[%s]", p));
            }
            else {
                HanLpLogger.info(CustomDictionary.class,
                        String.format("Load dictionary[%-25s], takes %s ms, using nature[%s], word count[%s], path[%s]",
                                "CustomDictionary", stopwatch.elapsed(TimeUnit.MILLISECONDS), defaultNature, map.size() - preLoadWordCount, p));
            }
            preLoadWordCount = map.size();
        }

        if (map.size() == 0) {
            HanLpLogger.warn(CustomDictionary.class, "There's no any word found in custom dictionaries.");
            // 当作空白占位符
            map.put(Predefine.TAG_OTHER, null);
        }

        try {
            datTrie.build(map);
        }
        catch (Exception e) {
            HanLpLogger.error(CustomDictionary.class, "Failed to load custom dictionary", e);
        }
        return true;
    }


    /**
     * 加载用户词典（追加）
     *
     * @param path                  词典路径
     * @param defaultNature         默认词性
     * @param customNatureCollector 收集用户词性
     * @return
     */
    public static boolean load(String path, Nature defaultNature, TreeMap<String, WordAttribute> map, LinkedHashSet<Nature> customNatureCollector) {
        return IOSafeHelper.openAutoCloseableFileReader(path, new LineOperator() {
            @Override
            public void process(String line) throws Exception {
                String splitter = "\\s";
                if (path.endsWith(".csv")) {
                    splitter = ",";
                }

                String[] param = line.split(splitter);
                if (param[0].length() == 0) {
                    return;
                }

                if (HanLpGlobalSettings.Normalization) {
                    // normalize
                    param[0] = CharTable.convert(param[0]);
                }

                int natureCount = (param.length - 1) / 2;
                WordAttribute attribute;
                if (natureCount == 0) {
                    attribute = new WordAttribute(defaultNature);
                }
                else {
                    attribute = new WordAttribute(natureCount);
                    for (int i = 0; i < natureCount; ++i) {
                        attribute.nature[i] = LexiconUtility.convertStringToNature(param[1 + 2 * i], customNatureCollector);
                        attribute.frequency[i] = Integer.parseInt(param[2 + 2 * i]);
                        attribute.totalFrequency += attribute.frequency[i];
                    }
                }
                map.put(param[0], attribute);
            }
        });
    }

    /**
     * 如果已经存在该词条,直接更新该词条的属性
     *
     * @param key       词语
     * @param attribute 词语的属性
     * @param map       加载期间的map
     * @return 是否更新了
     */

    private static boolean updateAttributeIfExist(String key, WordAttribute attribute, TreeMap<String, WordAttribute> map, TreeMap<Integer, WordAttribute> rewriteTable) {
        int wordID = CoreDictionary.INSTANCE.getWordID(key);
        WordAttribute attributeExisted;
        if (wordID != -1) {
            attributeExisted = CoreDictionary.INSTANCE.get(wordID);
            attributeExisted.nature = attribute.nature;
            attributeExisted.frequency = attribute.frequency;
            attributeExisted.totalFrequency = attribute.totalFrequency;
            // 收集该覆写
            rewriteTable.put(wordID, attribute);
            return true;
        }

        attributeExisted = map.get(key);
        if (attributeExisted != null) {
            attributeExisted.nature = attribute.nature;
            attributeExisted.frequency = attribute.frequency;
            attributeExisted.totalFrequency = attribute.totalFrequency;
            return true;
        }

        return false;
    }

    /**
     * 往自定义词典中插入一个新词（非覆盖模式）<br>
     * 动态增删不会持久化到词典文件
     *
     * @param word                新词 如“裸婚”
     * @param natureWithFrequency 词性和其对应的频次，比如“nz 1 v 2”，null时表示“nz 1”
     * @return 是否插入成功（失败的原因可能是不覆盖、natureWithFrequency有问题等，后者可以通过调试模式了解原因）
     */
    public static boolean add(String word, String natureWithFrequency) {
        if (contains(word)) return false;
        return insert(word, natureWithFrequency);
    }

    /**
     * 往自定义词典中插入一个新词（非覆盖模式）<br>
     * 动态增删不会持久化到词典文件
     *
     * @param word 新词 如“裸婚”
     * @return 是否插入成功（失败的原因可能是不覆盖等，可以通过调试模式了解原因）
     */
    public static boolean add(String word) {
        if (HanLpGlobalSettings.Normalization) word = CharTable.convert(word);
        if (contains(word)) return false;
        return insert(word, null);
    }

    /**
     * 往自定义词典中插入一个新词（覆盖模式）<br>
     * 动态增删不会持久化到词典文件
     *
     * @param word                新词 如“裸婚”
     * @param natureWithFrequency 词性和其对应的频次，比如“nz 1 v 2”，null时表示“nz 1”。
     * @return 是否插入成功（失败的原因可能是natureWithFrequency问题，可以通过调试模式了解原因）
     */
    public static boolean insert(String word, String natureWithFrequency) {
        if (word == null) {
            return false;
        }

        if (HanLpGlobalSettings.Normalization) {
            word = CharTable.convert(word);
        }

        WordAttribute att = natureWithFrequency == null ? new WordAttribute(Nature.nz, 1) : WordAttribute.create(natureWithFrequency);

        if (att == null) {
            return false;
        }

        if (datTrie.set(word, att)) {
            return true;
        }

        if (trie == null) {
            trie = BinTrie.newBinTrie();
        }
        trie.put(word, att);
        return true;
    }

    /**
     * 以覆盖模式增加新词<br>
     * 动态增删不会持久化到词典文件
     */
    public static boolean insert(String word) {
        return insert(word, null);
    }

    /**
     * 查单词
     */
    public static WordAttribute get(String key) {
        if (HanLpGlobalSettings.Normalization) key = CharTable.convert(key);
        WordAttribute attribute = datTrie.get(key);
        if (attribute != null) return attribute;
        if (trie == null) return null;
        return trie.get(key);
    }

    /**
     * 删除单词<br>
     * 动态增删不会持久化到词典文件
     */
    public static void remove(String key) {
        if (HanLpGlobalSettings.Normalization) key = CharTable.convert(key);
        if (trie == null) return;
        trie.remove(key);
    }

    /**
     * 前缀查询
     */
    public static LinkedList<Map.Entry<String, WordAttribute>> commonPrefixSearch(String key) {
        return trie.commonPrefixSearchWithValue(key);
    }

    /**
     * 前缀查询
     */
    public static LinkedList<Map.Entry<String, WordAttribute>> commonPrefixSearch(char[] chars, int begin) {
        return trie.commonPrefixSearchWithValue(chars, begin);
    }

    public static BaseSearcher<WordAttribute> getSearcher(String text) {
        return new Searcher(text);
    }

    @Override
    public String toString() {
        return "CustomDictionary{" +
                "doubleArrayTrie=" + trie +
                '}';
    }

    /**
     * 词典中是否含有词语
     *
     * @param key 词语
     * @return 是否包含
     */
    public static boolean contains(String key) {
        if (datTrie.exactMatchSearch(key) >= 0) {
            return true;
        }
        return trie != null && trie.containsKey(key);
    }

    /**
     * 获取一个BinTrie的查询工具
     *
     * @param charArray 文本
     * @return 查询者
     */
    public static BaseSearcher<WordAttribute> getSearcher(char[] charArray) {
        return new Searcher(charArray);
    }

    static class Searcher extends BaseSearcher<WordAttribute> {
        /**
         * 分词从何处开始，这是一个状态
         */
        int begin;

        private LinkedList<Map.Entry<String, WordAttribute>> entryList;

        protected Searcher(char[] c) {
            super(c);
            entryList = Lists.newLinkedList();
        }

        protected Searcher(String text) {
            super(text);
            entryList = Lists.newLinkedList();
        }

        @Override
        public Map.Entry<String, WordAttribute> next() {
            // 保证首次调用找到一个词语
            while (entryList.size() == 0 && begin < c.length) {
                entryList = trie.commonPrefixSearchWithValue(c, begin);
                ++begin;
            }
            // 之后调用仅在缓存用完的时候调用一次
            if (entryList.size() == 0 && begin < c.length) {
                entryList = trie.commonPrefixSearchWithValue(c, begin);
                ++begin;
            }
            if (entryList.size() == 0) {
                return null;
            }
            Map.Entry<String, WordAttribute> result = entryList.getFirst();
            entryList.removeFirst();
            offset = begin - 1;
            return result;
        }

    }

    /**
     * 获取词典对应的trie树
     *
     * @deprecated 谨慎操作，有可能废弃此接口
     */
    public static BinTrie<WordAttribute> getTrie() {
        return trie;
    }

    /**
     * 解析一段文本（目前采用了BinTrie+DAT的混合储存形式，此方法可以统一两个数据结构）
     *
     * @param text      文本
     * @param processor 处理器
     */
    public static void parseText(char[] text, AhoCorasickDoubleArrayTrie.IHit<WordAttribute> processor) {
        if (trie != null) {
            BaseSearcher<WordAttribute> searcher = CustomDictionary.getSearcher(text);
            int offset;
            Map.Entry<String, WordAttribute> entry;
            while ((entry = searcher.next()) != null) {
                offset = searcher.getOffset();
                processor.hit(offset, offset + entry.getKey().length(), entry.getValue());
            }
        }
        DoubleArrayTrie<WordAttribute>.Searcher searcher = datTrie.getSearcher(text, 0);
        while (searcher.next()) {
            processor.hit(searcher.begin, searcher.begin + searcher.length, searcher.value);
        }
    }

    /**
     * 解析一段文本（目前采用了BinTrie+DAT的混合储存形式，此方法可以统一两个数据结构）
     *
     * @param text      文本
     * @param processor 处理器
     */
    public static void parseText(String text, AhoCorasickDoubleArrayTrie.IHit<WordAttribute> processor) {
        if (trie != null) {
            BaseSearcher<WordAttribute> searcher = CustomDictionary.getSearcher(text);
            int offset;
            Map.Entry<String, WordAttribute> entry;
            while ((entry = searcher.next()) != null) {
                offset = searcher.getOffset();
                processor.hit(offset, offset + entry.getKey().length(), entry.getValue());
            }
        }
        DoubleArrayTrie<WordAttribute>.Searcher searcher = datTrie.getSearcher(text, 0);
        while (searcher.next()) {
            processor.hit(searcher.begin, searcher.begin + searcher.length, searcher.value);
        }
    }
}
