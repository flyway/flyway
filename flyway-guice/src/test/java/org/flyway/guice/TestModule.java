package org.flyway.guice;

import com.google.inject.AbstractModule;

import javax.sql.DataSource;

/**
 * Created by berndfarka on 13.01.15.
 */
public class TestModule extends AbstractModule {

    private final DataSource dataSource;

    public TestModule(DataSource dataSource){
        this.dataSource = dataSource;
    }


    @Override
    protected void configure() {
        bind(DataSource.class).toInstance(dataSource);
    }
}
