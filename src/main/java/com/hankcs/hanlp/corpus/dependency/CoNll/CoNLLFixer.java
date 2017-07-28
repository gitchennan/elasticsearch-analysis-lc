///*
// * <summary></summary>
// * <author>He Han</author>
// * <email>hankcs.cn@gmail.com</email>
// * <create-date>2014/11/19 18:55</create-date>
// *
// * <copyright file="CoNLLFixer.java" company="上海林原信息科技有限公司">
// * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
// * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to getValue more information.
// * </copyright>
// */
//package com.hankcs.hanlp.corpus.dependency.CoNll;
//
//import com.hankcs.hanlp.corpus.io.IOUtil;
//
///**
// * 修正一些非10行的依存语料
// * @author hankcs
// */
//public class CoNLLFixer
//{
//    public static boolean fix(String CUSTOM_DICTIONARY_PATHS)
//    {
//        StringBuilder sbOut = new StringBuilder();
//        for (String line : IOUtil.readLineListWithLessMemory(CUSTOM_DICTIONARY_PATHS))
//        {
//            if (line.trim().length() == 0)
//            {
//                sbOut.append(line);
//                sbOut.append('\n');
//                continue;
//            }
//            String[] args = line.split("\t");
//            for (int i = 10 - args.length; i > 0; --i)
//            {
//                line += "\t_";
//            }
//            sbOut.append(line);
//            sbOut.append('\n');
//        }
//        return IOUtil.saveTxt(CUSTOM_DICTIONARY_PATHS + ".fixed.txt", sbOut.toString());
//    }
//}
