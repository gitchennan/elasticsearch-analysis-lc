package lc.lucene.filter;

import com.google.common.collect.Lists;
import com.hankcs.hanlp.corpus.synonym.Synonym;
import com.hankcs.hanlp.dictionary.CoreSynonymDictionary;
import com.hankcs.hanlp.dictionary.common.CommonSynonymDictionary;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.LinkedList;

public class SynonymTokenFilter extends TokenFilter {
    // 当前词
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    //同义词缓存
    private final LinkedList<Synonym> synonymCache = Lists.newLinkedList();

    public SynonymTokenFilter(TokenStream in) {
        super(in);
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (hasMoreSynonymInCache()) {
            Synonym synonym = nextCachedSynonymTerm();
            termAtt.setEmpty();
            termAtt.append(synonym.getRealWord());
            return true;
        }

        if (!this.input.incrementToken()) {
            return false;
        }

        char[] text = this.termAtt.buffer();
        int termLength = this.termAtt.length();

        String curWord = String.valueOf(text, 0, termLength);
        CommonSynonymDictionary.SynonymItem synonymItem = CoreSynonymDictionary.INSTANCE.find(curWord);

        if (synonymItem != null && synonymItem.type == Synonym.Type.EQUAL
                && synonymItem.synonymList != null && synonymItem.synonymList.size() > 0) {
            for (Synonym synonym : synonymItem.synonymList) {
                if (curWord.equals(synonym.realWord)) {
                    continue;
                }
                addSynonymTermToCache(synonym);
            }
        }

        this.termAtt.setEmpty();
        this.termAtt.append(String.valueOf(text, 0, termLength));
        return true;
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        synonymCache.clear();
    }

    protected boolean hasMoreSynonymInCache() {
        return !synonymCache.isEmpty();
    }

    private Synonym nextCachedSynonymTerm() {
        return synonymCache.pollFirst();
    }

    private void addSynonymTermToCache(Synonym synonym) {
        if (synonym != null) {
            synonymCache.add(synonym);
        }
    }
}
