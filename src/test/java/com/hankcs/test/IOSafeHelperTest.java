package com.hankcs.test;

import com.hankcs.hanlp.io.IOSafeHelper;
import com.hankcs.hanlp.io.InputStreamOperator;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class IOSafeHelperTest {
    @Test
    public void test_openAutoCloseableFileInputStream() {
        String filePath = "data/dictionary/CoreNatureDictionary.txt";
        IOSafeHelper.openAutoCloseableFileInputStream(filePath, new InputStreamOperator() {
            @Override
            public void process(InputStream input) throws IOException {
                BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
                String line = null;
                while (null != (line = reader.readLine())) {
                    System.out.println(line);
                }
                reader.close();
            }
        });
    }
}
