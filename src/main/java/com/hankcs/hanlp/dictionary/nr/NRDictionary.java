/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/9/10 15:39</create-date>
 *
 * <copyright file="NRDictionary.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package com.hankcs.hanlp.dictionary.nr;


import com.hankcs.hanlp.corpus.dictionary.item.EnumItem;
import com.hankcs.hanlp.corpus.tag.NR;
import com.hankcs.hanlp.dictionary.common.CommonDictionary;
import com.hankcs.hanlp.log.HanLpLogger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * 一个好用的人名词典
 *
 * @author hankcs
 */
public class NRDictionary extends CommonDictionary<EnumItem<NR>> {
    @Override
    @SuppressWarnings("unchecked")
    protected EnumItem<NR>[] doLoadDictionary(String path) {
        List<EnumItem<NR>> valueList = new LinkedList<EnumItem<NR>>();
        String line = null;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
            while ((line = br.readLine()) != null) {
                Map.Entry<String, Map.Entry<String, Integer>[]> args = EnumItem.create(line);
                EnumItem<NR> nrEnumItem = new EnumItem<NR>();
                for (Map.Entry<String, Integer> e : args.getValue()) {
                    nrEnumItem.labelMap.put(NR.valueOf(e.getKey()), e.getValue());
                }
                valueList.add(nrEnumItem);
            }
            br.close();
        }
        catch (Exception e) {
            HanLpLogger.error(NRDictionary.class, "读取" + path + "失败[" + e + "]\n该词典这一行格式不对：" + line);
            return null;
        }
        return valueList.toArray(new EnumItem[0]);
    }

}
