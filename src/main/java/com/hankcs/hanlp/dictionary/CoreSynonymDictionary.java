package com.hankcs.hanlp.dictionary;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.hankcs.hanlp.api.HanLpGlobalSettings;
import com.hankcs.hanlp.collection.trie.bintrie.BinTrie;
import com.hankcs.hanlp.corpus.synonym.Synonym;
import com.hankcs.hanlp.dictionary.common.CommonSynonymDictionary;
import com.hankcs.hanlp.io.IOSafeHelper;
import com.hankcs.hanlp.io.InputStreamOperator;
import com.hankcs.hanlp.log.HanLpLogger;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 核心同义词词典
 *
 * @author hankcs
 */
public class CoreSynonymDictionary extends CommonSynonymDictionary {

    public volatile static CoreSynonymDictionary INSTANCE;

    private volatile static BinTrie<SynonymItem> customSynonymBinTrie;

    static {
        INSTANCE = new CoreSynonymDictionary();

        String dictPath = HanLpGlobalSettings.CoreSynonymDictionaryDictionaryPath;
        IOSafeHelper.openAutoCloseableFileInputStream(dictPath, new InputStreamOperator() {
            @Override
            public void process(InputStream inputStream) throws Exception {
                Stopwatch stopwatch = Stopwatch.createStarted();

                INSTANCE.load(inputStream);

                HanLpLogger.info(CoreSynonymDictionary.class,
                        String.format("Load dictionary[%s], takes %s ms, path[%s]", "CoreSynonymDictionary",
                                stopwatch.elapsed(TimeUnit.MILLISECONDS), HanLpGlobalSettings.CoreSynonymDictionaryDictionaryPath));
            }
        });

        customSynonymBinTrie = BinTrie.newBinTrie();
    }

    public void cleanBinTrie() {
        customSynonymBinTrie = BinTrie.newBinTrie();
    }

    public void add(Synonym.Type synonymType, String... synonyms) {
        add(synonymType, Lists.newArrayList(synonyms));
    }

    public void add(Synonym.Type synonymType, List<String> synonyms) {
        if (synonyms == null || synonyms.size() == 0) {
            return;
        }

        List<Synonym> synonymList = createSynonym(synonyms, synonymType);
        for (Synonym synonym : synonymList) {
            SynonymItem synonymItem = new SynonymItem(synonym, synonymList, synonymType);
            customSynonymBinTrie.put(synonym.getRealWord(), synonymItem);
        }
    }

    private List<Synonym> createSynonym(List<String> synonyms, Synonym.Type synonymType) {
        List<Synonym> synonymList = Lists.newArrayListWithCapacity(synonyms.size());
        for (String synonym : synonyms) {
            synonymList.add(new Synonym(synonym, 0, synonymType));
        }
        return synonymList;
    }

    @Override
    public SynonymItem find(String key) {
        SynonymItem synonymItem = customSynonymBinTrie.getValue(key);
        if (synonymItem != null) {
            return synonymItem;
        }

        return super.find(key);
    }
}