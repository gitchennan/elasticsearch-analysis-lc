package com.hankcs.hanlp.log;


import org.elasticsearch.common.logging.ESLoggerFactory;

public class HanLpLogger {

    public static void debug(Object object, String message) {
        if (object instanceof Class) {
            ESLoggerFactory.getLogger((Class) object).debug(message);
        }
        else {
            ESLoggerFactory.getLogger(object.getClass()).debug(message);
        }
    }

    public static void info(Object object, String message) {
        if (object instanceof Class) {
            ESLoggerFactory.getLogger((Class) object).info(message);
        }
        else {
            ESLoggerFactory.getLogger(object.getClass()).info(message);
        }
    }

    public static void info(Object object, String message, Throwable t) {
        if (object instanceof Class) {
            ESLoggerFactory.getLogger((Class) object).info(message, t);
        }
        else {
            ESLoggerFactory.getLogger(object.getClass()).info(message, t);
        }
    }


    public static void warn(Object object, String message) {
        if (object instanceof Class) {
            ESLoggerFactory.getLogger((Class) object).warn(message);
        }
        else {
            ESLoggerFactory.getLogger(object.getClass()).warn(message);
        }
    }

    public static void warn(Object object, String message, Throwable t) {
        if (object instanceof Class) {
            ESLoggerFactory.getLogger((Class) object).warn(message, t);
        }
        else {
            ESLoggerFactory.getLogger(object.getClass()).warn(message, t);
        }
    }

    public static void error(Object object, String message) {
        if (object instanceof Class) {
            ESLoggerFactory.getLogger((Class) object).error(message);
        }
        else {
            ESLoggerFactory.getLogger(object.getClass()).error(message);
        }
    }

    public static void error(Object object, String message, Throwable t) {
        if (object instanceof Class) {
            ESLoggerFactory.getLogger((Class) object).error(message, t);
        }
        else {
            ESLoggerFactory.getLogger(object.getClass()).error(message, t);
        }
    }
}
