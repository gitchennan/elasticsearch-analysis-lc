package com.hankcs.hanlp.io;

import java.io.OutputStream;

public interface OutputStreamCreator {
    OutputStream create() throws Exception;
}
