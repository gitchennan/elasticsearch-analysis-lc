package com.hankcs.test.proxy;

import org.junit.Test;

import java.lang.annotation.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyTest {

    @Test
    public void testProxy() {
        IMyService myService = new MyServiceImpl();
        InvocationHandler h = new MyInkHandler(myService);

        IMyService myServiceProxy = (IMyService) Proxy.newProxyInstance(ProxyTest.class.getClassLoader(), new Class[]{IMyService.class}, h);
        myServiceProxy.sayHi("chennan");


        System.out.println(myService.getClass());
        System.out.println(myServiceProxy.getClass());

        System.out.println(myService instanceof IMyService);
        System.out.println(myServiceProxy instanceof IMyService);

        System.out.println(myService.getClass().equals(myServiceProxy.getClass()));

    }

    class MyInkHandler implements InvocationHandler {

        private IMyService IMyService;

        MyInkHandler(IMyService IMyService) {
            this.IMyService = IMyService;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            for (Method oriMethod : IMyService.getClass().getDeclaredMethods()) {
                if (oriMethod.getName().equalsIgnoreCase(method.getName())) {
                    for (Annotation annotation : oriMethod.getAnnotations()) {
                        if (annotation.annotationType().equals(MyTransaction.class)) {
                            System.out.println("begin transaction");
                        }
                    }
                }
            }

            System.out.println(args[0].toString() + " before say hi");
            Object sayHi = method.invoke(IMyService, args);
            System.out.println(args[0].toString() + " after say hi");
            return sayHi;
        }
    }

    interface IMyService {
        void sayHi(String name);
    }

    class MyServiceImpl implements IMyService {
        @Override
        @MyTransaction
        public void sayHi(String name) {
            System.out.println(name + " : man say hi");
        }
    }

    @Target(value = ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface MyTransaction {

    }

}
