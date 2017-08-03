package com.hankcs.hanlp.dictionary;

import com.google.common.base.Stopwatch;
import com.hankcs.hanlp.io.IOSafeHelper;
import com.hankcs.hanlp.io.LineOperator;
import com.hankcs.hanlp.log.HanLpLogger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class FileSystemTxtDictionary implements Dictionary {

    protected static final String DICT_LINE_SPLIT_CHAR = "\\s";

    private String[] dictionaryLocations;

    public FileSystemTxtDictionary(String... dictionaryLocations) {
        this.dictionaryLocations = dictionaryLocations;
    }

    protected abstract void onLoadLine(String line);

    protected void onDictionaryLoaded() {

    }

    @Override
    public final synchronized void load() {
        Stopwatch stopwatch = Stopwatch.createUnstarted();
        for (String dictionaryLocation : dictionaryLocations) {
            stopwatch.start();
            AtomicInteger wordCount = new AtomicInteger(0);
            boolean loadResult = IOSafeHelper.openAutoCloseableFileReader(dictionaryLocation, new LineOperator() {
                @Override
                public void process(String line) throws Exception {
                    onLoadLine(line);
                    wordCount.incrementAndGet();
                }

                @Override
                public void onError(Throwable t) {
                    HanLpLogger.error(this,
                            String.format("Failed to load dictionary[%s], path[%s]", dictionaryName(), dictionaryLocation), t);
                }
            });
            stopwatch.stop();

            if (loadResult) {
                HanLpLogger.info(this,
                        String.format("Load dictionary[%s], takes %s ms, total_words[%d] path[%s] ",
                                dictionaryName(), stopwatch.elapsed(TimeUnit.MILLISECONDS), wordCount.get(), dictionaryLocation));
            }
            stopwatch.reset();
        }

        onDictionaryLoaded();
    }

//    @Override
//    public final synchronized void reLoad() {
//        try {
//            HanLpLogger.debug(this,
//                    String.format("Begin to reload dictionary[%s]", dictionaryName()));
//            releaseResource();
//        }
//        catch (Exception ex) {
//            HanLpLogger.error(this,
//                    String.format("Error occurred while releasing resource, ignore reload dictionary[%S]", dictionaryName()));
//            return;
//        }
//        load();
//
//        HanLpLogger.debug(this,
//                String.format("Finish reload dictionary[%s]", dictionaryName()));
//    }
}