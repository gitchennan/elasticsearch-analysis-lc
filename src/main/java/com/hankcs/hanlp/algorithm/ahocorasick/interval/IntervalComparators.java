package com.hankcs.hanlp.algorithm.ahocorasick.interval;

import java.util.Comparator;

public class IntervalComparators {

    public static Comparator<Interval> newBasedOnPositionComparator() {
        return new Comparator<Interval>() {
            @Override
            public int compare(Interval intervalOne, Interval intervalTwo) {
                if (intervalOne.getStart() == intervalTwo.getStart()) {
                    return 0;
                }
                return (intervalOne.getStart() - intervalTwo.getStart()) > 0 ? 1 : -1;
            }
        };
    }

    public static Comparator<Interval> newBasedOnSizeComparator() {
        return new Comparator<Interval>() {
            @Override
            public int compare(Interval intervalOne, Interval intervalTwo) {
                int comparison = intervalTwo.size() - intervalOne.size();
                if (comparison == 0) {
                    comparison = intervalOne.getStart() - intervalTwo.getStart();
                }
                return comparison;
            }
        };
    }
}
