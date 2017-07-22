package com.hankcs.hanlp.dictionary;

public interface Dictionary {

    String dictionaryName();

    void load();

    void reLoad();

    void releaseResource();

}
