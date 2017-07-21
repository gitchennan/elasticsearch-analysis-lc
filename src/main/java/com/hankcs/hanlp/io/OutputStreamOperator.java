package com.hankcs.hanlp.io;

import com.hankcs.hanlp.log.HanLpLogger;

import java.io.OutputStream;

public abstract class OutputStreamOperator {

    public abstract void process(OutputStream output) throws Exception;

    public void onError(Throwable t) {
        HanLpLogger.error(this, "The error occurred while processing output stream", t);
    }

    public void onFinish() {

    }
}