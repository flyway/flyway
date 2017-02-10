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
package org.flywaydb.core.internal.util.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import org.flywaydb.core.api.FlywayException;

/**
 * Utility methods for Mongo Database.
 */
public class MongoDatabaseUtil {

    /**
     * Checks whether a Mongo database exists.
     *
     * @param client a MongoClient for connecting to the database.
     * @param dbName the database name.
     * @return {@code true} if a database with the given name exists, {@code false} if not.
     * @throws FlywayException when the check fails.
     */
    public static boolean exists(MongoClient client, String dbName) throws FlywayException {
        try {
            MongoCursor<String> dbNameIterator = client.listDatabaseNames().iterator();
            while(dbNameIterator.hasNext()) {
                if (dbNameIterator.next().equals(dbName))
                    return true;
            }
            return false;
        } catch (MongoException e) {
            throw new FlywayException("Unable to check whether Mongo database" + dbName + " exists", e);
        }
    }

    /**
     * Checks whether a Mongo database is empty.
     *
     * @param client a MongoClient for connecting to the database.
     * @param dbName the database name.
     * @return {@code true} if it is, {@code false} if isn't. Returns {@code false} if the database does not exist.
     * @throws FlywayException when the check fails.
     */
    public static boolean empty(MongoClient client, String dbName) throws FlywayException {
        return empty(client.getDatabase(dbName));
    }

    /**
     * Checks whether a Mongo database is empty.
     *
     * @param mongoDb a MongoDatabase object
     * @return {@code true} if it is, {@code false} if isn't. Returns {@code false} if the database does not exist.
     * @throws FlywayException when the check fails.
     */
    public static boolean empty(MongoDatabase mongoDb) throws FlywayException {
        try {
            MongoIterable<String> collectionIterable = mongoDb.listCollectionNames();
            return !collectionIterable.iterator().hasNext();
        } catch (MongoException e) {
            throw new FlywayException("Unable to check whether Mongo database " + mongoDb.getName() + " is empty", e);
        }
    }

    /**
     * Checks whether a Mongo collection exists.
     *
     * @param client a MongoClient for connecting to the database.
     * @param dbName the database name.
     * @param name the collection name.
     * @return {@code true} if it does, {@code false} if not.
     */
    public static boolean hasCollection(MongoClient client, String dbName, String name) {
        try {
            MongoDatabase mongoDb = client.getDatabase(dbName);
            MongoIterable<String> collectionNameIterator = mongoDb.listCollectionNames();
            for(String collectionName: collectionNameIterator) {
                if (collectionName.equals(name)) return true;
            }
            return false;
        } catch (MongoException e) {
            throw new FlywayException("Unable to check whether collection " + name + " exists", e);
        }
    }

}
