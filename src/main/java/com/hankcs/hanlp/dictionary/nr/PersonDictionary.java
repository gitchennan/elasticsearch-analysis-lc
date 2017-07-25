/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/9/10 14:47</create-date>
 *
 * <copyright file="PersonDictionary.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package com.hankcs.hanlp.dictionary.nr;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.hankcs.hanlp.api.HanLpGlobalSettings;
import com.hankcs.hanlp.collection.AhoCorasick.AhoCorasickDoubleArrayTrie;
import com.hankcs.hanlp.corpus.dictionary.item.EnumItem;
import com.hankcs.hanlp.corpus.tag.NR;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.dictionary.TransformMatrixDictionary;
import com.hankcs.hanlp.dictionary.WordAttribute;
import com.hankcs.hanlp.log.HanLpLogger;
import com.hankcs.hanlp.seg.common.Vertex;
import com.hankcs.hanlp.seg.common.WordNet;
import com.hankcs.hanlp.utility.Predefine;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static com.hankcs.hanlp.corpus.tag.NR.B;
import static com.hankcs.hanlp.dictionary.nr.NRConstant.WORD_ID;

/**
 * 人名识别用的词典，实际上是对两个词典的包装
 *
 * @author hankcs
 */
public class PersonDictionary {
    /**
     * 人名词典
     */
    public static NRDictionary dictionary;
    /**
     * 转移矩阵词典
     */
    public static TransformMatrixDictionary<NR> transformMatrixDictionary;
    /**
     * AC算法用到的Trie树
     */
    public static AhoCorasickDoubleArrayTrie<NRPattern> trie;

    public static final WordAttribute ATTRIBUTE = new WordAttribute(Nature.nr, 100);

    static {
        Stopwatch stopwatch = Stopwatch.createStarted();

        dictionary = new NRDictionary();
        if (!dictionary.load(HanLpGlobalSettings.PersonDictionaryPath)) {
            HanLpLogger.error(PersonDictionary.class,
                    String.format("Load dictionary[%s], takes %sms, path[%s] ",
                            "PersonDictionary", stopwatch.elapsed(TimeUnit.MILLISECONDS), HanLpGlobalSettings.PersonDictionaryPath));
        }
        else {
            HanLpLogger.info(PersonDictionary.class,
                    String.format("Load dictionary[%-25s], takes %sms, path[%s] ",
                            "PersonDictionary", stopwatch.elapsed(TimeUnit.MILLISECONDS), HanLpGlobalSettings.PersonDictionaryPath));
        }

        stopwatch.stop().reset().start();

        transformMatrixDictionary = new TransformMatrixDictionary<NR>(NR.class);
        if (transformMatrixDictionary.load(HanLpGlobalSettings.PersonDictionaryTrPath)) {
            HanLpLogger.info(PersonDictionary.class,
                    String.format("Load dictionary[%-25s], takes %sms, path[%s] ",
                            "PersonDictionary.tr", stopwatch.elapsed(TimeUnit.MILLISECONDS), HanLpGlobalSettings.PersonDictionaryTrPath));
        }
        else {
            HanLpLogger.info(PersonDictionary.class,
                    String.format("Load dictionary[%s], takes %sms, path[%s] ",
                            "PersonDictionary.tr", stopwatch.elapsed(TimeUnit.MILLISECONDS), HanLpGlobalSettings.PersonDictionaryTrPath));
        }

        trie = new AhoCorasickDoubleArrayTrie<NRPattern>();
        TreeMap<String, NRPattern> map = Maps.newTreeMap();
        for (NRPattern pattern : NRPattern.values()) {
            map.put(pattern.toString(), pattern);
        }
        trie.build(map);
    }

