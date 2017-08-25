package lc.lucene.analyzer;

public class LcAnalyzerConfig {

    private boolean singleCharMode = false;

    private boolean indexMode = false;

    private boolean stopWordRecognize = true;

    private boolean synonymRecognize = false;

    private boolean namedEntityRecognize = true;

    private boolean keepChinese = true;

    private boolean extractFullPinyin = false;

    private boolean extractPinyinFirstLetter = false;

    private boolean lowerCase = true;

    public boolean isSingleCharMode() {
        return singleCharMode;
    }

    public boolean isSynonymRecognize() {
        return synonymRecognize;
    }

    public void setSynonymRecognize(boolean synonymRecognize) {
        this.synonymRecognize = synonymRecognize;
    }

    public boolean isNamedEntityRecognize() {
        return namedEntityRecognize;
    }

    public void setNamedEntityRecognize(boolean namedEntityRecognize) {
        this.namedEntityRecognize = namedEntityRecognize;
    }

    public boolean isStopWordRecognize() {
        return stopWordRecognize;
    }

    public void setStopWordRecognize(boolean stopWordRecognize) {
        this.stopWordRecognize = stopWordRecognize;
    }

    public boolean isIndexMode() {
        return indexMode;
    }

    public void setIndexMode(boolean indexMode) {
        this.indexMode = indexMode;
    }

    public boolean isLowerCase() {
        return lowerCase;
    }

    public void setLowerCase(boolean lowerCase) {
        this.lowerCase = lowerCase;
    }

    public boolean isExtractFullPinyin() {
        return extractFullPinyin;
    }

    public void setExtractFullPinyin(boolean extractFullPinyin) {
        this.extractFullPinyin = extractFullPinyin;
    }

    public boolean isExtractPinyinFirstLetter() {
        return extractPinyinFirstLetter;
    }

    public void setExtractPinyinFirstLetter(boolean extractPinyinFirstLetter) {
        this.extractPinyinFirstLetter = extractPinyinFirstLetter;
    }

    public boolean isKeepChinese() {
        return keepChinese;
    }

    public void setKeepChinese(boolean keepChinese) {
        this.keepChinese = keepChinese;
    }

    public void setSingleCharMode(boolean singleCharMode) {
        this.singleCharMode = singleCharMode;
    }
}
