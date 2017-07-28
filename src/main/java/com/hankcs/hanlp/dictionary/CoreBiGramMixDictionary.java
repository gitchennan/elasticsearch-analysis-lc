///*
// * <summary></summary>
// * <author>He Han</author>
// * <email>hankcs.cn@gmail.com</email>
// * <create-date>2014/12/24 12:46</create-date>
// *
// * <copyright file="CoreBiGramDictionary.java" company="上海林原信息科技有限公司">
// * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
// * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to getValue more information.
// * </copyright>
// */
//package com.hankcs.hanlp.dictionary;
//
//import com.google.common.collect.Maps;
//import com.hankcs.hanlp.api.HanLpGlobalSettings;
//import com.hankcs.hanlp.collection.trie.DoubleArrayTrie;
//import com.hankcs.hanlp.io.IOSafeHelper;
//import com.hankcs.hanlp.io.InputStreamOperator;
//import com.hankcs.hanlp.log.HanLpLogger;
//import com.hankcs.hanlp.utility.ByteUtil;
//
//import java.io.BufferedReader;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.TreeMap;
//
//
///**
// * 核心词典的二元接续词典，混合采用词ID和词本身储存
// *
// * @author hankcs
// */
//public class CoreBiGramMixDictionary {
//    static DoubleArrayTrie<Integer> trie;
//    public final static String path = HanLpGlobalSettings.BiGramDictionaryPath;
//
//    static {
//        HanLpLogger.info(CoreBiGramMixDictionary.class, "开始加载二元词典" + path + ".mix");
//        long start = System.currentTimeMillis();
//        if (!load(path)) {
//            HanLpLogger.error(CoreBiGramMixDictionary.class, "二元词典加载失败");
//        }
//        else {
//            HanLpLogger.info(CoreBiGramMixDictionary.class, path + ".mix" + "加载成功，耗时" + (System.currentTimeMillis() - start) + "ms");
//        }
//    }
//
//    static boolean load(String path) {
//        trie = DoubleArrayTrie.newDoubleArrayTrie();
//        TreeMap<String, Integer> map = Maps.newTreeMap();
//
//        return IOSafeHelper.openAutoCloseableFileInputStream(path, new InputStreamOperator() {
//            @Override
//            public void process(InputStream input) throws Exception {
//                BufferedReader br = new BufferedReader(new InputStreamReader(input, "UTF-8"));
//                String line;
//                StringBuilder sb = new StringBuilder();
//                while ((line = br.readLine()) != null) {
//                    String[] params = line.split("\\s");
//                    String[] twoWord = params[0].split("@", 2);
//                    buildID(twoWord[0], sb);
//                    sb.append('@');
//                    buildID(twoWord[1], sb);
//                    int freq = Integer.parseInt(params[1]);
//                    map.put(sb.toString(), freq);
//                    sb.setLength(0);
//                }
//                trie.build(map);
//            }
//        });
//    }
//
//    /**
//     * 二分搜索
//     */
//    static int binarySearch(int[][] a, int key) {
//        int low = 0;
//        int high = a.length - 1;
//
//        while (low <= high) {
//            int mid = (low + high) >>> 1;
//            int midVal = a[mid][0];
//
//            if (midVal < key)
//                low = mid + 1;
//            else if (midVal > key)
//                high = mid - 1;
//            else
//                return mid; // key found
//        }
//        return -(low + 1);  // key not found.
//    }
//
//
//    static void buildID(String word, StringBuilder sbStorage) {
//        int id = CoreDictionary.INSTANCE.getWordID(word);
//        if (id == -1) {
//            sbStorage.append(word);
//        }
//        else {
//            char[] twoChar = ByteUtil.convertIntToTwoChar(id);
//            sbStorage.append(twoChar);
//        }
//    }
//
//    /**
//     * 获取词语的ID
//     */
//    public static int getWordID(String a) {
//        return CoreDictionary.INSTANCE.getWordID(a);
//    }
//}
