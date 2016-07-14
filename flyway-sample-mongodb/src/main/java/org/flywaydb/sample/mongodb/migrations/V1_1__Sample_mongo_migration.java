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
package org.flywaydb.sample.mongodb.migrations;

import org.flywaydb.core.api.migration.mongodb.MongoMigration;

import com.mongodb.MongoClient;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;

public class V1_1__Sample_mongo_migration implements MongoMigration {

  @Override
  public void migrate(MongoClient client) throws Exception {
    MongoDatabase testDb = client.getDatabase("flyway_test");
    MongoCollection<BasicDBObject> testCollection =
			testDb.getCollection("testCollection", BasicDBObject.class);
    BasicDBObject testObj = new BasicDBObject().
      append("name", "Mr. Meeseeks").
      append("color", "blue");

    testCollection.insertOne(testObj);
  }
}
