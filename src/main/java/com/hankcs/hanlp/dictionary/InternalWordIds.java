package com.hankcs.hanlp.dictionary;

import com.hankcs.hanlp.utility.Predefine;

/**
 * 一些特殊词的ID
 */
public class InternalWordIds {
    public static final int NR_WORD_ID = CoreDictionary.INSTANCE.getWordID(Predefine.TAG_PEOPLE);
    public static final int NS_WORD_ID = CoreDictionary.INSTANCE.getWordID(Predefine.TAG_PLACE);
    public static final int NT_WORD_ID = CoreDictionary.INSTANCE.getWordID(Predefine.TAG_GROUP);
    public static final int T_WORD_ID = CoreDictionary.INSTANCE.getWordID(Predefine.TAG_TIME);
    public static final int X_WORD_ID = CoreDictionary.INSTANCE.getWordID(Predefine.TAG_CLUSTER);
    public static final int M_WORD_ID = CoreDictionary.INSTANCE.getWordID(Predefine.TAG_NUMBER);
    public static final int NX_WORD_ID = CoreDictionary.INSTANCE.getWordID(Predefine.TAG_PROPER);
}
