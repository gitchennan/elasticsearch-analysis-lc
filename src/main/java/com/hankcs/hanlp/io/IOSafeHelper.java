package com.hankcs.hanlp.io;

import com.hankcs.hanlp.log.HanLpLogger;

import java.io.*;
import java.security.AccessController;
import java.security.PrivilegedAction;

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
            IOSafeHelper.safeClose(bos);
        }
    }

    public static boolean openAutoCloseableFileReader(String filePath, LineOperator lineOperator) {
        return openAutoCloseableFileReader(filePath, lineOperator, "UTF-8");
    }

    public static boolean openAutoCloseableFileReader(String filePath, LineOperator lineOperator, String encoding) {
        return openAutoCloseableFileInputStream(filePath, new InputStreamOperator() {
            @Override
            public void process(InputStream input) throws Exception {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input, encoding));
                String line;
                while (null != (line = bufferedReader.readLine())) {
                    if (line.trim().length() == 0) {
                        continue;
                    }
                    lineOperator.process(line);
                }
            }
        });
    }

    public static boolean openAutoCloseableInputStream(InputStreamCreator creator, InputStreamOperator operator) {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            @Override
            public Boolean run() {
                InputStream inputStream = null;
                try {
                    inputStream = creator.create();
                    operator.process(inputStream);
                    return true;
                }
                catch (Throwable t) {
                    operator.onError(t);
                }
                finally {
                    safeClose(inputStream);
                }
                return false;
            }
        });
    }

    public static boolean openAutoCloseableOutputStream(OutputStreamCreator creator, OutputStreamOperator operator) {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            @Override
            public Boolean run() {
                OutputStream outputStream = null;
                try {
                    outputStream = creator.create();
                    operator.process(outputStream);
                    return true;
                }
                catch (Throwable t) {
                    operator.onError(t);
                }
                finally {
                    safeClose(outputStream);
                }
                return false;
            }
        });
    }

    public static boolean openAutoCloseableFileInputStream(String filePath, InputStreamOperator operator) {
        return openAutoCloseableInputStream(new InputStreamCreator() {
            @Override
            public InputStream create() throws IOException {
                HanLpLogger.debug(IOSafeHelper.class,
                        String.format("[open-input] Open file inputStream, path[%s]", filePath));

                return new FileInputStream(filePath);
            }
        }, operator);
    }

    public static boolean openAutoCloseableFileOutputStream(String filePath, OutputStreamOperator operator) {
        return openAutoCloseableOutputStream(new OutputStreamCreator() {
            @Override
            public OutputStream create() throws IOException {
                HanLpLogger.debug(IOSafeHelper.class,
                        String.format("[open-output] Open file outputStream, path[%s]", filePath));

                return new FileOutputStream(filePath);
            }
        }, operator);
    }


    public static void safeClose(Closeable closeable) {
        if (closeable != null) {
            try {
                HanLpLogger.debug(IOSafeHelper.class,
                        String.format("[safe-close] Safe close object[%s]", closeable.getClass()));

                closeable.close();
            }
            catch (Exception ex) {
                HanLpLogger.error(IOSafeHelper.class,
                        String.format("[safe-close] Failed to close object[%s]", closeable.getClass()), ex);
            }
        }
    }
}
