package com.hankcs.test.pinyin;

import com.hankcs.hanlp.api.HanLP;
import org.junit.Test;

public class PinyinTest {
    @Test
    public void test_convertToPinyin() {
        System.out.println(HanLP.convertToPinyinString("重庆大轰炸", "|", true));
        System.out.println(HanLP.convertToPinyinString("重", "|", true));
        System.out.println(HanLP.convertToPinyinString("重金所", "|", true));
        System.out.println(HanLP.convertToPinyinString("陆金所", "|", true));
    }
}
