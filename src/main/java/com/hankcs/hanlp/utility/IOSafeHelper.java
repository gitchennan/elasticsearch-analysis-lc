package com.hankcs.hanlp.utility;

import com.hankcs.hanlp.HanLP;

import java.io.*;

public class IOSafeHelper {
    private IOSafeHelper() {

    }

    public static FileInputStream openFileInputStream(String filePath) throws IOException {
        FileInputStream in = null;
        try {
            in = new FileInputStream(filePath);
        }
        catch (Exception ex) {
            closeInputStream(in);
            throw ex;
        }
        return in;
    }

    public static FileOutputStream openFileOutputStream(String filePath) throws IOException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filePath);
        }
        catch (Exception ex) {
            closeOutputStream(out);
            throw ex;
        }
        return out;
    }

    public static InputStream openInputStream(String filePath) throws IOException {
        InputStream in = null;
        try {
            if (HanLP.Config.IOAdapter == null) {
                in = new FileInputStream(filePath);
            }
            else {
                in = HanLP.Config.IOAdapter.open(filePath);
            }
        }
        catch (Exception ex) {
            closeInputStream(in);
            throw ex;
        }
        return in;
    }

    public static void closeInputStream(InputStream in) {
        try {
            if (in != null) {
                in.close();
            }
        }
        catch (Exception ex) {
            // ignore
        }
    }

    public static void closeOutputStream(OutputStream out) {
        try {
            if (out != null) {
                out.close();
            }
        }
        catch (Exception ex) {
            // ignore
        }
    }

    public static void closeReader(Reader reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        }
        catch (Exception ex) {
            // ignore
        }
    }

    public static void closeWriter(Writer writer) {
        try {
            if (writer != null) {
                writer.close();
            }
        }
        catch (Exception ex) {
            // ignore
        }
    }

}
