/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/5/3 11:34</create-date>
 *
 * <copyright file="BinTrie.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package com.hankcs.hanlp.collection.trie.bintrie;

import com.hankcs.hanlp.collection.trie.ITrie;
import com.hankcs.hanlp.corpus.io.ByteArray;
import com.hankcs.hanlp.io.IOSafeHelper;

import java.util.*;

/**
 * 首字直接分配内存，之后二分动态数组的Trie树，能够平衡时间和空间
 *
 * @author hankcs
 */
public class BinTrie<V> extends BaseNode<V> implements ITrie<V> {
    private int size;

    public BinTrie() {
        child = new BaseNode[65535 + 1];    // (int)Character.MAX_VALUE
        size = 0;
        status = Status.NOT_WORD_1;
    }

    public static <ValueType> BinTrie<ValueType> newBinTrie() {
        return new BinTrie<ValueType>();
    }


    /**
     * 插入一个词
     */
    public void put(String key, V value) {
        if (key.length() == 0) return;  // 安全起见
        BaseNode branch = this;
        char[] chars = key.toCharArray();
        for (int i = 0; i < chars.length - 1; ++i) {
            // 除了最后一个字外，都是继续
            branch.addChild(new Node<V>(chars[i], Status.NOT_WORD_1, null));
            branch = branch.getChild(chars[i]);
        }
        // 最后一个字加入时属性为end
        if (branch.addChild(new Node<V>(chars[chars.length - 1], Status.WORD_END_3, value))) {
            ++size; // 维护size
        }
    }

    public void put(char[] key, V value) {
        BaseNode branch = this;
        for (int i = 0; i < key.length - 1; ++i) {
            // 除了最后一个字外，都是继续
            branch.addChild(new Node<V>(key[i], Status.NOT_WORD_1, null));
            branch = branch.getChild(key[i]);
        }
        // 最后一个字加入时属性为end
        if (branch.addChild(new Node<V>(key[key.length - 1], Status.WORD_END_3, value))) {
            ++size; // 维护size
        }
    }

    /**
     * 设置键值对，当键不存在的时候会自动插入
     *
     * @param key
     * @param value
     */
    public void set(String key, V value) {
        put(key.toCharArray(), value);
    }

    /**
     * 删除一个词
     *
     * @param key
     */
    public void remove(String key) {
        BaseNode branch = this;
        char[] chars = key.toCharArray();
        for (int i = 0; i < chars.length - 1; ++i) {
            if (branch == null) return;
            branch = branch.getChild(chars[i]);
        }
        // 最后一个字设为undefined
        if (branch.addChild(new Node<V>(chars[chars.length - 1], Status.UNDEFINED_0, value))) {
            --size;
        }
    }

    public boolean containsKey(String key) {
        BaseNode branch = this;
        char[] chars = key.toCharArray();
        for (char aChar : chars) {
            if (branch == null) return false;
            branch = branch.getChild(aChar);
        }

        return branch != null && (branch.status == Status.WORD_END_3 || branch.status == Status.WORD_MIDDLE_2);
    }

    public V get(String key) {
        BaseNode<V> branch = this;
        char[] chars = key.toCharArray();
        for (char aChar : chars) {
            if (branch == null) return null;
            branch = branch.getChild(aChar);
        }

        if (branch == null) return null;
        // 下面这句可以保证只有成词的节点被返回
        if (!(branch.status == Status.WORD_END_3 || branch.status == Status.WORD_MIDDLE_2)) return null;
        return (V) branch.getValue();
    }

    public V get(char[] key) {
        BaseNode<V> branch = this;
        for (char aChar : key) {
            if (branch == null) return null;
            branch = branch.getChild(aChar);
        }

        if (branch == null) return null;
        // 下面这句可以保证只有成词的节点被返回
        if (!(branch.status == Status.WORD_END_3 || branch.status == Status.WORD_MIDDLE_2)) return null;
        return (V) branch.getValue();
    }

    /**
     * 获取键值对集合
     */
    public Set<Map.Entry<String, V>> entrySet() {
        Set<Map.Entry<String, V>> entrySet = new TreeSet<Map.Entry<String, V>>();
        for (BaseNode<V> node : child) {
            if (node == null) {
                continue;
            }
            node.walk(new StringBuilder(), entrySet);
        }
        return entrySet;
    }

    /**
     * 键集合
     *
     * @return
     */
    public Set<String> keySet() {
        TreeSet<String> keySet = new TreeSet<String>();
        for (Map.Entry<String, V> entry : entrySet()) {
            keySet.add(entry.getKey());
        }

        return keySet;
    }

