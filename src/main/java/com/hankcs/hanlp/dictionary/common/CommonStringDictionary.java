/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/11/18 17:16</create-date>
 *
 * <copyright file="CommonStringDictionary.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package com.hankcs.hanlp.dictionary.common;


import com.hankcs.hanlp.collection.trie.bintrie.BinTrie;
import com.hankcs.hanlp.io.IOSafeHelper;
import com.hankcs.hanlp.io.InputStreamOperator;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 最简单的词典，每一行只有一个词，没别的
 *
 * @author hankcs
 */
public class CommonStringDictionary {
    BinTrie<Byte> trie;

    public boolean load(String path) {
        trie = new BinTrie<Byte>();

        return IOSafeHelper.openAutoCloseableFileInputStream(path, new InputStreamOperator() {
            @Override
            public void process(InputStream input) throws Exception {
                String line;
                BufferedReader bw = new BufferedReader(new InputStreamReader(input, "UTF-8"));
                while ((line = bw.readLine()) != null) {
                    trie.put(line, null);
                }
            }
        });
    }


    public Set<String> keySet() {
        Set<String> keySet = new LinkedHashSet<String>();
        for (Map.Entry<String, Byte> entry : trie.entrySet()) {
            keySet.add(entry.getKey());
        }

        return keySet;
    }
}
