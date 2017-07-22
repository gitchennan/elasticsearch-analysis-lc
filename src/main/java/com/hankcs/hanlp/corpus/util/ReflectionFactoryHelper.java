package com.hankcs.hanlp.corpus.util;

import sun.reflect.ReflectionFactory;

import java.security.AccessController;
import java.security.PrivilegedAction;

public class ReflectionFactoryHelper {
    public static ReflectionFactory getReflectionFactory() {
        return AccessController.doPrivileged(new PrivilegedAction<ReflectionFactory>() {
            @Override
            public ReflectionFactory run() {
                return ReflectionFactory.getReflectionFactory();
            }
        });
    }
}
