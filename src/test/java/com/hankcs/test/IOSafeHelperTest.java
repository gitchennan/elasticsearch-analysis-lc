package com.hankcs.test;

import com.hankcs.hanlp.io.IOSafeHelper;
import com.hankcs.hanlp.io.LineOperator;
import org.junit.Test;

public class IOSafeHelperTest {
    @Test
    public void test_openAutoCloseableFileInputStream() {
        String filePath = "config/data/dictionary/custom/CustomDictionary.txt";
        IOSafeHelper.openAutoCloseableFileReader(filePath, new LineOperator() {
            private int count = 0;

            @Override
            public void process(String line) throws Exception {
                if (count++ < 100) {
                    System.out.println(line);
                }
            }
        });
    }
}
