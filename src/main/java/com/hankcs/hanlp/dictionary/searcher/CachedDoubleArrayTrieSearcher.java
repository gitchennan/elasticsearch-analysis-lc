package com.hankcs.hanlp.dictionary.searcher;

import com.google.common.collect.Lists;
import com.hankcs.hanlp.collection.trie.DoubleArrayTrie;
import com.hankcs.hanlp.dictionary.BaseSearcher;

import java.util.List;
import java.util.Map;

public class CachedDoubleArrayTrieSearcher<V> extends BaseSearcher<V> {
    /**
     * 分词从何处开始，这是一个状态
     */
    private int begin;

    private DoubleArrayTrie<V> doubleArrayTrie;

    private List<Map.Entry<String, V>> entryList = Lists.newLinkedList();

    public CachedDoubleArrayTrieSearcher(String text, DoubleArrayTrie<V> doubleArrayTrie) {
        super(text);
        this.doubleArrayTrie = doubleArrayTrie;
    }

    @Override
    public Map.Entry<String, V> next() {
        // 保证首次调用找到一个词语
        while (entryList.size() == 0 && begin < text.length) {
            entryList = doubleArrayTrie.commonPrefixSearchWithValue(text, begin);
            ++begin;
        }

        // 之后调用仅在缓存用完的时候调用一次
        if (entryList.size() == 0 && begin < text.length) {
            entryList = doubleArrayTrie.commonPrefixSearchWithValue(text, begin);
            ++begin;
        }

        if (entryList.size() == 0) {
            return null;
        }

        Map.Entry<String, V> result = entryList.get(0);
        entryList.remove(0);
        offset = begin - 1;
        return result;
    }
}