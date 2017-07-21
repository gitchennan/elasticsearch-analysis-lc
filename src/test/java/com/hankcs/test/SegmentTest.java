package com.hankcs.test;


import com.hankcs.hanlp.seg.Dijkstra.DijkstraSegment;
import com.hankcs.hanlp.seg.common.Term;
import org.junit.Test;

import java.util.List;

public class SegmentTest {
    @Test
    public void test_DijkstraSegment() {
        DijkstraSegment segment = new DijkstraSegment();
        List<Term> terms = segment.seg("对上海搞基有限公司的帮助文档来说是对的");

        for (Term term : terms) {
            System.out.print(term.word + "/" + term.nature.name() + " ");
        }
    }
}
