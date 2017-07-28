package lc.lucene.domain;

import java.util.List;

public class CustomWord {

    private String word;

    private List<CustomWordAttribute> wordAttributes;

    public CustomWord(String word, List<CustomWordAttribute> wordAttributes) {
        this.word = word;
        this.wordAttributes = wordAttributes;
    }

    public String getWord() {
        return word;
    }

    public List<CustomWordAttribute> getWordAttributes() {
        return wordAttributes;
    }
}
