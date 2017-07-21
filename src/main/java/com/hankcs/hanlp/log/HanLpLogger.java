package com.hankcs.hanlp.log;


public class HanLpLogger {

    public static void debug(Object object, String message) {
        info(object, message);
    }

    public static void info(Object object, String message) {
        if (object instanceof Class) {
            System.out.println(
                    String.format("[%s] %s",
                            ((Class) object).getName(), message));
        }
        else {
            System.out.println(
                    String.format("[%s.%s] %s",
                            object.getClass().getPackage().getName(),
                            object.getClass().getName(), message)
            );
        }
    }

    public static void info(Object object, String message, Throwable t) {
        if (object instanceof Class) {
            System.out.println(
                    String.format("[%s] %s",
                            ((Class) object).getName(), message));
        }
        else {
            System.out.println(
                    String.format("[%s.%s] %s",
                            object.getClass().getPackage().getName(),
                            object.getClass().getName(), message)
            );
        }
        t.printStackTrace();
    }


    public static void warn(Object object, String message) {
        info(object, message);
    }

    public static void warn(Object object, String message, Throwable e) {
        info(object, message, e);
    }

    public static void error(Object object, String message) {
        info(object, message);
    }

    public static void error(Object object, String message, Throwable t) {
        info(object, message, t);
    }
}
