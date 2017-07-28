package com.hankcs.hanlp.dictionary.searcher;

import com.google.common.collect.Lists;
import com.hankcs.hanlp.collection.trie.bintrie.BinTrie;
import com.hankcs.hanlp.dictionary.BaseSearcher;

import java.util.LinkedList;
import java.util.Map;

public class CachedBinTrieSearcher<V> extends BaseSearcher<V> {
    /**
     * 分词从何处开始，这是一个状态
     */
    private int begin;

    private BinTrie<V> binTrie;

    private LinkedList<Map.Entry<String, V>> entryList = Lists.newLinkedList();

    public CachedBinTrieSearcher(String text, BinTrie<V> binTrie) {
        super(text);
        this.binTrie = binTrie;

    }

    @Override
    public Map.Entry<String, V> next() {
        // 保证首次调用找到一个词语
        while (entryList.size() == 0 && begin < text.length) {
            entryList = binTrie.commonPrefixSearchWithValue(text, begin);
            ++begin;
        }

        // 之后调用仅在缓存用完的时候调用一次
        if (entryList.size() == 0 && begin < text.length) {
            entryList = binTrie.commonPrefixSearchWithValue(text, begin);
            ++begin;
        }

        if (entryList.size() == 0) {
            return null;
        }

        Map.Entry<String, V> result = entryList.getFirst();
        entryList.removeFirst();
        offset = begin - 1;
        return result;
    }

}