package com.hankcs.hanlp.io;

import com.hankcs.hanlp.log.HanLpLogger;

import java.io.*;

public class IOSafeHelper {

    private IOSafeHelper() {

    }

    public static byte[] readBytes(String path) {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        openAutoCloseableFileInputStream(path, new InputStreamOperator() {
            @Override
            public void process(InputStream input) throws IOException {
                int readBytes = 0;
                byte[] buffer = new byte[4 * 1024];
                while ((readBytes = input.read(buffer, 0, buffer.length)) != -1) {
                    bos.write(buffer, 0, readBytes);
                }
                bos.flush();
            }

            @Override
            public void onError(Throwable t) {
                try {
                    bos.close();
                }
                catch (Exception ex) {
                    // ignore
                }
            }
        });

        try {
            return bos.toByteArray();
        }
        finally {
            try {
                bos.close();
            }
            catch (Exception ex) {
                // ignore
            }
        }
    }

    public static void openAutoCloseableInputStream(InputStreamCreator creator, InputStreamOperator operator) {
        InputStream inputStream = null;
        try {
            inputStream = creator.create();
            operator.process(inputStream);
        }
        catch (Throwable t) {
            operator.onError(t);
        }
        finally {
            safeClose(inputStream);
        }
    }

    public static void openAutoCloseableOutputStream(OutputStreamCreator creator, OutputStreamOperator operator) {
        OutputStream outputStream = null;
        try {
            outputStream = creator.create();
            operator.process(outputStream);
        }
        catch (Throwable t) {
            operator.onError(t);
        }
        finally {
            safeClose(outputStream);
        }
    }

    public static void openAutoCloseableFileInputStream(String filePath, InputStreamOperator operator) {
        openAutoCloseableInputStream(new InputStreamCreator() {
            @Override
            public InputStream create() throws IOException {
                return new FileInputStream(filePath);
            }
        }, operator);
    }

    public static void openAutoCloseableFileOutputStream(String filePath, OutputStreamOperator operator) {
        openAutoCloseableOutputStream(new OutputStreamCreator() {
            @Override
            public OutputStream create() throws IOException {
                return new FileOutputStream(filePath);
            }
        }, operator);
    }


    public static void safeClose(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            }
            catch (Exception ex) {
                HanLpLogger.error(IOSafeHelper.class,
                        String.format("[safeClose] Failed to close object[%s]", closeable.getClass()), ex);
            }
        }
    }
}
