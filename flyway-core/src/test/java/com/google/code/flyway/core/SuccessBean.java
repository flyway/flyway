package com.google.code.flyway.core;

import org.springframework.beans.factory.InitializingBean;

/**
 * Dummy bean to start after the migration is complete.
 */
public class SuccessBean implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("SuccessBean initialized.");
    }
}
