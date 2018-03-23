package com.cmb.itc.dba.util;

import org.springframework.context.ApplicationContext;

public class  SpringContextUtil {

    private static ApplicationContext applicationContext;


    public static void setApplicationContext(ApplicationContext applicationContext) {
        SpringContextUtil.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
