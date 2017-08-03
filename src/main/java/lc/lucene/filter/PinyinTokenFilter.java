package lc.lucene.filter;

import com.google.common.collect.Lists;
import com.hankcs.hanlp.dictionary.py.Pinyin;
import com.hankcs.hanlp.dictionary.py.PinyinDictionary;
import com.hankcs.hanlp.dictionary.py.Shengmu;
import com.hankcs.hanlp.dictionary.py.Yunmu;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class PinyinTokenFilter extends TokenFilter {
    // 当前词
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    //拼音缓存
    private final LinkedList<String> pinyinCache = Lists.newLinkedList();
    //full / head / all
    private String pinyinMode = "full";

    private boolean keepChinese;

    public PinyinTokenFilter(TokenStream input, String pinyinMode, boolean keepChinese) {
        super(input);
        if (keepFullPinyin(pinyinMode) || keepPinyinFirstLetter(pinyinMode)) {
            this.pinyinMode = pinyinMode;
        }

        this.keepChinese = keepChinese;
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (hasMorePinyinInCache()) {
            String pinyin = nextCachedPinyinTerm();
            termAtt.setEmpty();
            termAtt.append(pinyin);
            return true;
        }

        if (!this.input.incrementToken()) {
            return false;
        }

        char[] text = this.termAtt.buffer();
        int termLength = this.termAtt.length();

        String curWord = String.valueOf(text, 0, termLength);

        List<Pinyin> pinyinList = PinyinDictionary.INSTANCE.convertToPinyin(curWord);
        if (pinyinList != null && pinyinList.size() == termLength) {
            StringBuilder fullPinyinBuilder = new StringBuilder();
            StringBuilder firstLetterBuilder = new StringBuilder();

            int idx = 0;
            for (Pinyin pinyin : pinyinList) {
                if (pinyin.getShengmu() == Shengmu.none && pinyin.getYunmu() == Yunmu.none) {
                    fullPinyinBuilder.append(text[idx]);
                    firstLetterBuilder.append(text[idx]);
                }
                else {
                    String curFullPinyin = pinyin.getPinyinWithoutTone();
                    String curFirstLetter = String.valueOf(curFullPinyin.charAt(0));

                    if (keepFullPinyin(pinyinMode)) {
                        fullPinyinBuilder.append(curFullPinyin);
                    }

                    if (keepPinyinFirstLetter(pinyinMode)) {
                        firstLetterBuilder.append(curFirstLetter);
                    }
                }
                idx++;
            }
            if (keepFullPinyin(pinyinMode)) {
                String curWordFullPinyin = fullPinyinBuilder.toString();
                if (!curWord.equals(curWordFullPinyin)) {
                    addPinyinToCache(curWordFullPinyin);
                }
            }

            if (keepPinyinFirstLetter(pinyinMode)) {
                String firstLetters = firstLetterBuilder.toString();
                if (!curWord.equals(firstLetters)) {
                    addPinyinToCache(firstLetters);
                }
            }
        }

        if (keepChinese) {
            this.termAtt.setEmpty();
            this.termAtt.append(curWord);
        }
        else {
            if (hasMorePinyinInCache()) {
                String pinyin = nextCachedPinyinTerm();
                termAtt.setEmpty();
                termAtt.append(pinyin);
            }
            else {
                this.termAtt.setEmpty();
                this.termAtt.append(curWord);
            }
        }
        return true;
    }

    private boolean keepFullPinyin(String pinyinMode) {
        return "full".equalsIgnoreCase(pinyinMode) || "all".equalsIgnoreCase(pinyinMode);
    }

    private boolean keepPinyinFirstLetter(String pinyinMode) {
        return "head".equalsIgnoreCase(pinyinMode);
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        pinyinCache.clear();
    }

    protected boolean hasMorePinyinInCache() {
        return !pinyinCache.isEmpty();
    }

    private String nextCachedPinyinTerm() {
        return pinyinCache.pollFirst();
    }

    private void addPinyinToCache(String pinyin) {
        if (pinyin != null) {
            pinyinCache.add(pinyin);
        }
    }

}
