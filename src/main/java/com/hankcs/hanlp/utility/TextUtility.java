package com.hankcs.hanlp.utility;


import java.io.*;
import java.util.Collection;

/**
 * 文本工具类
 */
public class TextUtility {

    /**
     * 单字节
     */
    public static final int CT_SINGLE = 5;// SINGLE byte

    /**
     * 分隔符"!,.?()[]{}+=
     */
    public static final int CT_DELIMITER = CT_SINGLE + 1;// delimiter

    /**
     * 中文字符
     */
    public static final int CT_CHINESE = CT_SINGLE + 2;// Chinese Char

    /**
     * 字母
     */
    public static final int CT_LETTER = CT_SINGLE + 3;// HanYu Pinyin

    /**
     * 数字
     */
    public static final int CT_NUMBER = CT_SINGLE + 4;// HanYu Pinyin

    /**
     * 序号
     */
    public static final int CT_INDEX = CT_SINGLE + 5;// HanYu Pinyin

    /**
     * 其他
     */
    public static final int CT_OTHER = CT_SINGLE + 12;// Other

    public static int charType(char c) {
        return charType(String.valueOf(c));
    }

    /**
     * 判断字符类型
     */
    public static int charType(String str) {
        if (str != null && str.length() > 0) {
            if ("零○〇一二两三四五六七八九十廿百千万亿壹贰叁肆伍陆柒捌玖拾佰仟".contains(str)) {
                return CT_NUMBER;
            }
            byte[] gbkByteArray;
            try {
                gbkByteArray = str.getBytes("GBK");
            }
            catch (UnsupportedEncodingException e) {
                gbkByteArray = str.getBytes();
            }
            byte b1 = gbkByteArray[0];
            byte b2 = gbkByteArray.length > 1 ? gbkByteArray[1] : 0;
            int ub1 = getUnsigned(b1);
            int ub2 = getUnsigned(b2);
            if (ub1 < 128) {
                if (' ' == b1) {
                    return CT_OTHER;
                }
                if ('\n' == b1) {
                    return CT_DELIMITER;
                }
                if ("*\"!,.?()[]{}+=/\\;:|".indexOf((char) b1) != -1) {
                    return CT_DELIMITER;
                }
                if ("0123456789".indexOf((char) b1) != -1) {
                    return CT_NUMBER;
                }
                return CT_SINGLE;
            }
            else if (ub1 == 162) {
                return CT_INDEX;
            }
            else if (ub1 == 163 && ub2 > 175 && ub2 < 186) {
                return CT_NUMBER;
            }
            else if (ub1 == 163 && (ub2 >= 193 && ub2 <= 218 || ub2 >= 225 && ub2 <= 250)) {
                return CT_LETTER;
            }
            else if (ub1 == 161 || ub1 == 163) {
                return CT_DELIMITER;
            }
            else if (ub1 >= 176 && ub1 <= 247) {
                return CT_CHINESE;
            }
        }
        return CT_OTHER;
    }

    /**
     * 是否全是中文
     */
    public static boolean isAllChinese(String str) {
        return str.matches("[\\u4E00-\\u9FA5]+");
    }

    /**
     * 是否全部不是中文
     */
    public static boolean isAllNonChinese(byte[] sString) {
        int nLen = sString.length;
        int i = 0;

        while (i < nLen) {
            if (getUnsigned(sString[i]) < 248 && getUnsigned(sString[i]) > 175)
                return false;
            if (sString[i] < 0)
                i += 2;
            else
                i += 1;
        }
        return true;
    }

