package com.hankcs.hanlp.io;

import com.hankcs.hanlp.log.HanLpLogger;

public abstract class LineOperator {

    public abstract void process(String line) throws Exception;

    public void onError(Throwable t) {
        HanLpLogger.error(this, "The error occurred while processing line reader", t);
    }

    public void onFinish() {

    }
}
