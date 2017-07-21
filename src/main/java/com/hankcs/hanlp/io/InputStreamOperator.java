package com.hankcs.hanlp.io;

import com.hankcs.hanlp.log.HanLpLogger;

import java.io.InputStream;

public abstract class InputStreamOperator {

    public abstract void process(InputStream input) throws Exception;

    public void onError(Throwable t) {
        HanLpLogger.error(this, "The error occurred while processing input stream", t);
    }

    public void onFinish() {

    }
}
