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
package org.flywaydb.core.internal.resolver.mongoscript;

import com.mongodb.MongoClient;
import org.flywaydb.core.api.resolver.AbstractMongoMigrationExecutor;
import org.flywaydb.core.internal.dbsupport.MongoScript;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.scanner.Resource;

public class MongoScriptMigrationExecutor extends AbstractMongoMigrationExecutor {
    /**
     * The Resource pointing to the javascript containing mongo database commands.
     * The complete JavaScript is not held as a member field here because this would use the total
     * size of all Mongo migrations files in heap space during db migration, see issue 184.
     */
    private final Resource mongoScriptResource;

    /**
     * The encoding of the javascript.
     */
    private final String encoding;

    /**
     * The database to use.
     */
    private String databaseName;

    /**
     * The placeholder replacer to apply to mongo js migration scripts.
     */
    private final PlaceholderReplacer placeholderReplacer;

    /**
     * Creates a new mongo javascript migration.
     *
     * @param mongoScriptResource   The resource containing the MongoScript.
     * @param encoding              The encoding of this Javascript migration.
     * @param databaseName          The database name on which migration will be applied.
     * @param placeholderReplacer   The placeholder replacer to apply to mongo js migration scripts.
     */
    public MongoScriptMigrationExecutor(Resource mongoScriptResource,
                                        String encoding,
                                        String databaseName,
                                        PlaceholderReplacer placeholderReplacer) {
        this.mongoScriptResource = mongoScriptResource;
        this.encoding = encoding;
        this.databaseName = databaseName;
        this.placeholderReplacer = placeholderReplacer;
    }

    @Override
    public void execute(MongoClient mongoClient) {
        MongoScript mongoScript = new MongoScript(mongoScriptResource, encoding, databaseName, placeholderReplacer);
        mongoScript.execute(mongoClient);
    }

    @Override
    public boolean executeInTransaction() {
        return true;
    }
}
