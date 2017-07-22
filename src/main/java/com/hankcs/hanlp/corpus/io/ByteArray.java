/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/10/30 14:33</create-date>
 *
 * <copyright file="ByteArray.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package com.hankcs.hanlp.corpus.io;

import com.hankcs.hanlp.log.HanLpLogger;
import com.hankcs.hanlp.utility.ByteUtil;


/**
 * 对字节数组进行封装，提供方便的读取操作
 *
 * @author hankcs
 */
public class ByteArray {
    /**
     * 当前字节数组，不一定是全部字节，可能只是一个片段
     */
    byte[] bytes;
    /**
     * 当前已读取的字节数，或下一个字节的指针
     */
    int offset;

    public ByteArray(byte[] bytes) {
        this.bytes = bytes;
    }

    /**
     * 获取全部字节
     */
    public byte[] getBytes() {
        return bytes;
    }

    /**
     * 读取一个int
     */
    public int nextInt() {
        int result = ByteUtil.bytesHighFirstToInt(bytes, offset);
        offset += 4;
        return result;
    }

    public double nextDouble() {
        double result = ByteUtil.bytesHighFirstToDouble(bytes, offset);
        offset += 8;
        return result;
    }

    /**
     * 读取一个char，对应于writeChar
     */
    public char nextChar() {
        char result = ByteUtil.bytesHighFirstToChar(bytes, offset);
        offset += 2;
        return result;
    }

    /**
     * 读取一个字节
     */
    public byte nextByte() {
        return bytes[offset++];
    }

    public boolean hasMore() {
        return offset < bytes.length;
    }

    /**
     * 读取一个String，注意这个String是双字节版的，在字符之前有一个整型表示长度
     */
    public String nextString() {
        char[] buffer = new char[nextInt()];
        for (int i = 0; i < buffer.length; ++i) {
            buffer[i] = nextChar();
        }
        return new String(buffer);
    }

    public float nextFloat() {
        float result = ByteUtil.bytesHighFirstToFloat(bytes, offset);
        offset += 4;
        return result;
    }

    /**
     * 读取一个无符号短整型
     */
    public int nextUnsignedShort() {
        byte a = nextByte();
        byte b = nextByte();
        return (((a & 0xff) << 8) | (b & 0xff));
    }

    /**
     * 读取一个UTF字符串
     */
    public String nextUTF() {
        int utfLength = nextUnsignedShort();
        byte[] byteArray = null;
        char[] charArray = null;
        byteArray = new byte[utfLength];
        charArray = new char[utfLength];

        int c, char2, char3;
        int count = 0;
        int chararr_count = 0;

        for (int i = 0; i < utfLength; ++i) {
            byteArray[i] = nextByte();
        }

        while (count < utfLength) {
            c = (int) byteArray[count] & 0xff;
            if (c > 127) break;
            count++;
            charArray[chararr_count++] = (char) c;
        }

        while (count < utfLength) {
            c = (int) byteArray[count] & 0xff;
            switch (c >> 4) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    /* 0xxxxxxx*/
                    count++;
                    charArray[chararr_count++] = (char) c;
                    break;
                case 12:
                case 13:
                    /* 110x xxxx   10xx xxxx*/
                    count += 2;
                    if (count > utfLength)
                        HanLpLogger.info(ByteArray.class,
                                "malformed input: partial character at end");
                    char2 = (int) byteArray[count - 1];
                    if ((char2 & 0xC0) != 0x80)
                        HanLpLogger.info(ByteArray.class,
                                "malformed input around byte " + count);
                    charArray[chararr_count++] = (char) (((c & 0x1F) << 6) |
                            (char2 & 0x3F));
                    break;
                case 14:
                    /* 1110 xxxx  10xx xxxx  10xx xxxx */
                    count += 3;
                    if (count > utfLength)
                        HanLpLogger.info(ByteArray.class,
                                "malformed input: partial character at end");
                    char2 = (int) byteArray[count - 2];
                    char3 = (int) byteArray[count - 1];
                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                        HanLpLogger.info(ByteArray.class,
                                "malformed input around byte " + (count - 1));
                    charArray[chararr_count++] = (char) (((c & 0x0F) << 12) |
                            ((char2 & 0x3F) << 6) |
                            ((char3 & 0x3F) << 0));
                    break;
                default:
                    /* 10xx xxxx,  1111 xxxx */
                    HanLpLogger.info(ByteArray.class,
                            "malformed input around byte " + count);
            }
        }
        // The number of chars produced may be less than utflen
        return new String(charArray, 0, chararr_count);
    }

    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return bytes.length;
    }

    /**
     * 通知执行关闭/销毁操作
     */
    public void close() {
        bytes = null;
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
}