    /**
     * 是否全是单字节
     */
    public static boolean isAllSingleByte(String str) {
        assert str != null;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) > 128) {
                return false;
            }
        }
        return true;
    }

    /**
     * 把表示数字含义的字符串转成整形
     *
     * @param str 要转换的字符串
     * @return 如果是有意义的整数，则返回此整数值。否则，返回-1。
     */
    public static int convertStringToInteger(String str) {
        if (str != null) {
            try {
                return Integer.parseInt(str);
            }
            catch (NumberFormatException e) {
                // ignore exception
            }
        }
        return -1;
    }

    /**
     * 是否全是数字
     */
    public static boolean isAllNum(String str) {

        if (str != null) {
            int i = 0;
            String temp = str + " ";
            // 判断开头是否是+-之类的符号
            if ("±+—-＋".contains(temp.substring(0, 1)))
                i++;
            /** 如果是全角的０１２３４５６７８９ 字符* */
            while (i < str.length() && "０１２３４５６７８９".contains(str.substring(i, i + 1)))
                i++;

            // Get middle delimiter such as .
            if (i < str.length()) {
                String s = str.substring(i, i + 1);
                if ("∶·．／".contains(s) || ".".equals(s) || "/".equals(s)) {// 98．1％
                    i++;
                    while (i + 1 < str.length() && "０１２３４５６７８９".contains(str.substring(i + 1, i + 2)))

                        i++;
                }
            }

            if (i >= str.length())
                return true;

            while (i < str.length() && convertStringToInteger(str.substring(i, i + 1)) >= 0
                    && convertStringToInteger(str.substring(i, i + 1)) <= 9)
                i++;
            // Get middle delimiter such as .
            if (i < str.length()) {
                String s = str.substring(i, i + 1);
                if ("∶·．／".contains(s) || ".".equals(s) || "/".equals(s)) {// 98．1％
                    i++;
                    while (i + 1 < str.length() && "0123456789".contains(str.substring(i + 1, i + 2)))
                        i++;
                }
            }

            if (i < str.length()) {

                if (!"百千万亿佰仟％‰".contains(str.substring(i, i + 1))
                        && !"%".equals(str.substring(i, i + 1)))
                    i--;
            }
            if (i >= str.length())
                return true;
        }
        return false;
    }

    /**
     * 是否全是序号
     */
    public static boolean isAllIndex(byte[] sString) {
        int nLen = sString.length;
        int i = 0;

        while (i < nLen - 1 && getUnsigned(sString[i]) == 162) {
            i += 2;
        }
        if (i >= nLen)
            return true;
        while (i < nLen && (sString[i] > 'A' - 1 && sString[i] < 'Z' + 1)
                || (sString[i] > 'a' - 1 && sString[i] < 'z' + 1)) {// single
            // byte
            // number
            // char
            i += 1;
        }

        return i >= nLen;

    }

    /**
     * 是否全为英文
     */
    public static boolean isAllLetter(String text) {
        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            if ((((c < 'a' || c > 'z')) && ((c < 'A' || c > 'Z')))) {
                return false;
            }
        }

        return true;
    }

    /**
     * 是否全为英文或字母
     */
    public static boolean isAllLetterOrNum(String text) {
        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            if ((((c < 'a' || c > 'z')) && ((c < 'A' || c > 'Z')) && ((c < '0' || c > '9')))) {
                return false;
            }
        }

        return true;
    }

    /**
     * 是否全是分隔符
     */
    public static boolean isAllDelimiter(byte[] sString) {
        int nLen = sString.length;
        int i = 0;

        while (i < nLen - 1 && (getUnsigned(sString[i]) == 161 || getUnsigned(sString[i]) == 163)) {
            i += 2;
        }
        return i >= nLen;
    }

    /**
     * 是否全是中国数字
     */
    public static boolean isAllChineseNum(String word) {
        // 百分之五点六的人早上八点十八分起床
        String chineseNum = "零○一二两三四五六七八九十廿百千万亿壹贰叁肆伍陆柒捌玖拾佰仟∶·．／点";//
        String prefix = "几数第上成";

        if (word != null) {
            String temp = word + " ";
            for (int i = 0; i < word.length(); i++) {
                // 百分之五
                if (temp.indexOf("分之", i) != -1) {
                    i += 2;
                    continue;
                }

                String tChar = temp.substring(i, i + 1);
                if (!chineseNum.contains(tChar) && (i != 0 || !prefix.contains(tChar))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }


    /**
     * 得到字符集的字符在字符串中出现的次数
     */
    public static int getCharCount(String charSet, String word) {
        int nCount = 0;

        if (word != null) {
            String temp = word + " ";
            for (int i = 0; i < word.length(); i++) {
                String s = temp.substring(i, i + 1);
                if (charSet.contains(s))
                    nCount++;
            }
        }

        return nCount;
    }


    /**
     * 获取字节对应的无符号整型数
     */
    public static int getUnsigned(byte b) {
        if (b > 0) {
            return (int) b;
        }
        else {
            return (b & 0x7F + 128);
        }
    }

    /**
     * 判断字符串是否是年份
     */
    public static boolean isYearTime(String strNum) {
        if (strNum != null) {
            int len = strNum.length();
            String first = strNum.substring(0, 1);

            // 1992年, 98年,06年
            if (isAllSingleByte(strNum)
                    && (len == 4 || len == 2 && (convertStringToInteger(first) > 4 || convertStringToInteger(first) == 0)))
                return true;
            if (isAllNum(strNum) && (len >= 6 || len == 4 && "０５６７８９".contains(first)))
                return true;
            if (getCharCount("零○一二三四五六七八九壹贰叁肆伍陆柒捌玖", strNum) == len && len >= 2)
                return true;
            if (len == 4 && getCharCount("千仟零○", strNum) == 2)// 二仟零二年
                return true;
            if (len == 1 && getCharCount("千仟", strNum) == 1)
                return true;
            if (len == 2 && getCharCount("甲乙丙丁戊己庚辛壬癸", strNum) == 1
                    && getCharCount("子丑寅卯辰巳午未申酉戌亥", strNum.substring(1)) == 1)
                return true;
        }
        return false;
    }

    /**
     * 判断一个字符串的所有字符是否在另一个字符串集合中
     *
     * @param aggStrings 字符串集合
     * @param str        需要判断的字符串
     */
    public static boolean isInAggregate(String aggStrings, String str) {
        if (aggStrings != null && str != null) {
            str += "1";
            for (int i = 0; i < str.length(); i++) {
                String s = str.substring(i, i + 1);
                if (!aggStrings.contains(s))
                    return false;
            }
            return true;
        }

        return false;
    }

    /**
     * 判断该字符串是否是半角字符
     */
    public static boolean isDBCCase(String str) {
        if (str != null) {
            str += " ";
            for (int i = 0; i < str.length(); i++) {
                String s = str.substring(i, i + 1);
                int length = 0;
                try {
                    length = s.getBytes("GBK").length;
                }
                catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    length = s.getBytes().length;
                }
                if (length != 1)
                    return false;
            }

            return true;
        }

        return false;
    }

    /**
     * 判断该字符串是否是全角字符
     */
    public static boolean isSBCCase(String str) {
        if (str != null) {
            str += " ";
            for (int i = 0; i < str.length(); i++) {
                String s = str.substring(i, i + 1);
                int length = 0;
                try {
                    length = s.getBytes("GBK").length;
                }
                catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    length = s.getBytes().length;
                }
                if (length != 2)
                    return false;
            }

            return true;
        }

        return false;
    }

    /**
     * 判断是否是一个连字符（分隔符）
     */
    public static boolean isDelimiter(String str) {
        return str != null && ("-".equals(str) || "－".equals(str));
    }

    public static boolean isUnknownWord(String word) {
        return word != null && word.indexOf("未##") == 0;
    }

    /**
     * 防止频率为0发生除零错误
     */
    public static double nonZero(double frequency) {
        if (frequency == 0) return 1e-3;

        return frequency;
    }

    /**
     * 转换long型为char数组
     */
    public static char[] long2char(long x) {
        char[] c = new char[4];
        c[0] = (char) (x >> 48);
        c[1] = (char) (x >> 32);
        c[2] = (char) (x >> 16);
        c[3] = (char) (x);
        return c;
    }

    /**
     * 转换long类型为string
     */
    public static String long2String(long x) {
        char[] cArray = long2char(x);
        StringBuilder sbResult = new StringBuilder(cArray.length);
        for (char c : cArray) {
            sbResult.append(c);
        }
        return sbResult.toString();
    }

    /**
     * 判断某个字符是否为汉字
     *
     * @param c 需要判断的字符
     * @return 是汉字返回true，否则返回false
     */
    public static boolean isChinese(char c) {
        String regex = "[\\u4e00-\\u9fa5]";
        return String.valueOf(c).matches(regex);
    }

    /**
     * 统计 keyword 在 srcText 中的出现次数
     */
    public static int count(String keyword, String srcText) {
        int count = 0;
        int srcTextLength = srcText.length();
        int j = 0;
        for (int i = 0; i < srcTextLength; i++) {
            if (srcText.charAt(i) == keyword.charAt(j)) {
                j++;
                if (j == keyword.length()) {
                    count++;
                    j = 0;
                }
            }
            else {
                i = i - j;// should rollback when not match
                j = 0;
            }
        }

        return count;
    }

    /**
     * 判断字符串是否为空（null和空格）
     */
    public static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static String join(String delimiter, Collection<String> stringCollection) {
        StringBuilder sb = new StringBuilder(stringCollection.size() * (16 + delimiter.length()));
        for (String str : stringCollection) {
            sb.append(str).append(delimiter);
        }

        return sb.toString();
    }
}
