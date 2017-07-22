package com.hankcs.hanlp.dictionary;

import com.google.common.base.Stopwatch;
import com.hankcs.hanlp.io.IOSafeHelper;
import com.hankcs.hanlp.io.LineOperator;
import com.hankcs.hanlp.log.HanLpLogger;

import java.util.concurrent.TimeUnit;

public abstract class FileSystemDictionary implements Dictionary {

    private String[] dictionaryLocations;

    public FileSystemDictionary(String... dictionaryLocations) {
        this.dictionaryLocations = dictionaryLocations;
    }

    abstract void loadLine(String line);

    void onDictionaryLoaded() {

    }

    @Override
    public final synchronized void load() {
        Stopwatch stopwatch = Stopwatch.createUnstarted();
        for (String dictionaryLocation : dictionaryLocations) {
            stopwatch.start();
            boolean loadResult = IOSafeHelper.openAutoCloseableFileReader(dictionaryLocation, new LineOperator() {
                @Override
                public void process(String line) throws Exception {
                    loadLine(line);
                }

                @Override
                public void onError(Throwable t) {
                    HanLpLogger.error(this,
                            String.format("Failed to load dictionary, path[%s]", dictionaryLocation), t);
                }
            });
            stopwatch.stop();

            if (loadResult) {
                HanLpLogger.info(this,
                        String.format("Load dictionary, takes %sms, path[%s] ",
                                stopwatch.elapsed(TimeUnit.MILLISECONDS), dictionaryLocation));
            }
            stopwatch.reset();
        }

        onDictionaryLoaded();
    }

    @Override
    public final synchronized void reLoad() {
        try {
            HanLpLogger.info(this,
                    String.format("Begin to reload dictionary[%s]", dictionaryName()));
            releaseResource();
        }
        catch (Exception ex) {
            HanLpLogger.error(this,
                    String.format("Error occurred while releasing resource, ignore reload dictionary[%S]", dictionaryName()));
            return;
        }
        load();

        HanLpLogger.info(this,
                String.format("Finish reload dictionary[%s]", dictionaryName()));
    }
}