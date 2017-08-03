/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/10/17 19:02</create-date>
 *
 * <copyright file="HanLP.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package com.hankcs.hanlp.api;

import com.hankcs.hanlp.dictionary.py.Pinyin;
import com.hankcs.hanlp.dictionary.py.PinyinDictionary;
import com.hankcs.hanlp.dictionary.ts.*;
import com.hankcs.hanlp.log.HanLpLogger;
import com.hankcs.hanlp.seg.Dijkstra.DijkstraSegment;
import com.hankcs.hanlp.seg.NShort.NShortSegment;
import com.hankcs.hanlp.seg.Other.DoubleArrayTrieSegment;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.Viterbi.ViterbiSegment;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.summary.TextRankKeyword;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;

import java.util.List;

/**
 * HanLP: Han Language Processing <br>
 * 汉语言处理包 <br>
 * 常用接口工具类
 *
 * @author hankcs
 */
public class HanLP {

    private HanLP() {

    }

    /**
     * 分词
     *
     * @param text 文本
     * @return 切分后的单词
     */
    public static List<Term> segment(String text) {
        return StandardTokenizer.segment(text.toCharArray());
    }

    /**
     * 创建一个分词器<br>
     * 这是一个工厂方法<br>
     * 与直接new一个分词器相比，使用本方法的好处是，以后HanLP升级了，总能用上最合适的分词器
     *
     * @return 一个分词器
     */
    public static Segment newViterbiSegment() {
        return new ViterbiSegment();
    }

    public static Segment newNShortSegment() {
        return new NShortSegment();
    }

    public static Segment newDijkstraSegment() {
        return new DijkstraSegment();
    }

    public static Segment newDoubleArrayTrieSegment() {
        return new DoubleArrayTrieSegment();
    }

    /**
     * 繁转简
     *
     * @param traditionalChineseString 繁体中文
     * @return 简体中文
     */
    public static String convertToSimplifiedChinese(String traditionalChineseString) {
        return TraditionalChineseDictionary.convertToSimplifiedChinese(traditionalChineseString.toCharArray());
    }

    /**
     * 简转繁
     *
     * @param simplifiedChineseString 简体中文
     * @return 繁体中文
     */
    public static String convertToTraditionalChinese(String simplifiedChineseString) {
        return SimplifiedChineseDictionary.convertToTraditionalChinese(simplifiedChineseString.toCharArray());
    }

    /**
     * 简转繁,是{@link HanLP#convertToTraditionalChinese(java.lang.String)}的简称
     *
     * @param s 简体中文
     * @return 繁体中文(大陆标准)
     */
    public static String s2t(String s) {
        return HanLP.convertToTraditionalChinese(s);
    }

    /**
     * 繁转简,是{@link HanLP#convertToSimplifiedChinese(String)}的简称
     *
     * @param t 繁体中文(大陆标准)
     * @return 简体中文
     */
    public static String t2s(String t) {
        return HanLP.convertToSimplifiedChinese(t);
    }

    /**
     * 簡體到臺灣正體
     *
     * @param s 簡體
     * @return 臺灣正體
     */
    public static String s2tw(String s) {
        return SimplifiedToTaiwanChineseDictionary.convertToTraditionalTaiwanChinese(s);
    }

    /**
     * 臺灣正體到簡體
     *
     * @param tw 臺灣正體
     * @return 簡體
     */
    public static String tw2s(String tw) {
        return TaiwanToSimplifiedChineseDictionary.convertToSimplifiedChinese(tw);
    }

    /**
     * 簡體到香港繁體
     *
     * @param s 簡體
     * @return 香港繁體
     */
    public static String s2hk(String s) {
        return SimplifiedToHongKongChineseDictionary.convertToTraditionalHongKongChinese(s);
    }

    /**
     * 香港繁體到簡體
     *
     * @param hk 香港繁體
     * @return 簡體
     */
    public static String hk2s(String hk) {
        return HongKongToSimplifiedChineseDictionary.convertToSimplifiedChinese(hk);
    }

