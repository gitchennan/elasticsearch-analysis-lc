package com.hankcs.hanlp.utility;

import com.google.common.collect.Lists;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.seg.common.Term;

import java.util.List;

/**
 * 文本断句
 */
public class SentencesUtil {
    /**
     * 将文本切割为句子
     */
    public static List<String> toSentenceList(String content) {
        return toSentenceList(content.toCharArray());
    }

    public static List<String> toSentenceList(char[] chars) {
        StringBuilder sentenceBuilder = new StringBuilder();
        List<String> sentences = Lists.newLinkedList();

        for (int i = 0; i < chars.length; ++i) {
            if (sentenceBuilder.length() == 0 && (Character.isWhitespace(chars[i]) || chars[i] == ' ')) {
                continue;
            }

            sentenceBuilder.append(chars[i]);
            switch (chars[i]) {
                case '.':
                    if (i < chars.length - 1 && chars[i + 1] > 128) {
                        insertIntoList(sentenceBuilder, sentences);
                        sentenceBuilder = new StringBuilder();
                    }
                    break;
                case '…': {
                    if (i < chars.length - 1 && chars[i + 1] == '…') {
                        sentenceBuilder.append('…');
                        ++i;
                        insertIntoList(sentenceBuilder, sentences);
                        sentenceBuilder = new StringBuilder();
                    }
                }
                break;
                case ' ':
                case '	':
                case ' ':
                case '。':
                case '，':
                case ',':
                    insertIntoList(sentenceBuilder, sentences);
                    sentenceBuilder = new StringBuilder();
                    break;
                case ';':
                case '；':
                    insertIntoList(sentenceBuilder, sentences);
                    sentenceBuilder = new StringBuilder();
                    break;
                case '!':
                case '！':
                    insertIntoList(sentenceBuilder, sentences);
                    sentenceBuilder = new StringBuilder();
                    break;
                case '?':
                case '？':
                    insertIntoList(sentenceBuilder, sentences);
                    sentenceBuilder = new StringBuilder();
                    break;
                case '\n':
                case '\r':
                    insertIntoList(sentenceBuilder, sentences);
                    sentenceBuilder = new StringBuilder();
                    break;
            }
        }

        if (sentenceBuilder.length() > 0) {
            insertIntoList(sentenceBuilder, sentences);
        }

        return sentences;
    }

    private static void insertIntoList(StringBuilder sb, List<String> sentences) {
        String content = sb.toString().trim();
        if (content.length() > 0) {
            sentences.add(content);
        }
    }

    /**
     * 句子中是否含有词性
     */
    public static boolean hasNatureInSentence(List<Term> sentence, Nature nature) {
        for (Term term : sentence) {
            if (term.nature == nature) {
                return true;
            }
        }
        return false;
    }
}
