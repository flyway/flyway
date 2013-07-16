package com.googlecode.flyway.core.dbsupport.h2;

import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.api.FlywayException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class H2TriggerExceptionSmallTest {
    @Test
    public void triggerException() {
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:h2:mem:flyway_db_trigger;DB_CLOSE_DELAY=-1", "sa", "");
        flyway.setLocations("com.googlecode.flyway.core.dbsupport.h2");

        try {
            flyway.migrate();
            fail();
        } catch (FlywayException e) {
            assertEquals("Expected", e.getCause().getMessage());
        }
    }
}
