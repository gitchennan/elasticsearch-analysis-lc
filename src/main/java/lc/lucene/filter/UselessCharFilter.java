package lc.lucene.filter;

import com.google.common.collect.Sets;
import lc.lucene.util.CharacterUtil;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.Set;

public class UselessCharFilter extends TokenFilter {
    private CharTermAttribute termAtt = (CharTermAttribute) this.addAttribute(CharTermAttribute.class);

    Set<Character> specialChars = Sets.newHashSet(
            '!', '@', '#', '$', '%', '^', '&', '*',
            '(', ')', '-', '_', '+', '=', '/', '\\',
            '?', ',', '.', '"', ':', '[', ']',
            '{', '}', '|', '~', '`', '<', '>', ';'
    );

    public UselessCharFilter(TokenStream in) {
        super(in);
    }

    @Override
    public boolean incrementToken() throws IOException {
        while (this.input.incrementToken()) {
            char[] text = this.termAtt.buffer();
            int termLength = this.termAtt.length();

            for (int idx = 0; idx < termLength; idx++) {
                if (specialChars.contains(text[idx])) {
                    return true;
                }

                int charType = CharacterUtil.identifyCharType(text[idx]);
                if (charType != CharacterUtil.CHAR_USELESS) {
                    this.termAtt.setEmpty();
                    this.termAtt.append(String.valueOf(text, 0, termLength));
                    return true;
                }
            }
        }

        return false;
    }
}