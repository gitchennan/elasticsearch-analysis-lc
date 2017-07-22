/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/12/24 12:46</create-date>
 *
 * <copyright file="CoreBiGramDictionary.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package com.hankcs.hanlp.dictionary;

import com.hankcs.hanlp.api.HanLpGlobalSettings;
import com.hankcs.hanlp.io.IOSafeHelper;
import com.hankcs.hanlp.io.InputStreamOperator;
import com.hankcs.hanlp.log.HanLpLogger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;

/**
 * 核心词典的二元接续词典，采用整型储存，高性能
 *
 * @author hankcs
 */
public class CoreBiGramTableDictionary {
    /**
     * 描述了词在pair中的范围，具体说来<br>
     * 给定一个词idA，从pair[start[idA]]开始的start[idA + 1] - start[idA]描述了一些接续的频次
     */
    static int start[];
    /**
     * pair[偶数n]表示key，pair[n+1]表示frequency
     */
    static int pair[];

    public final static String path = HanLpGlobalSettings.BiGramDictionaryPath;

    static {
        HanLpLogger.info(CoreBiGramTableDictionary.class, "开始加载二元词典" + path + ".table");
        long start = System.currentTimeMillis();
        if (!load(path)) {
            HanLpLogger.error(CoreBiGramTableDictionary.class, "二元词典加载失败");
            System.exit(-1);
        }
        else {
            HanLpLogger.info(CoreBiGramTableDictionary.class, path + ".table" + "加载成功，耗时" + (System.currentTimeMillis() - start) + "ms");
        }
    }

    static boolean load(String path) {
        BufferedReader br;
        TreeMap<Integer, TreeMap<Integer, Integer>> map = new TreeMap<Integer, TreeMap<Integer, Integer>>();
        int maxWordId = CoreDictionary.INSTANCE.getMaxWordID();

        boolean readResult = IOSafeHelper.openAutoCloseableFileInputStream(path, new InputStreamOperator() {
            @Override
            public void process(InputStream input) throws Exception {
                BufferedReader br = new BufferedReader(new InputStreamReader(input, "UTF-8"));
                String line;
                int total = 0;
                while ((line = br.readLine()) != null) {
                    String[] params = line.split("\\s");
                    String[] twoWord = params[0].split("@", 2);
                    String wordA = twoWord[0];

                    int idA = CoreDictionary.INSTANCE.getWordID(wordA);
                    if (idA == -1) {
                        continue;
                    }

                    String wordB = twoWord[1];
                    int idB = CoreDictionary.INSTANCE.getWordID(wordB);
                    if (idB == -1) {
                        continue;
                    }

                    int freq = Integer.parseInt(params[1]);
                    TreeMap<Integer, Integer> biMap = map.get(idA);
                    if (biMap == null) {
                        biMap = new TreeMap<Integer, Integer>();
                        map.put(idA, biMap);
                    }

                    biMap.put(idB, freq);
                    total += 2;
                }
                start = new int[maxWordId + 1];
                // total是接续的个数*2
                pair = new int[total];
            }
        });

        if (!readResult) {
            return false;
        }

        int offset = 0;
        for (int i = 0; i < maxWordId; ++i) {
            TreeMap<Integer, Integer> bMap = map.get(i);
            if (bMap != null) {
                for (Map.Entry<Integer, Integer> entry : bMap.entrySet()) {
                    int index = offset << 1;
                    pair[index] = entry.getKey();
                    pair[index + 1] = entry.getValue();
                    ++offset;
                }
            }
            start[i + 1] = offset;
        }
        return true;
    }

    /**
     * 二分搜索，由于二元接续前一个词固定时，后一个词比较少，所以二分也能取得很高的性能
     *
     * @param a         目标数组
     * @param fromIndex 开始下标
     * @param length    长度
     * @param key       词的id
     * @return 共现频次
     */
    private static int binarySearch(int[] a, int fromIndex, int length, int key) {
        int low = fromIndex;
        int high = fromIndex + length - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midVal = a[mid << 1];

            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found.
    }

    /**
     * 获取共现频次
     *
     * @param wordA 第一个词
     * @param wordB 第二个词
     * @return 第一个词@第二个词出现的频次
     */
    public static int getBiFrequency(String wordA, String wordB) {
        int idA = CoreDictionary.INSTANCE.getWordID(wordA);
        if (idA == -1) {
            return 0;
        }
        int idB = CoreDictionary.INSTANCE.getWordID(wordB);
        if (idB == -1) {
            return 0;
        }
        int index = binarySearch(pair, start[idA], start[idA + 1] - start[idA], idB);
        if (index < 0) return 0;
        index <<= 1;
        return pair[index + 1];
    }

    /**
     * 获取共现频次
     *
     * @param idA 第一个词的id
     * @param idB 第二个词的id
     * @return 共现频次
     */
    public static int getBiFrequency(int idA, int idB) {
        if (idA == -1 || idB == -1) {
            // -1表示用户词典，返回正值增加其亲和度
            return 1000;
        }
        int index = binarySearch(pair, start[idA], start[idA + 1] - start[idA], idB);
        if (index < 0) return 0;
        index <<= 1;
        return pair[index + 1];
    }

    /**
     * 获取词语的ID
     *
     * @param word 词语
     * @return id
     */
    public static int getWordID(String word) {
        return CoreDictionary.INSTANCE.getWordID(word);
    }
}
