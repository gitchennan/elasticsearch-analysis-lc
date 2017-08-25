package com.hankcs.test.analyzer;

import com.hankcs.hanlp.seg.common.Term;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;
import java.util.List;

public class BaseAnalyzerTest {
    protected void showWords(List<Term> terms) {
        StringBuilder wordsBuilder = new StringBuilder();
        wordsBuilder.append("【");
        for (Term term : terms) {
            wordsBuilder.append(String.format("%s/%s, ", term.word, term.nature));
        }
        wordsBuilder.append("】");
        System.out.println(wordsBuilder.toString());
    }

    protected void showWords(TokenStream tokenStream) throws IOException {
        StringBuilder wordsBuilder = new StringBuilder();
        wordsBuilder.append("【");

        CharTermAttribute charTermAttribute = tokenStream.getAttribute(CharTermAttribute.class);
//        OffsetAttribute offsetAttribute = tokenStream.getAttribute(OffsetAttribute.class);
//        PositionIncrementAttribute positionIncrementAttribute = tokenStream.getAttribute(PositionIncrementAttribute.class);
        TypeAttribute typeAtt = tokenStream.getAttribute(TypeAttribute.class);
        tokenStream.reset();

        while (tokenStream.incrementToken()) {
            wordsBuilder.append(String.format("%s/%s,", charTermAttribute.toString(), typeAtt.type()));
        }
        if (wordsBuilder.length() > 1) {
            wordsBuilder.deleteCharAt(wordsBuilder.length() - 1);
        }

        wordsBuilder.append("】");

        System.out.println(wordsBuilder.toString());
    }

}