    /**
     * 繁體到臺灣正體
     *
     * @param t 繁體
     * @return 臺灣正體
     */
    public static String t2tw(String t) {
        return TraditionalToTaiwanChineseDictionary.convertToTaiwanChinese(t);
    }

    /**
     * 臺灣正體到繁體
     *
     * @param tw 臺灣正體
     * @return 繁體
     */
    public static String tw2t(String tw) {
        return TaiwanToTraditionalChineseDictionary.convertToTraditionalChinese(tw);
    }

    /**
     * 繁體到香港繁體
     *
     * @param t 繁體
     * @return 香港繁體
     */
    public static String t2hk(String t) {
        return TraditionalToHongKongChineseDictionary.convertToHongKongTraditionalChinese(t);
    }

    /**
     * 香港繁體到繁體
     *
     * @param hk 香港繁體
     * @return 繁體
     */
    public static String hk2t(String hk) {
        return HongKongToTraditionalChineseDictionary.convertToTraditionalChinese(hk);
    }

    /**
     * 香港繁體到臺灣正體
     *
     * @param hk 香港繁體
     * @return 臺灣正體
     */
    public static String hk2tw(String hk) {
        return HongKongToTaiwanChineseDictionary.convertToTraditionalTaiwanChinese(hk);
    }

    /**
     * 臺灣正體到香港繁體
     *
     * @param tw 臺灣正體
     * @return 香港繁體
     */
    public static String tw2hk(String tw) {
        return TaiwanToHongKongChineseDictionary.convertToTraditionalHongKongChinese(tw);
    }

    /**
     * 转化为拼音
     *
     * @param text       文本
     * @param separator  分隔符
     * @param remainNone 有些字没有拼音（如标点），是否保留它们的拼音（true用none表示，false用原字符表示）
     * @return 一个字符串，由[拼音][分隔符][拼音]构成
     */
    public static String convertToPinyinString(String text, String separator, boolean remainNone) {
        List<Pinyin> pinyinList = PinyinDictionary.INSTANCE.convertToPinyin(text, true);
        int length = pinyinList.size();
        StringBuilder sb = new StringBuilder(length * (5 + separator.length()));
        int i = 1;
        for (Pinyin pinyin : pinyinList) {

            if (pinyin == Pinyin.none5 && !remainNone) {
                sb.append(text.charAt(i - 1));
            }
            else sb.append(pinyin.getPinyinWithoutTone());
            if (i < length) {
                sb.append(separator);
            }
            ++i;
        }
        return sb.toString();
    }

    /**
     * 转化为拼音
     *
     * @param text 待解析的文本
     * @return 一个拼音列表
     */
    public static List<Pinyin> convertToPinyinList(String text) {
        return PinyinDictionary.INSTANCE.convertToPinyin(text);
    }

    /**
     * 转化为拼音（首字母）
     *
     * @param text       文本
     * @param separator  分隔符
     * @param remainNone 有些字没有拼音（如标点），是否保留它们（用none表示）
     * @return 一个字符串，由[首字母][分隔符][首字母]构成
     */
    public static String convertToPinyinFirstCharString(String text, String separator, boolean remainNone) {
        List<Pinyin> pinyinList = PinyinDictionary.INSTANCE.convertToPinyin(text, remainNone);
        int length = pinyinList.size();
        StringBuilder sb = new StringBuilder(length * (1 + separator.length()));
        int i = 1;
        for (Pinyin pinyin : pinyinList) {
            sb.append(pinyin.getFirstChar());
            if (i < length) {
                sb.append(separator);
            }
            ++i;
        }
        return sb.toString();
    }

    /**
     * 提取关键词
     *
     * @param document 文档内容
     * @param size     希望提取几个关键词
     * @return 一个列表
     */
    public static List<String> extractKeyword(String document, int size) {
        return TextRankKeyword.getKeywordList(document, size);
    }
}
