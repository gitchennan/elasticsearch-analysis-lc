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
import com.hankcs.hanlp.dictionary.searcher.DoubleArrayTrieSearcher;
import com.hankcs.hanlp.io.IOSafeHelper;
import com.hankcs.hanlp.io.InputStreamOperator;
import com.hankcs.hanlp.log.HanLpLogger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
                    String.format("Load dictionary[%s], takes %sms, path[%s] ",
                            "JapanesePersonDictionary", stopwatch.elapsed(TimeUnit.MILLISECONDS), HanLpGlobalSettings.JapanesePersonDictionaryPath));
        }
        stopwatch.stop();
    }

    static boolean load() {
        trie = DoubleArrayTrie.newDoubleArrayTrie();
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
        if (!trie.containsKey(key)) {
            return false;
        }
        return key.length() >= length;
    }

    public static Character get(String key) {
        return trie.getValue(key);
    }

    public static BaseSearcher<Character> getSearcher(char[] charArray) {
        return new DoubleArrayTrieSearcher<Character>(String.valueOf(charArray), trie);
    }

}
