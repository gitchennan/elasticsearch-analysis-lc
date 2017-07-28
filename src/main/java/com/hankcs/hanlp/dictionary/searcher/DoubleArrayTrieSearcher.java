package com.hankcs.hanlp.dictionary.searcher;

import com.hankcs.hanlp.collection.trie.DoubleArrayTrie;
import com.hankcs.hanlp.dictionary.BaseSearcher;

import java.util.LinkedList;
import java.util.Map;

public class DoubleArrayTrieSearcher<V> extends BaseSearcher<V> {
    /**
     * 分词从何处开始，这是一个状态
     */
    private int begin;

    private DoubleArrayTrie<V> doubleArrayTrie;

    public DoubleArrayTrieSearcher(String text, DoubleArrayTrie<V> doubleArrayTrie) {
        super(text);
        this.doubleArrayTrie = doubleArrayTrie;
    }

    @Override
    public Map.Entry<String, V> next() {
        // 保证首次调用找到一个词语
        Map.Entry<String, V> result = null;
        while (begin < text.length) {
            LinkedList<Map.Entry<String, V>> entryList
                    = doubleArrayTrie.commonPrefixSearchWithValue(text, begin);

            if (entryList.size() == 0) {
                ++begin;
            }
            else {
                result = entryList.getLast();
                offset = begin;
                begin += result.getKey().length();
                break;
            }
        }
        if (result == null) {
            return null;
        }
        return result;
    }
}