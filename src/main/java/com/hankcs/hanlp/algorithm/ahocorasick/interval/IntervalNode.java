package com.hankcs.hanlp.algorithm.ahocorasick.interval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 线段树上面的节点，实际上是一些区间的集合，并且按中点维护了两个节点
 */
public class IntervalNode {
    /**
     * 区间集合的最左端
     */
    private IntervalNode left = null;
    /**
     * 最右端
     */
    private IntervalNode right = null;
    /**
     * 中点
     */
    private int point;
    /**
     * 区间集合
     */
    private List<Interval> intervals = new ArrayList<Interval>();
    /**
     * 构造一个节点
     */
    public IntervalNode(List<? extends Interval> intervals) {
        this.point = determineMedian(intervals);

        /**
         * 以中点为界靠左的区间
         */
        List<Interval> toLeft = new ArrayList<Interval>();
        /**
         * 靠右的区间
         */
        List<Interval> toRight = new ArrayList<Interval>();

        for (Interval interval : intervals) {
            if (interval.getEnd() < this.point) {
                toLeft.add(interval);
            }
            else if (interval.getStart() > this.point) {
                toRight.add(interval);
            }
            else {
                this.intervals.add(interval);
            }
        }

        if (toLeft.size() > 0) {
            this.left = new IntervalNode(toLeft);
        }
        if (toRight.size() > 0) {
            this.right = new IntervalNode(toRight);
        }
    }

    /**
     * 是对IntervalNode.findOverlaps(Interval)的一个包装，防止NPE
     *
     * @see IntervalNode#findOverlaps(Interval)
     */
    protected static List<Interval> findOverlappingRanges(IntervalNode node, Interval interval) {
        if (node != null) {
            return node.findOverlaps(interval);
        }
        return Collections.emptyList();
    }

    /**
     * 计算中点
     *
     * @param intervals 区间集合
     * @return 中点坐标
     */
    public int determineMedian(List<? extends Interval> intervals) {
        int start = -1;
        int end = -1;
        for (Interval interval : intervals) {
            int currentStart = interval.getStart();
            int currentEnd = interval.getEnd();
            if (start == -1 || currentStart < start) {
                start = currentStart;
            }
            if (end == -1 || currentEnd > end) {
                end = currentEnd;
            }
        }
        return (start + end) / 2;
    }

    /**
     * 寻找与interval有重叠的区间
     */
    public List<Interval> findOverlaps(Interval interval) {
        List<Interval> overlaps = new ArrayList<Interval>();

        if (this.point < interval.getStart()) {
            addToOverlaps(interval, overlaps, findOverlappingRanges(this.right, interval));
            addToOverlaps(interval, overlaps, checkForOverlapsToTheRight(interval));
        }
        else if (this.point > interval.getEnd()) {
            addToOverlaps(interval, overlaps, findOverlappingRanges(this.left, interval));
            addToOverlaps(interval, overlaps, checkForOverlapsToTheLeft(interval));
        }
        else {
            addToOverlaps(interval, overlaps, this.intervals);
            addToOverlaps(interval, overlaps, findOverlappingRanges(this.left, interval));
            addToOverlaps(interval, overlaps, findOverlappingRanges(this.right, interval));
        }

        return overlaps;
    }

    /**
     * 添加到重叠区间列表中
     *
     * @param interval    跟此区间重叠
     * @param overlaps    重叠区间列表
     * @param newOverlaps 希望将这些区间加入
     */
    protected void addToOverlaps(Interval interval, List<Interval> overlaps, List<Interval> newOverlaps) {
        for (Interval currentInterval : newOverlaps) {
            if (!currentInterval.equals(interval)) {
                overlaps.add(currentInterval);
            }
        }
    }

    /**
     * 往左边寻找重叠
     */
    protected List<Interval> checkForOverlapsToTheLeft(Interval interval) {
        return checkForOverlaps(interval, Direction.LEFT);
    }

    /**
     * 往右边寻找重叠
     */
    protected List<Interval> checkForOverlapsToTheRight(Interval interval) {
        return checkForOverlaps(interval, Direction.RIGHT);
    }

    /**
     * 寻找重叠
     *
     * @param interval  一个区间，与该区间重叠
     * @param direction 方向，表明重叠区间在interval的左边还是右边
     * @return 重叠的区间
     */
    protected List<Interval> checkForOverlaps(Interval interval, Direction direction) {
        List<Interval> overlaps = new ArrayList<Interval>();
        for (Interval currentInterval : this.intervals) {

            if (Direction.LEFT == direction) {
                if (currentInterval.getStart() <= interval.getEnd()) {
                    overlaps.add(currentInterval);
                }
            }
            else if (Direction.RIGHT == direction) {
                if (currentInterval.getEnd() >= interval.getStart()) {
                    overlaps.add(currentInterval);
                }
            }
        }
        return overlaps;
    }

    /**
     * 方向
     */
    private enum Direction {
        LEFT, RIGHT
    }

}
