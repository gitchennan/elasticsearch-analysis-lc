/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/9/9 22:30</create-date>
 *
 * <copyright file="CommonDictioanry.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package com.hankcs.hanlp.corpus.dictionary;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import com.hankcs.hanlp.collection.trie.bintrie.BinTrie;
import com.hankcs.hanlp.io.IOSafeHelper;
import com.hankcs.hanlp.io.LineOperator;
import com.hankcs.hanlp.log.HanLpLogger;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

/**
 * 可以调整大小的词典
 *
 * @author hankcs
 */
public abstract class SimpleDictionary<V> {

    BinTrie<V> trie = BinTrie.newBinTrie();

    public boolean load(String... pathList) {
        for (String path : pathList) {
            Stopwatch stopwatch = Stopwatch.createStarted();
            IOSafeHelper.openAutoCloseableFileReader(path, new LineOperator() {
                @Override
                public void process(String line) throws Exception {
                    Map.Entry<String, V> entry = onGenerateEntry(line);
                    if (entry == null) {
                        return;
                    }
                    trie.put(entry.getKey(), entry.getValue());
                }
            });

            HanLpLogger.info(SimpleDictionary.class,
                    String.format("Load dictionary[%s], takes %s ms, path[%s] ",
                            "PinyinDictionary", stopwatch.elapsed(TimeUnit.MILLISECONDS), path));
        }
        return true;
    }

    /**
     * 查询一个单词
     *
     * @return 单词对应的条目
     */
    public V get(String key) {
        return trie.getValue(key);
    }

    /**
     * 由参数构造一个词条
     */
    protected abstract Map.Entry<String, V> onGenerateEntry(String line);

    /**
     * 获取键值对集合
     */
    public Set<Map.Entry<String, V>> entrySet() {
        return trie.entrySet();
    }

    /**
     * 键集合
     */
    public Set<String> keySet() {
        TreeSet<String> keySet = Sets.newTreeSet();

        for (Map.Entry<String, V> entry : entrySet()) {
            keySet.add(entry.getKey());
        }

        return keySet;
    }

    /**
     * 过滤部分词条
     *
     * @param filter 过滤器
     * @return 删除了多少条
     */
    public int remove(Filter<V> filter) {
        int size = trie.size();
        for (Map.Entry<String, V> entry : entrySet()) {
            if (filter.remove(entry)) {
                trie.remove(entry.getKey());
            }
        }

        return size - trie.size();
    }

    public interface Filter<V> {
        boolean remove(Map.Entry<String, V> entry);
    }

    /**
     * 向中加入单词
     */
    public void add(String key, V value) {
        trie.put(key, value);
    }

    public int size() {
        return trie.size();
    }
}
