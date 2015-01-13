package org.flyway.guice;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;


/**
 * Module binding Flayway to the Guice Context.
 */
public class FlywayModule extends AbstractModule {

    private final boolean migrate;
    private final Flyway instance;

    /**
     *
     * @param migrate if troe Flyway.migrate() is called while binding Flyway.
     */
    public FlywayModule(boolean migrate){
        this(new Flyway(), migrate);
    }

    @VisibleForTesting
    FlywayModule(Flyway instance, boolean migrate){
        this.instance = instance;
        this.migrate = migrate;
    }

    @Inject
    private DataSource dataSource;

    @Override
    protected void configure() {
        instance.setDataSource(dataSource);
        if(migrate){
            instance.migrate();
        }
        bind(Flyway.class).toInstance(instance);
    }
}
