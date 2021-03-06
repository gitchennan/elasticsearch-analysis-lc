package com.hankcs.hanlp.seg.Other;

import com.hankcs.hanlp.collection.AhoCorasick.AhoCorasickDoubleArrayTrie;
import com.hankcs.hanlp.collection.trie.DoubleArrayTrie;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.dictionary.CoreDictionary;
import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.dictionary.WordAttribute;
import com.hankcs.hanlp.seg.DictionaryBasedSegment;
import com.hankcs.hanlp.seg.NShort.Path.AtomNode;
import com.hankcs.hanlp.seg.common.Term;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 使用DoubleArrayTrie实现的最长分词器
 *
 * @author hankcs
 */
public class DoubleArrayTrieSegment extends DictionaryBasedSegment {
    @Override
    protected List<Term> segSentence(char[] sentence) {
        int[] wordNet = new int[sentence.length];
        Arrays.fill(wordNet, 1);
        Nature[] natureArray = config.speechTagging ? new Nature[sentence.length] : null;

        DoubleArrayTrie<WordAttribute>.Searcher searcher = CoreDictionary.INSTANCE.getSearcher(sentence, 0);
        while (searcher.next()) {
            int length = searcher.length;
            if (length > wordNet[searcher.begin]) {
                wordNet[searcher.begin] = length;
                if (config.speechTagging && natureArray != null) {
                    natureArray[searcher.begin] = searcher.value.nature[0];
                }
            }
        }
        if (config.useCustomDictionary) {
            CustomDictionary.INSTANCE.parseText(sentence, new AhoCorasickDoubleArrayTrie.IHit<WordAttribute>() {
                @Override
                public void hit(int begin, int end, WordAttribute value) {
                    int length = end - begin;
                    if (length > wordNet[begin]) {
                        wordNet[begin] = length;
                        if (config.speechTagging && natureArray != null) {
                            natureArray[begin] = value.nature[0];
                        }
                    }
                }
            });
        }
        LinkedList<Term> termList = new LinkedList<Term>();
        if (config.speechTagging && natureArray != null) {
            for (int i = 0; i < natureArray.length; ) {
                if (natureArray[i] == null) {
                    int j = i + 1;
                    for (; j < natureArray.length; ++j) {
                        if (natureArray[j] != null) break;
                    }
                    List<AtomNode> atomNodeList = quickAtomSegment(sentence, i, j);
                    for (AtomNode atomNode : atomNodeList) {
                        if (atomNode.sWord.length() >= wordNet[i]) {
                            wordNet[i] = atomNode.sWord.length();
                            natureArray[i] = atomNode.getNature();
                            i += wordNet[i];
                        }
                    }
                    i = j;
                }
                else {
                    ++i;
                }
            }
        }
        for (int i = 0; i < wordNet.length; ) {
            Term term = new Term(
                    new String(sentence, i, wordNet[i]),
                    (config.speechTagging && natureArray != null) ? (natureArray[i] == null ? Nature.nz : natureArray[i]) : null
            );
            term.offset = i;
            termList.add(term);
            i += wordNet[i];
        }
        return termList;
    }

    public DoubleArrayTrieSegment() {
        super();
        config.useCustomDictionary = false;
    }
}
