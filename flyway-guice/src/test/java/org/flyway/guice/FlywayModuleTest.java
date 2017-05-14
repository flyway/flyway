package org.flyway.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Assert;
import org.flywaydb.core.Flyway;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.sql.DataSource;

import static com.google.inject.Guice.createInjector;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by berndfarka on 13.01.15.
 */
public class FlywayModuleTest {

    private Flyway flyway;
    private DataSource ds;

    @Before
    public void createMocks(){
        this.flyway = mock(Flyway.class);
        this.ds =  mock(DataSource.class);
    }


    @Test
    public void testMigration(){

        Injector injector = createInjector(new TestModule(ds), new FlywayModule(flyway, true));

        verify(flyway, times(1)).setDataSource(ds);
        verify(flyway, times(1)).migrate();
        assertNotNull("Flyway must be able to injectable", injector.getInstance(Flyway.class));
    }

    @Test
    public void testInjectionWithoutMigration(){
        Injector injector = createInjector(new TestModule(ds), new FlywayModule(flyway, false));

        verify(flyway, times(1)).setDataSource(ds);
        verify(flyway, times(0)).migrate();
        assertNotNull("Flyway must be able to injectable", injector.getInstance(Flyway.class));
    }

}
