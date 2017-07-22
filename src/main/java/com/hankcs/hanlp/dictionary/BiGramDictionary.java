/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/05/2014/5/16 20:55</create-date>
 *
 * <copyright file="BiGramDictionary.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package com.hankcs.hanlp.dictionary;

import com.google.common.collect.Maps;
import com.hankcs.hanlp.api.HanLpGlobalSettings;
import com.hankcs.hanlp.collection.trie.DoubleArrayTrie;
import com.hankcs.hanlp.io.IOSafeHelper;
import com.hankcs.hanlp.io.LineOperator;
import com.hankcs.hanlp.log.HanLpLogger;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;


/**
 * 2元语法词典
 *
 * @author hankcs
 * @deprecated 现在基于DoubleArrayTrie的BiGramDictionary已经由CoreBiGramTableDictionary替代，可以显著降低内存
 */
public class BiGramDictionary {
    static DoubleArrayTrie<Integer> trie;

    public final static String path = HanLpGlobalSettings.BiGramDictionaryPath;
    public static final int totalFrequency = 37545990;

    // 自动加载词典
    static {
        long start = System.currentTimeMillis();
        if (!load(path)) {
            HanLpLogger.info(BiGramDictionary.class, "二元词典加载失败");
        }
        else {
            HanLpLogger.info(BiGramDictionary.class, path + "加载成功，耗时" + (System.currentTimeMillis() - start) + "ms");
        }
    }

    public static boolean load(String path) {
        HanLpLogger.info(BiGramDictionary.class, "二元词典开始加载:" + path);
        trie = new DoubleArrayTrie<Integer>();
        TreeMap<String, Integer> map = Maps.newTreeMap();

        return IOSafeHelper.openAutoCloseableFileReader(path, new LineOperator() {
            @Override
            public void process(String line) throws Exception {
                String[] params = line.split("\\s");
                String twoWord = params[0];
                int freq = Integer.parseInt(params[1]);
                map.put(twoWord, freq);

                trie.build(map);
            }
        });
    }

    /**
     * 找寻特殊字串，如未##串
     *
     * @return 一个包含特殊词串的set
     * @deprecated 没事就不要用了
     */
    public static Set<String> _findSpecialString() {
        Set<String> stringSet = new HashSet<String>();

        IOSafeHelper.openAutoCloseableFileReader(path, new LineOperator() {
            @Override
            public void process(String line) throws Exception {
                String[] params = line.split("\t");
                String twoWord = params[0];
                params = twoWord.split("@");
                for (String w : params) {
                    if (w.contains("##")) {
                        stringSet.add(w);
                    }
                }
            }
        });

        return stringSet;
    }

    /**
     * 获取共现频次
     *
     * @param from 第一个词
     * @param to   第二个词
     * @return 第一个词@第二个词出现的频次
     */
    public static int getBiFrequency(String from, String to) {
        return getBiFrequency(from + '@' + to);
    }

    /**
     * 获取共现频次
     *
     * @param twoWord 用@隔开的两个词
     * @return 共现频次
     */
    public static int getBiFrequency(String twoWord) {
        Integer result = trie.get(twoWord);
        return (result == null ? 0 : result);
    }

//    /**
//     * 将NGram词典重新写回去
//     *
//     * @param map
//     * @param path
//     * @return
//     */
//    private static boolean reSaveDictionary(TreeMap<String, Integer> map, String path)
//    {
//        StringBuilder sbOut = new StringBuilder();
//        for (Map.Entry<String, Integer> entry : map.entrySet())
//        {
//            sbOut.append(entry.getKey());
//            sbOut.append(' ');
//            sbOut.append(entry.getValue());
//            sbOut.append('\n');
//        }
//
//        return IOUtil.saveTxt(path, sbOut.toString());
//    }

//    /**
//     * 接受键数组与值数组，排序以供建立trie树
//     *
//     * @param wordList
//     * @param freqList
//     */
//    private static void sortListForBuildTrie(List<String> wordList, List<Integer> freqList, String path)
//    {
//        BinTrie<Integer> binTrie = new BinTrie<Integer>();
//        for (int i = 0; i < wordList.size(); ++i)
//        {
//            binTrie.put(wordList.get(i), freqList.get(i));
//        }
//        Collections.sort(wordList);
//        try
//        {
//            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(IOUtil.newOutputStream(path)));
////            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + "_sort.txt")));
//            for (String w : wordList)
//            {
//                bw.write(w + '\t' + binTrie.get(w));
//                bw.newLine();
//            }
//            bw.close();
//        }
//        catch (FileNotFoundException e)
//        {
//            e.printStackTrace();
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//    }
}
