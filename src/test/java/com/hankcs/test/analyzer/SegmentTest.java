package com.hankcs.test.analyzer;


import com.hankcs.hanlp.api.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import org.junit.Test;

import java.util.List;

public class SegmentTest {

    String text = "陆金所-中国平安集团倾力打造的投资理财平台。在健全的风险管控体系基础上，" +
            "为投资者提供专业的理财服务。荣获亚洲银行家“最佳线上私人财富管理平台”。";

    @Test
    public void test_StandardSegment() {
        List<Term> terms = HanLP.newViterbiSegment().seg(text);

        for (Term term : terms) {
            System.out.print(term.word + "/" + term.nature.name() + " ");
        }
    }

    @Test
    public void test_indexSegment() {
        List<Term> terms = HanLP.newViterbiSegment()
                .enableOffset(true)
                .enableAllNamedEntityRecognize(true)
                .enableIndexMode(true)
                .seg(text);

        for (Term token : terms) {
            System.out.print(token.word + "/" + token.nature + " ");
        }
    }
}