    /**
     * 模式匹配
     *
     * @param nrList         确定的标注序列
     * @param vertexList     原始的未加角色标注的序列
     * @param wordNetOptimum 待优化的图
     * @param wordNetAll     全词图
     */
    public static void parsePattern(List<NR> nrList, List<Vertex> vertexList, final WordNet wordNetOptimum, final WordNet wordNetAll) {
        // 拆分UV
        ListIterator<Vertex> listIterator = vertexList.listIterator();
        StringBuilder sbPattern = new StringBuilder(nrList.size());
        NR preNR = NR.A;
        boolean backUp = false;
        int index = 0;
        for (NR nr : nrList) {
            ++index;
            Vertex current = listIterator.next();
//            logger.trace("{}/{}", current.realWord, nr);
            switch (nr) {
                case U:
                    if (!backUp) {
                        vertexList = new ArrayList<Vertex>(vertexList);
                        listIterator = vertexList.listIterator(index);
                        backUp = true;
                    }
                    sbPattern.append(NR.K.toString());
                    sbPattern.append(NR.B.toString());
                    preNR = B;
                    listIterator.previous();
                    String nowK = current.realWord.substring(0, current.realWord.length() - 1);
                    String nowB = current.realWord.substring(current.realWord.length() - 1);
                    listIterator.set(new Vertex(nowK));
                    listIterator.next();
                    listIterator.add(new Vertex(nowB));
                    continue;
                case V:
                    if (!backUp) {
                        vertexList = new ArrayList<Vertex>(vertexList);
                        listIterator = vertexList.listIterator(index);
                        backUp = true;
                    }
                    if (preNR == B) {
                        sbPattern.append(NR.E.toString());  //BE
                    }
                    else {
                        sbPattern.append(NR.D.toString());  //CD
                    }
                    sbPattern.append(NR.L.toString());
                    // 对串也做一些修改
                    listIterator.previous();
                    String nowED = current.realWord.substring(current.realWord.length() - 1);
                    String nowL = current.realWord.substring(0, current.realWord.length() - 1);
                    listIterator.set(new Vertex(nowED));
                    listIterator.add(new Vertex(nowL));
                    listIterator.next();
                    continue;
                default:
                    sbPattern.append(nr.toString());
                    break;
            }
            preNR = nr;
        }
        String pattern = sbPattern.toString();
//        logger.trace("模式串：{}", pattern);
//        logger.trace("对应串：{}", vertexList);
//        if (pattern.length() != vertexList.size())
//        {
//            logger.warn("人名识别模式串有bug", pattern, vertexList);
//            return;
//        }
        final Vertex[] wordArray = vertexList.toArray(new Vertex[vertexList.size()]);
        final int[] offsetArray = new int[wordArray.length];
        offsetArray[0] = 0;
        for (int i = 1; i < wordArray.length; ++i) {
            offsetArray[i] = offsetArray[i - 1] + wordArray[i - 1].realWord.length();
        }

        trie.parseText(pattern, new AhoCorasickDoubleArrayTrie.IHit<NRPattern>() {
            @Override
            public void hit(int begin, int end, NRPattern value) {

                StringBuilder sbName = new StringBuilder();
                for (int i = begin; i < end; ++i) {
                    sbName.append(wordArray[i].realWord);
                }
                String name = sbName.toString();

                // 对一些bad case做出调整
                switch (value) {
                    case BCD:
                        if (name.charAt(0) == name.charAt(2)) {
                            return;
                        }
                        break;
                }
                if (isBadCase(name)) return;

                int offset = offsetArray[begin];
                wordNetOptimum.insert(offset, new Vertex(Predefine.TAG_PEOPLE, name, ATTRIBUTE, WORD_ID), wordNetAll);
            }
        });
    }

    /**
     * 因为任何算法都无法解决100%的问题，总是有一些bad case，这些bad case会以“盖公章 A 1”的形式加入词典中<BR>
     * 这个方法返回人名是否是bad case
     *
     * @param name
     * @return
     */
    static boolean isBadCase(String name) {
        EnumItem<NR> nrEnumItem = dictionary.get(name);
        if (nrEnumItem == null) {
            return false;
        }
        return nrEnumItem.containsLabel(NR.A);
    }
}
