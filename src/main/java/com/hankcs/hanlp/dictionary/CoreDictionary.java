/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/12/23 20:07</create-date>
 *
 * <copyright file="CoreDictionaryACDAT.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package com.hankcs.hanlp.dictionary;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.hankcs.hanlp.api.HanLpGlobalSettings;
import com.hankcs.hanlp.collection.trie.DoubleArrayTrie;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.log.HanLpLogger;

import java.util.TreeMap;
import java.util.concurrent.TimeUnit;


/**
 * 使用DoubleArrayTrie实现的核心词典
 *
 * @author hankcs, chennan
 */
public class CoreDictionary extends FileSystemDictionary {
    /**
     * core dictionary's name
     */
    public static final String DICTIONARY_NAME = "CoreDictionary";

    public static final int TOTAL_FREQUENCY = 221894;
    /**
     * a data structure double array trie for store the dictionary items
     */
    private volatile DoubleArrayTrie<WordAttribute> doubleArrayTrie;

    /**
     * attribute map for preLoad dictionary
     */
    private TreeMap<String, WordAttribute> wordAttributeMap;

    /**
     * core dictionary singleton
     */
    public static final CoreDictionary INSTANCE;

    static {
        INSTANCE = new CoreDictionary();
        INSTANCE.load();
    }

    @Override
    public String dictionaryName() {
        return CoreDictionary.DICTIONARY_NAME;
    }

    private CoreDictionary() {
        super(HanLpGlobalSettings.CoreDictionaryPath);
        doubleArrayTrie = new DoubleArrayTrie<WordAttribute>();
        wordAttributeMap = Maps.newTreeMap();
    }

    @Override
    void loadLine(String line) {
        String param[] = line.split("\\s");
        int natureCount = (param.length - 1) / 2;
        WordAttribute wordAttribute = new WordAttribute(natureCount);
        for (int i = 0; i < natureCount; i++) {
            wordAttribute.nature[i] = Enum.valueOf(Nature.class, param[1 + 2 * i]);
            wordAttribute.frequency[i] = Integer.parseInt(param[2 + 2 * i]);
            wordAttribute.totalFrequency += wordAttribute.frequency[i];
        }
        wordAttributeMap.put(param[0], wordAttribute);
    }

    @Override
    void onDictionaryLoaded() {
        if (!wordAttributeMap.isEmpty()) {
            Stopwatch stopwatch = Stopwatch.createStarted();
            int buildResult = doubleArrayTrie.build(wordAttributeMap);
            stopwatch.stop();
            if (buildResult == 0) {
                HanLpLogger.info(this,
                        String.format("Build doubleArrayTrie dictionary, takes %sms build_result[%s]",
                                stopwatch.elapsed(TimeUnit.MILLISECONDS), buildResult));
            }
            else {
                HanLpLogger.error(this,
                        String.format("Error occurred while building doubleArrayTrie dictionary, build_result[%s]", buildResult));
            }
        }
        wordAttributeMap.clear();
    }

    @Override
    public synchronized void releaseResource() {
        doubleArrayTrie = new DoubleArrayTrie<WordAttribute>();

        if (wordAttributeMap == null) {
            wordAttributeMap = Maps.newTreeMap();
        }
        wordAttributeMap.clear();

        HanLpLogger.info(this, "Release dictionary resource");
    }

    /**
     * 获取条目
     */
    public WordAttribute get(String key) {
        return doubleArrayTrie.get(key);
    }

    /**
     * 获取条目
     */
    public WordAttribute get(int wordID) {
        return doubleArrayTrie.get(wordID);
    }

    /**
     * 获取词频
     */
    public int getTermFrequency(String term) {
        WordAttribute attribute = get(term);
        if (attribute == null) return 0;
        return attribute.totalFrequency;
    }

    /**
     * 是否包含词语
     */
    public boolean contains(String key) {
        return doubleArrayTrie.get(key) != null;
    }

    /**
     * 获取词语的ID
     *
     * @param word 词语
     * @return ID, 如果不存在, 则返回-1
     */
    public int getWordID(String word) {
        return doubleArrayTrie.exactMatchSearch(word);
    }

    /**
     * get max word id in dictionary
     *
     * @return max word id
     */
    public int getMaxWordID() {
        return doubleArrayTrie.size();
    }

    public DoubleArrayTrie<WordAttribute>.Searcher getSearcher(String text, int offset) {
        return doubleArrayTrie.getSearcher(text, offset);
    }

    public DoubleArrayTrie<WordAttribute>.Searcher getSearcher(char[] text, int offset) {
        return doubleArrayTrie.getSearcher(text, offset);
    }

    public boolean updateWordAttribute(String key, WordAttribute wordAttribute) {
        return doubleArrayTrie.set(key, wordAttribute);
    }
}
