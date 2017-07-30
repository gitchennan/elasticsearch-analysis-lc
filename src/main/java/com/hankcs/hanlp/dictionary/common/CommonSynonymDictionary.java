package com.hankcs.hanlp.dictionary.common;

import com.google.common.collect.Maps;
import com.hankcs.hanlp.collection.trie.DoubleArrayTrie;
import com.hankcs.hanlp.corpus.synonym.Synonym;
import com.hankcs.hanlp.corpus.synonym.Synonym.Type;
import com.hankcs.hanlp.corpus.synonym.SynonymHelper;
import com.hankcs.hanlp.log.HanLpLogger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;


/**
 * 一个没有指定资源位置的通用同义词词典
 *
 * @author hankcs
 */
public abstract class CommonSynonymDictionary {

    protected DoubleArrayTrie<SynonymItem> doubleArrayTrie;

//    /**
//     * 词典中最大的语义ID距离
//     */
//    private long maxSynonymItemIdDistance;

    public boolean load(InputStream inputStream) {
        doubleArrayTrie = DoubleArrayTrie.newDoubleArrayTrie();
        TreeMap<String, SynonymItem> treeMap = Maps.newTreeMap();
        String line;
        try {
            BufferedReader bw = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            ArrayList<Synonym> synonymList = null;
            while ((line = bw.readLine()) != null) {
                String[] args = line.split(" ");
                synonymList = Synonym.create(args);
                char type = args[0].charAt(args[0].length() - 1);
                for (Synonym synonym : synonymList) {
                    treeMap.put(synonym.realWord, new SynonymItem(synonym, synonymList, type));
                }
            }

//            // 获取最大语义id
//            if (synonymList != null && synonymList.size() > 0) {
//                maxSynonymItemIdDistance = synonymList.get(synonymList.size() - 1).id - SynonymHelper.convertString2IdWithIndex("Aa01A01", 0) + 1;
//            }

            int resultCode = doubleArrayTrie.build(treeMap);

            if (resultCode != 0) {
                HanLpLogger.error(CommonSynonymDictionary.class,
                        String.format("Failed to build dat, result[%s]", resultCode));
                return false;
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public SynonymItem find(String key) {
        return doubleArrayTrie.getValue(key);
    }

//    /**
//     * 获取最大id
//     *
//     * @return 一个长整型的id
//     */
//    public long getMaxSynonymItemIdDistance() {
//        return maxSynonymItemIdDistance;
//    }
//
//    /**
//     * 语义距离
//     */
//    public long distance(String a, String b) {
//        SynonymItem itemA = find(a);
//        if (itemA == null) {
//            return Long.MAX_VALUE / 3;
//        }
//
//        SynonymItem itemB = find(b);
//        if (itemB == null) {
//            return Long.MAX_VALUE / 3;
//        }
//
//        return itemA.distance(itemB);
//    }

    /**
     * 词典中的一个条目
     */
    public static class SynonymItem {
        /**
         * 条目的key
         */
        public Synonym entry;
        /**
         * 条目的value，是key的同义词列表
         */
        public List<Synonym> synonymList;

        /**
         * 这个条目的类型，同义词或同类词或封闭词
         */
        public Type type;

        public SynonymItem(Synonym entry, List<Synonym> synonymList, Type type) {
            this.entry = entry;
            this.synonymList = synonymList;
            this.type = type;
        }

        public SynonymItem(Synonym entry, List<Synonym> synonymList, char type) {
            this.entry = entry;
            this.synonymList = synonymList;
            switch (type) {
                case '=':
                    this.type = Type.EQUAL;
                    break;
                case '#':
                    this.type = Type.LIKE;
                    break;
                default:
                    this.type = Type.SINGLE;
                    break;
            }
        }

        @Override
        public String toString() {
            return "entry:" + entry.getRealWord() +
                    ", type:" + type +
                    ", synonym:" + synonymList;
        }
//
//        /**
//         * 语义距离
//         */
//        public long distance(SynonymItem other) {
//            return entry.distance(other.entry);
//        }
    }
}
