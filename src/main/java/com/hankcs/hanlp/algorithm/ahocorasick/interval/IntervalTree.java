package com.hankcs.hanlp.algorithm.ahocorasick.interval;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * 线段树，用于检查区间重叠
 */
public class IntervalTree {
    /**
     * 根节点
     */
    private IntervalNode rootNode = null;

    /**
     * 构造线段树
     *
     * @param intervals
     */
    public IntervalTree(List<Interval> intervals) {
        this.rootNode = new IntervalNode(intervals);
    }

    /**
     * 从区间列表中移除重叠的区间
     *
     * @param intervals
     * @return
     */
    public List<Interval> removeOverlaps(List<Interval> intervals) {

        // 排序，按照先大小后左端点的顺序
        Collections.sort(intervals, IntervalComparators.newBasedOnSizeComparator());

        Set<Interval> removeIntervals = new TreeSet<Interval>();

        for (Interval interval : intervals) {
            // 如果区间已经被移除了，就忽略它
            if (removeIntervals.contains(interval)) {
                continue;
            }

            // 否则就移除它
            removeIntervals.addAll(findOverlaps(interval));
        }

        // 移除所有的重叠区间
        for (Interval removeInterval : removeIntervals) {
            intervals.remove(removeInterval);
        }

        // 排序，按照左端顺序
        Collections.sort(intervals, IntervalComparators.newBasedOnPositionComparator());

        return intervals;
    }

    /**
     * 寻找重叠区间
     *
     * @param interval 与这个区间重叠
     * @return 重叠的区间列表
     */
    public List<Interval> findOverlaps(Interval interval) {
        return rootNode.findOverlaps(interval);
    }

}
