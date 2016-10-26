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
package org.flywaydb.core.internal.resolver.mongodb.dummy;

import org.flywaydb.core.api.migration.mongodb.MongoMigration;
import com.mongodb.MongoClient;

/**
 * Test migration.
 */
public abstract class DummyAbstractMongoMigration implements MongoMigration {
    public final void migrate(MongoClient client) throws Exception {
        doMigrate(client);
    }

	public abstract void doMigrate(MongoClient client) throws Exception;
}