    /**
     * 前缀查询
     *
     * @param key 查询串
     * @return 键值对
     */
    public Set<Map.Entry<String, V>> prefixSearch(String key) {
        Set<Map.Entry<String, V>> entrySet = new TreeSet<Map.Entry<String, V>>();
        StringBuilder sb = new StringBuilder(key.substring(0, key.length() - 1));
        BaseNode branch = this;
        char[] chars = key.toCharArray();
        for (char aChar : chars) {
            if (branch == null) return entrySet;
            branch = branch.getChild(aChar);
        }

        if (branch == null) return entrySet;
        branch.walk(sb, entrySet);
        return entrySet;
    }

    /**
     * 前缀查询，包含值
     *
     * @param key 键
     * @return 键值对列表
     */
    public LinkedList<Map.Entry<String, V>> commonPrefixSearchWithValue(String key) {
        char[] chars = key.toCharArray();
        return commonPrefixSearchWithValue(chars, 0);
    }

    /**
     * 前缀查询，通过字符数组来表示字符串可以优化运行速度
     *
     * @param chars 字符串的字符数组
     * @param begin 开始的下标
     * @return
     */
    public LinkedList<Map.Entry<String, V>> commonPrefixSearchWithValue(char[] chars, int begin) {
        LinkedList<Map.Entry<String, V>> result = new LinkedList<Map.Entry<String, V>>();
        StringBuilder sb = new StringBuilder();
        BaseNode branch = this;
        for (int i = begin; i < chars.length; ++i) {
            char aChar = chars[i];
            branch = branch.getChild(aChar);
            if (branch == null || branch.status == Status.UNDEFINED_0) return result;
            sb.append(aChar);
            if (branch.status == Status.WORD_MIDDLE_2 || branch.status == Status.WORD_END_3) {
                result.add(new AbstractMap.SimpleEntry<String, V>(sb.toString(), (V) branch.value));
            }
        }

        return result;
    }

    @Override
    protected boolean addChild(BaseNode node) {
        boolean add = false;
        char c = node.getChar();
        BaseNode target = getChild(c);
        if (target == null) {
            child[c] = node;
            add = true;
        }
        else {
            switch (node.status) {
                case UNDEFINED_0:
                    if (target.status != Status.NOT_WORD_1) {
                        target.status = Status.NOT_WORD_1;
                        add = true;
                    }
                    break;
                case NOT_WORD_1:
                    if (target.status == Status.WORD_END_3) {
                        target.status = Status.WORD_MIDDLE_2;
                    }
                    break;
                case WORD_END_3:
                    if (target.status == Status.NOT_WORD_1) {
                        target.status = Status.WORD_MIDDLE_2;
                    }
                    if (target.getValue() == null) {
                        add = true;
                    }
                    target.setValue(node.getValue());
                    break;
            }
        }
        return add;
    }

    public int size() {
        return size;
    }

    @Override
    protected char getChar() {
        return 0;   // 根节点没有char
    }

    @Override
    public BaseNode getChild(char c) {
        return child[c];
    }

    @Override
    public int build(TreeMap<String, V> keyValueMap) {
        for (Map.Entry<String, V> entry : keyValueMap.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
        return 0;
    }

    /**
     * 从磁盘加载二分数组树
     *
     * @param path  路径
     * @param value 额外提供的值数组，按照值的字典序。（之所以要求提供它，是因为泛型的保存不归树管理）
     * @return 是否成功
     */
    public boolean load(String path, V[] value) {
        byte[] bytes = IOSafeHelper.readBytes(path);
        if (bytes == null) {
            return false;
        }
        _ValueArray valueArray = new _ValueArray(value);
        ByteArray byteArray = new ByteArray(bytes);
        for (int i = 0; i < child.length; ++i) {
            int flag = byteArray.nextInt();
            if (flag == 1) {
                child[i] = new Node<V>();
                child[i].walkToLoad(byteArray, valueArray);
            }
        }
        size = value.length;

        return true;
    }

    /**
     * 只加载值，此时相当于一个set
     *
     * @param path
     * @return
     */
    public boolean load(String path) {
        byte[] bytes = IOSafeHelper.readBytes(path);
        if (bytes == null) {
            return false;
        }
        _ValueArray valueArray = new _EmptyValueArray();
        ByteArray byteArray = new ByteArray(bytes);
        for (int i = 0; i < child.length; ++i) {
            int flag = byteArray.nextInt();
            if (flag == 1) {
                child[i] = new Node<V>();
                child[i].walkToLoad(byteArray, valueArray);
            }
        }
        size = -1;  // 不知道有多少

        return true;
    }

    public boolean load(ByteArray byteArray, _ValueArray valueArray) {
        for (int i = 0; i < child.length; ++i) {
            int flag = byteArray.nextInt();
            if (flag == 1) {
                child[i] = new Node<V>();
                child[i].walkToLoad(byteArray, valueArray);
            }
        }
        size = valueArray.value.length;

        return true;
    }

    public boolean load(ByteArray byteArray, V[] value) {
        return load(byteArray, newValueArray().setValue(value));
    }

    public _ValueArray newValueArray() {
        return new _ValueArray();
    }
}
