
package com.hankcs.hanlp.dictionary;


import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.hankcs.hanlp.api.HanLpGlobalSettings;
import com.hankcs.hanlp.collection.AhoCorasick.AhoCorasickDoubleArrayTrie;
import com.hankcs.hanlp.collection.trie.DoubleArrayTrie;
import com.hankcs.hanlp.collection.trie.bintrie.BaseNode;
import com.hankcs.hanlp.collection.trie.bintrie.BinTrie;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.dictionary.other.CharTable;
import com.hankcs.hanlp.dictionary.searcher.CachedBinTrieSearcher;
import com.hankcs.hanlp.log.HanLpLogger;
import com.hankcs.hanlp.seg.common.Vertex;
import com.hankcs.hanlp.seg.common.WordNet;
import com.hankcs.hanlp.utility.LexiconUtility;
import com.hankcs.hanlp.utility.Predefine;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * 用户自定义词典
 *
 * @author He Han
 */
public class CustomDictionary extends FileSystemTxtDictionary {
    /**
     * custom dictionary's name
     */
    public static final String DICTIONARY_NAME = "CustomDictionary";
    /**
     * 用于存储从文件加载的用户词典
     */
    private volatile BinTrie<WordAttribute> binTrie;
    /**
     * 用于储存用户动态插入词条的二分trie树
     */
    private volatile DoubleArrayTrie<WordAttribute> doubleArrayTrie;
    /**
     * attribute map for preLoad dictionary
     */
    private TreeMap<String, WordAttribute> wordAttributeMap;

    /**
     * core dictionary singleton
     */
    public static final CustomDictionary INSTANCE;

    static {
        INSTANCE = new CustomDictionary();
        INSTANCE.load();
    }

    private CustomDictionary() {
        super(HanLpGlobalSettings.CustomDictionaryPath);
        binTrie = BinTrie.newBinTrie();
        doubleArrayTrie = DoubleArrayTrie.newDoubleArrayTrie();
        wordAttributeMap = Maps.newTreeMap();
    }

    @Override
    public String dictionaryName() {
        return DICTIONARY_NAME;
    }

//    @Override
//    public void releaseResource() {
//        binTrie = BinTrie.newBinTrie();
//        doubleArrayTrie = DoubleArrayTrie.newDoubleArrayTrie();
//
//        if (wordAttributeMap == null) {
//            wordAttributeMap = Maps.newTreeMap();
//        }
//        wordAttributeMap.clear();
//
//        HanLpLogger.info(this, "Release dictionary resource");
//    }

    @Override
    protected void onLoadLine(String line) {
        String[] param = line.split(DICT_LINE_SPLIT_CHAR);
        if (param[0].length() == 0) {
            return;
        }

        if (HanLpGlobalSettings.Normalization) {
            param[0] = CharTable.convert(param[0]);
        }

        int natureCount = (param.length - 1) / 2;
        WordAttribute attribute;
        if (natureCount == 0) {
            attribute = new WordAttribute(Nature.n, 1);
        }
        else {
            attribute = new WordAttribute(natureCount);
            for (int i = 0; i < natureCount; ++i) {
                attribute.nature[i] = LexiconUtility.convertStringToNature(param[1 + 2 * i]);
                attribute.frequency[i] = Integer.parseInt(param[2 + 2 * i]);
                attribute.totalFrequency += attribute.frequency[i];
            }
        }
        wordAttributeMap.put(param[0], attribute);
    }

    @Override
    protected void onDictionaryLoaded() {
        if (wordAttributeMap.isEmpty()) {
            HanLpLogger.warn(CustomDictionary.class, "There's no any word found in custom dictionaries.");
            wordAttributeMap.put(Predefine.TAG_OTHER, null);
        }

        Stopwatch stopwatch = Stopwatch.createStarted();
        int buildResult = doubleArrayTrie.build(wordAttributeMap);
        stopwatch.stop();
        if (buildResult == 0) {
            HanLpLogger.debug(this,
                    String.format("Build doubleArrayTrie dictionary, takes %sms build_result[%s]",
                            stopwatch.elapsed(TimeUnit.MILLISECONDS), buildResult));
        }
        else {
            HanLpLogger.error(this,
                    String.format("Error occurred while building doubleArrayTrie dictionary, build_result[%s]", buildResult));
        }
        wordAttributeMap.clear();
    }

    public boolean isBinTrieEmpty() {
        return binTrie == null || binTrie.size() == 0;
    }

    public BaseNode<WordAttribute> binTrieTransition(char[] path, int begin) {
        return binTrie.transition(path, begin);
    }

//    public void binTriePut(String key, WordAttribute attribute) {
//        binTrie.put(key, attribute);
//    }
//
//    public boolean updateWordAttribute(String key, WordAttribute wordAttribute) {
//        return doubleArrayTrie.updateValue(key, wordAttribute);
//    }

