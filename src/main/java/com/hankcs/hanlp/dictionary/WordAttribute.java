package com.hankcs.hanlp.dictionary;

import com.hankcs.hanlp.corpus.io.ByteArray;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.log.HanLpLogger;
import com.hankcs.hanlp.utility.LexiconUtility;

import java.io.Serializable;

/**
 * 核心词典中的词属性
 */
public class WordAttribute implements Serializable {
    /**
     * 词性列表
     */
    public Nature nature[];
    /**
     * 词性对应的词频
     */
    public int frequency[];

    public int totalFrequency;


    public WordAttribute(int size) {
        nature = new Nature[size];
        frequency = new int[size];
    }

    public WordAttribute(Nature[] nature, int[] frequency) {
        this.nature = nature;
        this.frequency = frequency;
    }

    public WordAttribute(Nature nature, int frequency) {
        this(1);
        this.nature[0] = nature;
        this.frequency[0] = frequency;
        totalFrequency = frequency;
    }

    public WordAttribute(Nature[] nature, int[] frequency, int totalFrequency) {
        this.nature = nature;
        this.frequency = frequency;
        this.totalFrequency = totalFrequency;
    }

    /**
     * 使用单个词性，默认词频1000构造
     */
    public WordAttribute(Nature nature) {
        this(nature, 1000);
    }

    public static WordAttribute create(String natureWithFrequency) {
        try {
            String param[] = natureWithFrequency.split(" ");
            int natureCount = param.length / 2;
            WordAttribute attribute = new WordAttribute(natureCount);
            for (int i = 0; i < natureCount; ++i) {
//                attribute.nature[i] = LexiconUtility.convertStringToNature(param[2 * i], null);
                attribute.nature[i] = LexiconUtility.convertStringToNature(param[2 * i]);
                attribute.frequency[i] = Integer.parseInt(param[1 + 2 * i]);
                attribute.totalFrequency += attribute.frequency[i];
            }
            return attribute;
        }
        catch (Exception e) {
            HanLpLogger.error(CoreDictionary.class,
                    String.format("create word attribute error, text:%s", natureWithFrequency), e);
            return null;
        }
    }

    /**
     * 从字节流中加载
     */
    public static WordAttribute create(ByteArray byteArray, Nature[] natureIndexArray) {
        int currentTotalFrequency = byteArray.nextInt();
        int length = byteArray.nextInt();
        WordAttribute attribute = new WordAttribute(length);
        attribute.totalFrequency = currentTotalFrequency;
        for (int j = 0; j < length; ++j) {
            attribute.nature[j] = natureIndexArray[byteArray.nextInt()];
            attribute.frequency[j] = byteArray.nextInt();
        }

        return attribute;
    }

    /**
     * 获取词性的词频
     *
     * @param nature 字符串词性
     * @return 词频
     * @deprecated 推荐使用Nature参数！
     */
    public int getNatureFrequency(String nature) {
        try {
            Nature pos = Enum.valueOf(Nature.class, nature);
            return getNatureFrequency(pos);
        }
        catch (IllegalArgumentException e) {
            return 0;
        }
    }

    /**
     * 获取词性的词频
     *
     * @param nature 词性
     * @return 词频
     */
    public int getNatureFrequency(final Nature nature) {
        int i = 0;
        for (Nature pos : this.nature) {
            if (nature == pos) {
                return frequency[i];
            }
            ++i;
        }
        return 0;
    }

    /**
     * 是否有某个词性
     */
    public boolean hasNature(Nature nature) {
        return getNatureFrequency(nature) > 0;
    }

    /**
     * 是否有以某个前缀开头的词性
     *
     * @param prefix 词性前缀，比如u会查询是否有ude, uzhe等等
     */
    public boolean hasNatureStartsWith(String prefix) {
        for (Nature n : nature) {
            if (n.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nature.length; ++i) {
            sb.append(nature[i]).append(' ').append(frequency[i]).append(' ');
        }
        return sb.toString();
    }
}
