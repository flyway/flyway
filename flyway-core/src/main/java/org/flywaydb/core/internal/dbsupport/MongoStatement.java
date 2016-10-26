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
package org.flywaydb.core.internal.dbsupport;

/**
 * Represents a single Mongo JavaScript statement that can be executed at once against a database.
 */
public class MongoStatement {
    /**
     * The original line number where the statement was located in the script it came from.
     */
    private int lineNumber;

    /**
     * The json string associated with this MongoStatement.
     */
    private String json;

    /**
     * The database name where this mongo statement will be applied.
     */
    private String dbName;

    /**
     * Creates a new MongoStatement.
     *
     * @param lineNumber The original line number where the statement was located in the script it came from.
     * @param json  JSON string related with this MongoStatement.
     * @param dbName the database name where this statement will be applied.
     */
    public MongoStatement(int lineNumber, String json, String dbName) {
        this.lineNumber = lineNumber;
        this.json = json;
        this.dbName = dbName;
    }

    /**
     * @return The original line number where the statement was located in the script it came from.
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * @return The JSON string related with this MongoStatement.
     */
    public String getJson() {
        return json;
    }

    /**
     * @return The database name where this mongo statement will be applied.
     */
    public String getDbName() {
        return dbName;
    }

}
