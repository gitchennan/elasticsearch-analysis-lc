/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/9/10 15:39</create-date>
 *
 * <copyright file="NSDictionary.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.liNSunsoft.com/
 * This source is subject to the LiNSunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package com.hankcs.hanlp.dictionary.ns;


import com.hankcs.hanlp.corpus.dictionary.item.EnumItem;
import com.hankcs.hanlp.corpus.tag.NS;
import com.hankcs.hanlp.dictionary.common.CommonDictionary;
import com.hankcs.hanlp.io.IOSafeHelper;
import com.hankcs.hanlp.io.InputStreamOperator;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 一个好用的地名词典
 *
 * @author hankcs
 */
public class NSDictionary extends CommonDictionary<EnumItem<NS>> {
    @Override
    @SuppressWarnings("unchecked")
    protected EnumItem<NS>[] doLoadDictionary(String path) {
        List<EnumItem<NS>> valueList = new LinkedList<EnumItem<NS>>();

        IOSafeHelper.openAutoCloseableFileInputStream(path, new InputStreamOperator() {
            @Override
            public void process(InputStream input) throws Exception {
                BufferedReader br = new BufferedReader(new InputStreamReader(input, "UTF-8"));
                String line;
                while ((line = br.readLine()) != null) {
                    Map.Entry<String, Map.Entry<String, Integer>[]> args = EnumItem.create(line);
                    EnumItem<NS> NSEnumItem = new EnumItem<NS>();
                    for (Map.Entry<String, Integer> e : args.getValue()) {
                        NSEnumItem.labelMap.put(NS.valueOf(e.getKey()), e.getValue());
                    }
                    valueList.add(NSEnumItem);
                }
            }
        });
        return valueList.toArray(new EnumItem[valueList.size()]);
    }
}
