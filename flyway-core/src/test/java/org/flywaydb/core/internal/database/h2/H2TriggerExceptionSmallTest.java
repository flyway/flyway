/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.database.h2;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.ExceptionUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class H2TriggerExceptionSmallTest {
    @Test
    public void triggerException() {
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:h2:mem:flyway_db_trigger;DB_CLOSE_DELAY=-1", "sa", "");
        flyway.setLocations("org.flywaydb.core.internal.database.h2");

        try {
            flyway.migrate();
            fail();
        } catch (FlywayException e) {
            assertEquals("Expected", ExceptionUtils.getRootCause(e).getMessage());
        }
    }
}
