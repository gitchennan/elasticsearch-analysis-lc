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
import com.hankcs.hanlp.io.IOSafeHelper;
import com.hankcs.hanlp.io.InputStreamOperator;
import com.hankcs.hanlp.log.HanLpLogger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

        HanLpLogger.info(this.getClass(),
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
        return trie.get(key);
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
        return new Searcher(text);
    }

    /**
     * 前缀搜索，长短都可匹配
     */
    public class Searcher extends BaseSearcher<V> {
        /**
         * 分词从何处开始，这是一个状态
         */
        int begin;

        private List<Map.Entry<String, V>> entryList;

        protected Searcher(char[] c) {
            super(c);
        }

        protected Searcher(String text) {
            super(text);
            entryList = new LinkedList<Map.Entry<String, V>>();
        }

        @Override
        public Map.Entry<String, V> next() {
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
            Map.Entry<String, V> result = entryList.get(0);
            entryList.remove(0);
            offset = begin - 1;
            return result;
        }
    }
}
