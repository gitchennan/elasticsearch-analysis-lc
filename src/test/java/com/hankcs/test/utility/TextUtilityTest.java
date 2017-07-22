package com.hankcs.test.utility;


import com.hankcs.hanlp.utility.TextUtility;
import org.junit.Assert;
import org.junit.Test;

public class TextUtilityTest {
    @Test
    public void test_charType() {
        Assert.assertEquals(TextUtility.CT_CHINESE, TextUtility.charType('我'));
        Assert.assertEquals(TextUtility.CT_DELIMITER, TextUtility.charType('{'));
        Assert.assertEquals(TextUtility.CT_NUMBER, TextUtility.charType('1'));
        Assert.assertEquals(TextUtility.CT_NUMBER, TextUtility.charType('一'));
    }
}
