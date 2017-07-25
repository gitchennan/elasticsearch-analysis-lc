package com.hankcs.test.analyzer;

import com.google.common.collect.Sets;
import com.hankcs.hanlp.api.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import lc.lucene.analyzer.HanLPStandardAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class HanLpAnalyzerTest {

    @Test
    public void test_standardSegment() {
        List<Term> terms = HanLP.segment("对上海搞基有限公司的帮助文档来说是对的");
        for (Term term : terms) {
            System.out.print(term.word + "/" + term.nature.name() + " ");
        }
    }

    @Test
    public void test_indexSegment() {
        List<Term> terms = HanLP.newViterbiSegment()
                .enableIndexMode(true)
                .seg("对上海陆金所金融科技有限公司的稳盈e享计划是高收益产品");

        for (Term term : terms) {
            System.out.print(term.word + "/" + term.nature.name() + " ");
        }
    }

    @Test
    public void test_standardSegment2() {
        List<Term> terms = HanLP.newViterbiSegment()
                .enablePlaceRecognize(true)
                .enableAllNamedEntityRecognize(false)
                .enableCustomDictionary(true)
                .enableNumberQuantifierRecognize(true)
                .enableOffset(true)
                .seg("对上海陆金所金融科技有限公司的稳盈e享计划是高收益产品");

        for (Term term : terms) {
            System.out.print(term.word + "/" + term.nature.name() + " ");
        }
    }

    @Test
    public void testFirstLetterAnalysis() throws IOException {
        Analyzer analyzer = new HanLPStandardAnalyzer(Sets.newHashSet("的", "是"), true);
        TokenStream tokenStream = analyzer.tokenStream("lc", "对上海陆金所金融科技有限公司的稳盈e享计划是高收益产品");
        CharTermAttribute charTermAttribute = tokenStream.getAttribute(CharTermAttribute.class);
        OffsetAttribute offsetAttribute = tokenStream.getAttribute(OffsetAttribute.class);
        PositionIncrementAttribute positionIncrementAttribute = tokenStream.getAttribute(PositionIncrementAttribute.class);
        tokenStream.reset();

        while (tokenStream.incrementToken()) {
            System.out.println("-----------------------------------------");
            System.out.print("term:" + charTermAttribute.toString());
            System.out.print(", inc:" + positionIncrementAttribute.toString());
            System.out.print(", start:" + offsetAttribute.startOffset());
            System.out.print(", end:" + offsetAttribute.endOffset());
            System.out.println();
        }

        tokenStream.close();
    }
}
