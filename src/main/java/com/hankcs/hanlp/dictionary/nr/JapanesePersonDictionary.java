/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/11/12 20:17</create-date>
 *
 * <copyright file="JapanesePersonDictionary.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package com.hankcs.hanlp.dictionary.nr;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.hankcs.hanlp.api.HanLpGlobalSettings;
import com.hankcs.hanlp.collection.trie.DoubleArrayTrie;
import com.hankcs.hanlp.dictionary.BaseSearcher;
import com.hankcs.hanlp.io.IOSafeHelper;
import com.hankcs.hanlp.io.InputStreamOperator;
import com.hankcs.hanlp.log.HanLpLogger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;


/**
 * @author hankcs
 */
public class JapanesePersonDictionary {
    static String path = HanLpGlobalSettings.JapanesePersonDictionaryPath;
    static DoubleArrayTrie<Character> trie;
    /**
     * 姓
     */
    public static final char X = 'x';
    /**
     * 名
     */
    public static final char M = 'm';
    /**
     * bad case
     */
    public static final char A = 'A';

    static {
        Stopwatch stopwatch = Stopwatch.createStarted();
        if (!load()) {
            HanLpLogger.error(JapanesePersonDictionary.class,
                    String.format("Load dictionary[%s], takes %sms, path[%s] ",
                            "JapanesePersonDictionary", stopwatch.elapsed(TimeUnit.MILLISECONDS), HanLpGlobalSettings.JapanesePersonDictionaryPath));
        }
        else {
            HanLpLogger.info(JapanesePersonDictionary.class,
                    String.format("Load dictionary[%-25s], takes %sms, path[%s] ",
                            "JapanesePersonDictionary", stopwatch.elapsed(TimeUnit.MILLISECONDS), HanLpGlobalSettings.JapanesePersonDictionaryPath));
        }
        stopwatch.stop();
    }

    static boolean load() {
        trie = new DoubleArrayTrie<Character>();
        return IOSafeHelper.openAutoCloseableFileInputStream(path, new InputStreamOperator() {
            @Override
            public void process(InputStream input) throws Exception {
                BufferedReader br = new BufferedReader(new InputStreamReader(input, "UTF-8"));
                String line;
                TreeMap<String, Character> map = Maps.newTreeMap();
                while ((line = br.readLine()) != null) {
                    String[] param = line.split(" ", 2);
                    map.put(param[0], param[1].charAt(0));
                }
                trie.build(map);
            }
        });
    }


    /**
     * 是否包含key
     * <p>
     * public static boolean containsKey(String key) {
     * return doubleArrayTrie.containsKey(key);
     * }
     * <p>
     * /**
     * 包含key，且key至少长length
     */
    public static boolean containsKey(String key, int length) {
        if (!trie.containsKey(key)) return false;
        return key.length() >= length;
    }

    public static Character get(String key) {
        return trie.get(key);
    }

    public static BaseSearcher getSearcher(char[] charArray) {
        return new Searcher(charArray, trie);
    }

    /**
     * 最长分词
     */
    public static class Searcher extends BaseSearcher<Character> {
        /**
         * 分词从何处开始，这是一个状态
         */
        int begin;

        DoubleArrayTrie<Character> trie;

        protected Searcher(char[] c, DoubleArrayTrie<Character> trie) {
            super(c);
            this.trie = trie;
        }

        protected Searcher(String text, DoubleArrayTrie<Character> trie) {
            super(text);
            this.trie = trie;
        }

        @Override
        public Map.Entry<String, Character> next() {
            // 保证首次调用找到一个词语
            Map.Entry<String, Character> result = null;
            while (begin < c.length) {
                LinkedList<Map.Entry<String, Character>> entryList = trie.commonPrefixSearchWithValue(c, begin);
                if (entryList.size() == 0) {
                    ++begin;
                }
                else {
                    result = entryList.getLast();
                    offset = begin;
                    begin += result.getKey().length();
                    break;
                }
            }
            if (result == null) {
                return null;
            }
            return result;
        }
    }
}
