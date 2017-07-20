package com.hankcs.hanlp.algorithm.ahocorasick.trie;

/**
 * 配置
 */
public class TrieConfig {
    /**
     * 只保留最长匹配
     */
    public boolean remainLongest = false;
    /**
     * 允许重叠
     */
    private boolean allowOverlaps = true;

    /**
     * 是否允许重叠
     */
    public boolean isAllowOverlaps() {
        return allowOverlaps;
    }

    /**
     * 设置是否允许重叠
     */
    public void setAllowOverlaps(boolean allowOverlaps) {
        this.allowOverlaps = allowOverlaps;
    }
}
