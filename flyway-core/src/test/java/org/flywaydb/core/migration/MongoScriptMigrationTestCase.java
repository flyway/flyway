/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.migration;

import com.mongodb.MongoClient;
import org.flywaydb.core.EmbeddedMongoDb;
import org.flywaydb.core.MongoFlyway;
import org.junit.Before;
import java.util.*;

/**
 * Test to demonstrate the migration functionality with MongoDB.
 */
public abstract class MongoScriptMigrationTestCase extends EmbeddedMongoDb {

    /**
     * The base directory for the regular test migrations.
     */
    protected static final String BASEDIR = "migration/mongoscript";

    protected MongoClient client;
    protected MongoFlyway flyway;

    @Before
    public void setUp() throws Exception {
        Properties mongoProperties = new Properties();
        mongoProperties.setProperty("flyway.locations", BASEDIR);
        mongoProperties.setProperty("flyway.validateOnMigrate", "false");
        mongoProperties.setProperty("flyway.mongoUri", getMongoUri());
        flyway = new MongoFlyway();
        flyway.configure(mongoProperties);
        client = getMongoClient();
        flyway.setMongoClient(client); // using this to avoid MongoClient being closed by Flyway
        flyway.clean();
    }

}