    /**
     * 往自定义词典中插入一个新词（非覆盖模式）<br>
     * 动态增删不会持久化到词典文件
     *
     * @param word                新词 如“裸婚”
     * @param natureWithFrequency 词性和其对应的频次，比如“nz 1 v 2”，null时表示“nz 1”
     * @return 是否插入成功（失败的原因可能是不覆盖、natureWithFrequency有问题等，后者可以通过调试模式了解原因）
     */
    public boolean add(String word, String natureWithFrequency) {
        if (word == null || contains(word)) {
            return false;
        }
        return insert(word, natureWithFrequency);
    }

    /**
     * 往自定义词典中插入一个新词（非覆盖模式）<br>
     * 动态增删不会持久化到词典文件
     *
     * @param word 新词 如“裸婚”
     * @return 是否插入成功（失败的原因可能是不覆盖等，可以通过调试模式了解原因）
     */
    public boolean add(String word) {
        if (word == null) {
            return false;
        }

        if (HanLpGlobalSettings.Normalization) {
            word = CharTable.convert(word);
        }

        if (contains(word)) {
            return false;
        }

        return insert(word, null);
    }

    /**
     * 往自定义词典中插入一个新词（覆盖模式）<br>
     * 动态增删不会持久化到词典文件
     *
     * @param word                新词 如“裸婚”
     * @param natureWithFrequency 词性和其对应的频次，比如“nz 1 v 2”，null时表示“nz 1”。
     * @return 是否插入成功（失败的原因可能是natureWithFrequency问题，可以通过调试模式了解原因）
     */
    public boolean insert(String word, String natureWithFrequency) {
        if (word == null) {
            return false;
        }

        if (HanLpGlobalSettings.Normalization) {
            word = CharTable.convert(word);
        }

        WordAttribute att = natureWithFrequency == null ?
                new WordAttribute(Nature.nz, 1) : WordAttribute.create(natureWithFrequency);

        if (att == null) {
            return false;
        }

//        if (INSTANCE.doubleArrayTrie.updateValue(word, att)) {
//            return true;
//        }

        INSTANCE.binTrie.put(word, att);
        return true;
    }

    /**
     * 以覆盖模式增加新词<br>
     * 动态增删不会持久化到词典文件
     */
    public boolean insert(String word) {
        return insert(word, null);
    }

    public void cleanBinTrie() {
        binTrie = BinTrie.newBinTrie();
    }

    /**
     * 查单词
     */
    public WordAttribute find(String key) {
        if (HanLpGlobalSettings.Normalization) {
            key = CharTable.convert(key);
        }

        WordAttribute attribute = INSTANCE.doubleArrayTrie.getValue(key);

        if (attribute != null) {
            return attribute;
        }

        return INSTANCE.binTrie.getValue(key);
    }

    /**
     * 删除单词<br>
     * 动态增删不会持久化到词典文件
     */
    public void remove(String key) {
        if (HanLpGlobalSettings.Normalization) {
            key = CharTable.convert(key);
        }

        INSTANCE.binTrie.remove(key);
    }

    /**
     * 前缀查询
     */
    public LinkedList<Map.Entry<String, WordAttribute>> commonPrefixSearch(String key) {
        return INSTANCE.binTrie.commonPrefixSearchWithValue(key);
    }

    /**
     * 前缀查询
     */
    public LinkedList<Map.Entry<String, WordAttribute>> commonPrefixSearch(char[] chars, int begin) {
        return INSTANCE.binTrie.commonPrefixSearchWithValue(chars, begin);
    }

    @Override
    public String toString() {
        return "CustomDictionary{" +
                "doubleArrayTrie=" + binTrie +
                '}';
    }

    /**
     * 词典中是否含有词语
     *
     * @param key 词语
     * @return 是否包含
     */
    public boolean contains(String key) {
        if (INSTANCE.doubleArrayTrie.exactMatchSearch(key) >= 0) {
            return true;
        }
        return INSTANCE.binTrie != null && INSTANCE.binTrie.containsKey(key);
    }

    /**
     * 获取一个BinTrie的查询工具
     *
     * @param text 文本
     * @return 查询者
     */
    public BaseSearcher<WordAttribute> getSearcher(String text) {
        return new CachedBinTrieSearcher<WordAttribute>(text, INSTANCE.binTrie);
    }

