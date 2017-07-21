package com.hankcs.test;

import com.hankcs.hanlp.api.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import org.junit.Test;

import java.util.List;

public class HanLpAnalyzerTest {

    @Test
    public void test_standardSegmentx() {
        List<Term> terms = HanLP.segment("对上海搞基有限公司的帮助文档来说是对的");
        for (Term term : terms) {
            System.out.print(term.word + "/" + term.nature.name() + " ");
        }
    }

    @Test
    public void test_standardSegment2() {
        List<Term> terms = HanLP.newSegment()
                .enablePlaceRecognize(true)
                .enableAllNamedEntityRecognize(true)
                .enableCustomDictionary(true)
                .enableMultithreading(false)
                .enableNumberQuantifierRecognize(true)
                .enableOffset(true)
                .seg("对上海搞基有限公司的帮助文档来说是对的");

        for (Term term : terms) {
            System.out.print(term.word + "/" + term.nature.name() + " ");
        }
    }
}
