package com.hankcs.hanlp.algorithm.ahocorasick.interval;


public class Interval implements Comparable<Interval> {

    private int start;

    private int end;

    public Interval(final int start, final int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return this.start;
    }

    public int getEnd() {
        return this.end;
    }

    public int size() {
        return end - start + 1;
    }

    /**
     * whether current interval overlaps with another
     *
     * @param otherInterval another one
     * @return true or false
     */
    public boolean overlapsWith(Interval otherInterval) {
        return this.start <= otherInterval.getEnd() &&
                this.end >= otherInterval.getStart();
    }

    /**
     * whether current interval cover the given point
     *
     * @param point the point
     * @return true or false
     */
    public boolean overlapsWith(int point) {
        return this.start <= point && point <= this.end;
    }

    @Override
    public boolean equals(Object otherInterval) {
        if (!(otherInterval instanceof Interval)) {
            return false;
        }
        Interval other = (Interval) otherInterval;
        return this.start == other.getStart() && this.end == other.getEnd();
    }

    @Override
    public int hashCode() {
        return this.start % 100 + this.end % 100;
    }

    @Override
    public int compareTo(Interval other) {
        int comparison = this.start - other.getStart();
        return comparison != 0 ? comparison : this.end - other.getEnd();
    }

    @Override
    public String toString() {
        return "[interval] start=" + this.start + ", end=" + this.end;
    }
}
