/**
 * Copyright 2010-2016 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core.internal.dbsupport.h2;

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
        flyway.setLocations("org.flywaydb.core.internal.dbsupport.h2");

        try {
            flyway.migrate();
            fail();
        } catch (FlywayException e) {
            assertEquals("Expected", ExceptionUtils.getRootCause(e).getMessage());
        }
    }
}
