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
package com.hankcs.hanlp.dictionary.common;

import com.google.common.base.Stopwatch;
import com.hankcs.hanlp.collection.trie.DoubleArrayTrie;
import com.hankcs.hanlp.dictionary.BaseSearcher;
import com.hankcs.hanlp.dictionary.searcher.CachedDoubleArrayTrieSearcher;
import com.hankcs.hanlp.io.IOSafeHelper;
import com.hankcs.hanlp.io.InputStreamOperator;
import com.hankcs.hanlp.log.HanLpLogger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * 通用的词典，对应固定格式的词典，但是标签可以泛型化
 *
 * @author hankcs
 */
public abstract class CommonDictionary<V> {

    protected DoubleArrayTrie<V> trie;

    public boolean load(String path) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        trie = new DoubleArrayTrie<V>();

        V[] valueArray = doLoadDictionary(path);
        if (valueArray == null) {
            HanLpLogger.error(this.getClass(),
                    String.format("Failed to load values from:[%s], takes %sms",
                            path, stopwatch.elapsed(TimeUnit.MILLISECONDS)));
            return false;
        }

        HanLpLogger.debug(this.getClass(),
                String.format("Load values from:[%s], takes %sms",
                        path, stopwatch.elapsed(TimeUnit.MILLISECONDS)));

        TreeMap<String, V> dictionaryMap = new TreeMap<String, V>();
        IOSafeHelper.openAutoCloseableFileInputStream(path, new InputStreamOperator() {
            @Override
            public void process(InputStream input) throws Exception {
                BufferedReader br = new BufferedReader(new InputStreamReader(input, "UTF-8"));
                String line;
                int i = 0;
                while ((line = br.readLine()) != null) {
                    String[] paramArray = line.split("\\s");
                    dictionaryMap.put(paramArray[0], valueArray[i]);
                    i++;
                }
            }
        });
        int buildTrieResult = trie.build(dictionaryMap);

        if (buildTrieResult != 0) {
            HanLpLogger.error(this.getClass(),
                    String.format("Failed to build DAT, takes %sms", stopwatch.elapsed(TimeUnit.MILLISECONDS)));
        }
        return buildTrieResult == 0;
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
     * 是否含有键
     */
    public boolean contains(String key) {
        return get(key) != null;
    }

    /**
     * 词典大小
     */
    public int size() {
        return trie.size();
    }


    /**
     * 实现此方法来加载值
     */
    protected abstract V[] doLoadDictionary(String path);

    public BaseSearcher getSearcher(String text) {
        return new CachedDoubleArrayTrieSearcher<V>(text, trie);
    }

}