    /**
     * 解析一段文本（目前采用了BinTrie+DAT的混合储存形式，此方法可以统一两个数据结构）
     *
     * @param text      文本
     * @param processor 处理器
     */
    public void parseText(String text, AhoCorasickDoubleArrayTrie.IHit<WordAttribute> processor) {
        BaseSearcher<WordAttribute> binTrieSearcher = CustomDictionary.INSTANCE.getSearcher(text);
        int offset;
        Map.Entry<String, WordAttribute> entry;
        while ((entry = binTrieSearcher.next()) != null) {
            offset = binTrieSearcher.getOffset();
            processor.hit(offset, offset + entry.getKey().length(), entry.getValue());
        }

        DoubleArrayTrie<WordAttribute>.Searcher datSearcher = INSTANCE.doubleArrayTrie.getSearcher(text, 0);
        while (datSearcher.next()) {
            processor.hit(datSearcher.begin, datSearcher.begin + datSearcher.length, datSearcher.value);
        }
    }

    public void parseText(char[] text, AhoCorasickDoubleArrayTrie.IHit<WordAttribute> processor) {
        parseText(String.valueOf(text), processor);
    }

    /**
     * 使用用户词典合并粗分结果
     *
     * @param vertexList 粗分结果
     * @return 合并后的结果
     */
    public List<Vertex> combineByCustomDictionary(List<Vertex> vertexList) {
        Vertex[] wordNet = new Vertex[vertexList.size()];
        wordNet = vertexList.toArray(wordNet);
        // DAT合并
        for (int i = 0; i < wordNet.length; ++i) {
            int state = 1;
            state = doubleArrayTrie.transition(wordNet[i].realWord, state);
            if (state > 0) {
                int to = i + 1;
                int end = to;
                WordAttribute value = doubleArrayTrie.output(state);
                for (; to < wordNet.length; ++to) {
                    state = doubleArrayTrie.transition(wordNet[to].realWord, state);
                    if (state < 0) {
                        break;
                    }
                    WordAttribute output = doubleArrayTrie.output(state);
                    if (output != null) {
                        value = output;
                        end = to + 1;
                    }
                }
                if (value != null) {
                    combineWords(wordNet, i, end, value);
                    i = end - 1;
                }
            }
        }
        // BinTrie合并
        if (!CustomDictionary.INSTANCE.isBinTrieEmpty()) {
            for (int i = 0; i < wordNet.length; ++i) {
                if (wordNet[i] == null) {
                    continue;
                }
                BaseNode<WordAttribute> state = CustomDictionary.INSTANCE.binTrieTransition(wordNet[i].realWord.toCharArray(), 0);
                if (state != null) {
                    int to = i + 1;
                    int end = to;
                    WordAttribute value = state.getValue();
                    for (; to < wordNet.length; ++to) {
                        if (wordNet[to] == null) {
                            continue;
                        }
                        state = state.transition(wordNet[to].realWord.toCharArray(), 0);
                        if (state == null) {
                            break;
                        }
                        if (state.getValue() != null) {
                            value = state.getValue();
                            end = to + 1;
                        }
                    }
                    if (value != null) {
                        combineWords(wordNet, i, end, value);
                        i = end - 1;
                    }
                }
            }
        }

        vertexList.clear();
        for (Vertex vertex : wordNet) {
            if (vertex != null) {
                vertexList.add(vertex);
            }
        }

        return vertexList;
    }

    /**
     * 使用用户词典合并粗分结果，并将用户词语收集到全词图中
     *
     * @param vertexList 粗分结果
     * @param wordNetAll 收集用户词语到全词图中
     * @return 合并后的结果
     */
    public List<Vertex> combineByCustomDictionary(List<Vertex> vertexList, WordNet wordNetAll) {
        List<Vertex> outputList = CustomDictionary.INSTANCE.combineByCustomDictionary(vertexList);

        int line = 0;
        for (Vertex vertex : outputList) {
            int parentLength = vertex.realWord.length();
            int currentLine = line;
            if (parentLength >= 3) {
                CustomDictionary.INSTANCE.parseText(vertex.realWord, new AhoCorasickDoubleArrayTrie.IHit<WordAttribute>() {
                    @Override
                    public void hit(int begin, int end, WordAttribute value) {
                        if (end - begin == parentLength) {
                            return;
                        }
                        wordNetAll.add(currentLine + begin, new Vertex(vertex.realWord.substring(begin, end), value));
                    }
                });
            }
            line += parentLength;
        }
        return outputList;
    }

    /**
     * 将连续的词语合并为一个
     *
     * @param wordNet 词图
     * @param start   起始下标（包含）
     * @param end     结束下标（不包含）
     * @param value   新的属性
     */
    private void combineWords(Vertex[] wordNet, int start, int end, WordAttribute value) {
        // 小优化，如果只有一个词，那就不需要合并，直接应用新属性
        if (start + 1 == end) {
            wordNet[start].attribute = value;
        }
        else {
            StringBuilder sbTerm = new StringBuilder();
            for (int j = start; j < end; ++j) {
                if (wordNet[j] == null) {
                    continue;
                }
                String realWord = wordNet[j].realWord;
                sbTerm.append(realWord);
                wordNet[j] = null;
            }
            wordNet[start] = new Vertex(sbTerm.toString(), value);
        }
    }
}
