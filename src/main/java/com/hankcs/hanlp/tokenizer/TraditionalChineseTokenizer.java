
package com.hankcs.hanlp.tokenizer;

import com.hankcs.hanlp.api.HanLP;
import com.hankcs.hanlp.dictionary.other.CharTable;
import com.hankcs.hanlp.dictionary.ts.SimplifiedChineseDictionary;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.utility.SentencesUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * 繁体中文分词器
 *
 * @author hankcs
 */
public class TraditionalChineseTokenizer {
    /**
     * 预置分词器
     */
    public static Segment Segment = HanLP.newViterbiSegment();

    private static List<Term> segSentence(String text) {
        String sText = CharTable.convert(text);
        List<Term> termList = Segment.seg(sText);
        int offset = 0;
        for (Term term : termList) {
            String tText;
            term.offset = offset;
            if (term.length() == 1 || (tText = SimplifiedChineseDictionary.getTraditionalChinese(term.word)) == null) {
                term.word = text.substring(offset, offset + term.length());
                offset += term.length();
            }
            else {
                offset += term.length();
                term.word = tText;
            }
        }

        return termList;
    }

    public static List<Term> segment(String text) {
        List<Term> termList = new LinkedList<Term>();
        for (String sentence : SentencesUtil.toSentenceList(text)) {
            termList.addAll(segSentence(sentence));
        }

        return termList;
    }

    /**
     * 分词
     *
     * @param text 文本
     * @return 分词结果
     */
    public static List<Term> segment(char[] text) {
        text = CharTable.convert(text);
        return segment(new String(text));
    }

    /**
     * 切分为句子形式
     *
     * @param text 文本
     * @return 句子列表
     */
    public static List<List<Term>> seg2sentence(String text) {
        List<List<Term>> resultList = new LinkedList<List<Term>>();
        for (String sentence : SentencesUtil.toSentenceList(text)) {
            resultList.add(segment(sentence));
        }

        return resultList;
    }
}
