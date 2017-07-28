/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/10/8 14:07</create-date>
 *
 * <copyright file="TFDictionary.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package com.hankcs.hanlp.corpus.dictionary;

import com.hankcs.hanlp.corpus.occurrence.TermFrequency;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.TreeSet;

/**
 * 词频词典
 *
 * @author hankcs
 */
public class TFDictionary extends SimpleDictionary<TermFrequency> {
    String delimeter;

    public TFDictionary(String delimeter) {
        this.delimeter = delimeter;
    }

    public TFDictionary() {
        this("=");
    }

    @Override
    protected Map.Entry<String, TermFrequency> onGenerateEntry(String line) {
        String[] param = line.split(delimeter);
        return new AbstractMap.SimpleEntry<String, TermFrequency>(param[0], new TermFrequency(param[0], Integer.valueOf(param[1])));
    }

    /**
     * 合并自己（主词典）和某个词频词典
     *
     * @param dictionary 某个词频词典
     * @param limit      如果该词频词典试图引入一个词语，其词频不得超过此limit（如果不需要使用limit功能，可以传入Integer.MAX_VALUE）
     * @param add        设为true则是词频叠加模式，否则是词频覆盖模式
     * @return 词条的增量
     */
    public int combine(TFDictionary dictionary, int limit, boolean add) {
        int preSize = trie.size();
        for (Map.Entry<String, TermFrequency> entry : dictionary.trie.entrySet()) {
            TermFrequency termFrequency = trie.getValue(entry.getKey());
            if (termFrequency == null) {
                trie.put(entry.getKey(), new TermFrequency(entry.getKey(), Math.min(limit, entry.getValue().getValue())));
            }
            else {
                if (add) {
                    termFrequency.setValue(termFrequency.getValue() + Math.min(limit, entry.getValue().getValue()));
                }
            }
        }
        return trie.size() - preSize;
    }

    /**
     * 获取频次
     */
    public int getFrequency(String key) {
        TermFrequency termFrequency = get(key);
        if (termFrequency == null) return 0;
        return termFrequency.getFrequency();
    }

    public void add(String key) {
        TermFrequency termFrequency = trie.getValue(key);
        if (termFrequency == null) {
            termFrequency = new TermFrequency(key);
            trie.put(key, termFrequency);
        }
        else {
            termFrequency.increase();
        }
    }

    /**
     * 按照频率从高到低排序的条目
     */
    public TreeSet<TermFrequency> values() {
        TreeSet<TermFrequency> set = new TreeSet<TermFrequency>(Collections.reverseOrder());

        for (Map.Entry<String, TermFrequency> entry : entrySet()) {
            set.add(entry.getValue());
        }

        return set;
    }
}
