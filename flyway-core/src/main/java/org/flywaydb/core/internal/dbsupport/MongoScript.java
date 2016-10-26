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

import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.json.JsonParseException;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.flywaydb.core.internal.util.scanner.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains a series of MongoStatements that were found in a JavaScript file.
 * Each valid statement in a JavaScript file must be terminated by a delimiter (eg: ;).
 * Single-line (//) and multi-line (/* * /)comments are stripped and ignored.
 */
public class MongoScript {

    private static final Log LOG = LogFactory.getLog(MongoScript.class);

    /**
     * A list of Mongo statements contained in this script.
     */
    private final List<MongoStatement> mongoStatements;

    /**
     * The resource containing the statements.
     */
    private final Resource resource;

    /**
     * The database name where statements belonging to this MongoScript will be applied.
     */
    private final String databaseName;

    /**
     * Creates a new MongoScript from this source.
     *
     * @param mongoScriptSource The java script as a text block with all placeholders already replaced.
     * @param databaseName      The database name where the MongoScript will be applied.
     */
    public MongoScript(String mongoScriptSource, String databaseName) {
        this.resource = null;
        this.databaseName = databaseName;
        this.mongoStatements = parse(mongoScriptSource);
    }

    /**
     * Creates a new MongoScript from this resource.
     *
     * @param mongoScriptResource  The resource containing the statements.
     * @param encoding             The encoding to use.
     * @param databaseName         The database name where the MongoScript will be applied.
     * @param placeholderReplacer  The placeholder replacer to apply to mongo js migration scripts.
     */
    public MongoScript(Resource mongoScriptResource, String encoding, String databaseName, PlaceholderReplacer placeholderReplacer) {
        String mongoScriptSource = mongoScriptResource.loadAsString(encoding);
        this.resource = mongoScriptResource;
        this.databaseName = databaseName;
        this.mongoStatements = parse(placeholderReplacer.replacePlaceholders(mongoScriptSource));
    }

    /**
     * @return The resource containing the statements.
     */
    public Resource getResource() {
        return resource;
    }

    /**
     * Executes the MongoStatements found in this MongoScript against the database.
     *
     * @param mongoClient The MongoClient to use to execute this script.
     */
    public void execute(final MongoClient mongoClient) {
        for (MongoStatement mongoStatement : mongoStatements) {
            MongoDatabase mongoDatabase = mongoClient.getDatabase(mongoStatement.getDbName());
            LOG.debug("Executing MONGO: " + mongoStatement);
            try {
                mongoDatabase.runCommand(Document.parse(mongoStatement.getJson()));
            } catch (JsonParseException jpe) {
                MongoException e = new MongoException("Cannot parse mongo command. " + jpe.getMessage());
                throw new FlywayMongoScriptException(resource, mongoStatement, e);
            } catch (MongoException e) {
                throw new FlywayMongoScriptException(resource, mongoStatement, e);
            }
        }
    }

    /**
     * Parses this Mongo javaScript source into statements.
     *
     * @param mongoScriptSource The script source to parse.
     * @return The parsed statements.
     */
    /* private -> for testing */
    List<MongoStatement> parse(String mongoScriptSource) {
        return linesToStatements(readLines(new StringReader(mongoScriptSource)));
    }

    /**
     * Turns these lines in a series of statements.
     *
     * @param lines The lines to analyse.
     * @return The statements contained in these lines (in order).
     */
    /* private -> for testing */
    List<MongoStatement> linesToStatements(List<String> lines) {
        List<MongoStatement> statements = new ArrayList<MongoStatement>();

        MongoStatementBuilder mongoStatementBuilder = new MongoStatementBuilder();
        String dbNameInScope = databaseName;

        for (int lineNumber = 1; lineNumber <= lines.size(); lineNumber++) {
            String line = lines.get(lineNumber - 1);

            if (mongoStatementBuilder.isEmpty()) {
                if (!StringUtils.hasText(line)) {
                    // Skip empty line between statements.
                    continue;
                }
                mongoStatementBuilder.setLineNumber(lineNumber);
            }

            mongoStatementBuilder.addLine(line);

            if (mongoStatementBuilder.canDiscard()) {
                mongoStatementBuilder = new MongoStatementBuilder();
            } else if (mongoStatementBuilder.isTerminated()) {
                String currentDbNameInScope = mongoStatementBuilder.getDbName();
                if (currentDbNameInScope != null) {
                    dbNameInScope = currentDbNameInScope;
                    LOG.debug("Changing database in scope to: " + dbNameInScope);
                } else {
                    MongoStatement mongoStatement = mongoStatementBuilder.getMongoStatement(dbNameInScope);
                    if (mongoStatement != null) {
                        statements.add(mongoStatement);
                        LOG.debug("Found statement at line " + mongoStatement.getLineNumber() +
                                ": " + mongoStatement.getJson());
                    }
                }
                mongoStatementBuilder = new MongoStatementBuilder();
            }
        }

        // Catch any statements not followed by delimiter.
        if (!mongoStatementBuilder.isEmpty()) {
            statements.add(mongoStatementBuilder.getMongoStatement(dbNameInScope));
        }

        return statements;
    }

    /**
     * Parses the textual data provided by this reader into a list of lines.
     *
     * @param reader The reader for the textual data.
     * @return The list of lines (in order).
     * @throws IllegalStateException Thrown when the textual data parsing failed.
     */
    private List<String> readLines(Reader reader) {
        List<String> lines = new ArrayList<String>();

        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;

        try {
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            String message = resource == null ?
                    "Unable to parse lines" :
                    "Unable to parse " + resource.getLocation() + " (" + resource.getLocationOnDisk() + ")";
            throw new FlywayException(message, e);
        }

        return lines;
    }
}
