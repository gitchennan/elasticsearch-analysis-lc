/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/12/5 15:37</create-date>
 *
 * <copyright file="CharType.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package com.hankcs.hanlp.dictionary.other;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.hankcs.hanlp.corpus.io.ByteArray;
import com.hankcs.hanlp.io.IOSafeHelper;
import com.hankcs.hanlp.log.HanLpLogger;
import com.hankcs.hanlp.utility.TextUtility;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 字符类型
 *
 * @author hankcs
 */
public class CharType {
    /**
     * 单字节
     */
    public static final byte CT_SINGLE = 5;

    /**
     * 分隔符"!,.?()[]{}+=
     */
    public static final byte CT_DELIMITER = CT_SINGLE + 1;

    /**
     * 中文字符
     */
    public static final byte CT_CHINESE = CT_SINGLE + 2;

    /**
     * 字母
     */
    public static final byte CT_LETTER = CT_SINGLE + 3;

    /**
     * 数字
     */
    public static final byte CT_NUM = CT_SINGLE + 4;

    /**
     * 序号
     */
    public static final byte CT_INDEX = CT_SINGLE + 5;

    /**
     * 其他
     */
    public static final byte CT_OTHER = CT_SINGLE + 12;

    public static byte[] type;

    static {
        type = new byte[65536];
        ByteArray byteArray = null;

        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            byteArray = generate();
        }
        catch (IOException e) {
            HanLpLogger.error(CharType.class,
                    String.format("Failed to load [CharType], takes %sms", stopwatch.elapsed(TimeUnit.MILLISECONDS)), e);
        }

        if (byteArray != null) {
            while (byteArray.hasMore()) {
                int b = byteArray.nextChar();
                int e = byteArray.nextChar();
                byte t = byteArray.nextByte();
                for (int i = b; i <= e; ++i) {
                    type[i] = t;
                }
            }

            HanLpLogger.info(CharType.class,
                    String.format("Load dictionary[%-25s], takes %s ms", "CharType", stopwatch.elapsed(TimeUnit.MILLISECONDS)));
        }
    }

    private static ByteArray generate() throws IOException {
        int preType = 5;
        int preChar = 0;
        List<int[]> typeList = Lists.newLinkedList();
        for (int i = 0; i <= Character.MAX_VALUE; ++i) {
            int type = TextUtility.charType((char) i);
            if (type != preType) {
                int[] array = new int[3];
                array[0] = preChar;
                array[1] = i - 1;
                array[2] = preType;
                typeList.add(array);
                preChar = i;
            }
            preType = type;
        }

        int[] array = new int[3];
        array[0] = preChar;
        array[1] = (int) Character.MAX_VALUE;
        array[2] = preType;
        typeList.add(array);


        ByteArrayOutputStream byteArrayOut = null;
        try {
            byteArrayOut = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(byteArrayOut);
            for (int[] tArray : typeList) {
                out.writeChar(tArray[0]);
                out.writeChar(tArray[1]);
                out.writeByte(tArray[2]);
            }
            out.flush();
            byteArrayOut.flush();
            return new ByteArray(byteArrayOut.toByteArray());
        }
        catch (Exception ex) {
            HanLpLogger.error(CharType.class, "Failed to read charType file", ex);
        }
        finally {
            IOSafeHelper.safeClose(byteArrayOut);
        }
        return null;
    }

    /**
     * 获取字符的类型
     */
    public static byte get(char c) {
        return type[(int) c];
    }
}
