/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>me@hankcs.com</email>
 * <create-date>2015/7/14 11:01</create-date>
 *
 * <copyright file="WordNatureUtil.java" company="码农场">
 * Copyright (c) 2008-2015, 码农场. All Right Reserved, http://www.hankcs.com/
 * This source is subject to Hankcs. Please contact Hankcs to get more information.
 * </copyright>
 */
package com.hankcs.hanlp.utility;

import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.corpus.util.CustomNatureUtility;
import com.hankcs.hanlp.dictionary.CoreDictionary;
import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.dictionary.WordAttribute;
import com.hankcs.hanlp.seg.common.Term;

import java.util.Arrays;
import java.util.LinkedHashSet;

/**
 * 跟词语与词性有关的工具类，可以全局动态修改HanLP内部词库
 *
 * @author hankcs
 */
public class LexiconUtility {
    /**
     * 从HanLP的词库中提取某个单词的属性（包括核心词典和用户词典）
     *
     * @param word 单词
     * @return 包含词性与频次的信息
     */
    public static WordAttribute getAttribute(String word) {
        WordAttribute attribute = CoreDictionary.INSTANCE.get(word);
        if (attribute != null) {
            return attribute;
        }
        return CustomDictionary.INSTANCE.find(word);
    }

//    /**
//     * 从HanLP的词库中提取某个单词的属性（包括核心词典和用户词典）
//     *
//     * @param term 单词
//     * @return 包含词性与频次的信息
//     */
//    public static WordAttribute getAttribute(Term term) {
//        return getAttribute(term.word);
//    }

    /**
     * 获取某个单词的词频
     */
    public static int getFrequency(String word) {
        WordAttribute attribute = getAttribute(word);
        if (attribute == null) {
            return 0;
        }
        return attribute.totalFrequency;
    }
//
//    /**
//     * 设置某个单词的属性
//     */
//    public static boolean setAttribute(String word, WordAttribute attribute) {
//        if (attribute == null) {
//            return false;
//        }
//
//        if (CoreDictionary.INSTANCE.updateWordAttribute(word, attribute)) {
//            return true;
//        }
//
//        if (CustomDictionary.INSTANCE.updateWordAttribute(word, attribute)) {
//            return true;
//        }
//
//        CustomDictionary.INSTANCE.binTriePut(word, attribute);
//        return true;
//    }

//    /**
//     * 设置某个单词的属性
//     */
//    public static boolean setAttribute(String word, Nature... natures) {
//        if (natures == null) {
//            return false;
//        }
//
//        WordAttribute attribute = new WordAttribute(natures, new int[natures.length]);
//        Arrays.fill(attribute.frequency, 1);
//
//        return setAttribute(word, attribute);
//    }

//    /**
//     * 设置某个单词的属性
//     */
//    public static boolean setAttribute(String word, String... natures) {
//        if (natures == null) {
//            return false;
//        }
//
//        Nature[] natureArray = new Nature[natures.length];
//        for (int i = 0; i < natureArray.length; i++) {
//            natureArray[i] = Nature.create(natures[i]);
//        }
//
//        return setAttribute(word, natureArray);
//    }


//    /**
//     * 设置某个单词的属性
//     */
//    public static boolean setAttribute(String word, String natureWithFrequency) {
//        WordAttribute attribute = WordAttribute.create(natureWithFrequency);
//        return setAttribute(word, attribute);
//    }

    /**
     * 将字符串词性转为Enum词性
     *
     * @param name                  词性名称
     * @param customNatureCollector 一个收集集合
     * @return 转换结果
     */
    public static Nature convertStringToNature(String name, LinkedHashSet<Nature> customNatureCollector) {
        try {
            return Nature.valueOf(name);
        }
        catch (Exception e) {
            Nature nature = CustomNatureUtility.addNature(name);
            if (customNatureCollector != null) {
                customNatureCollector.add(nature);
            }
            return nature;
        }
    }

    /**
     * 将字符串词性转为Enum词性
     *
     * @param name 词性名称
     * @return 转换结果
     */
    public static Nature convertStringToNature(String name) {
        try {
            return Nature.valueOf(name);
        }
        catch (Exception e) {
            return CustomNatureUtility.addNature(name);
        }
    }
}
