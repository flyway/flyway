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

import com.mongodb.MongoException;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.scanner.Resource;

/**
 * This specific exception is thrown when Flyway encounters a problem in JavaScript Mongo migration.
 */
public class FlywayMongoScriptException extends FlywayException {

    private final Resource resource;
    private final MongoStatement statement;

    /**
     * Creates new instance of FlywayMongoScriptException.
     *
     * @param resource     The resource containing the failed statement.
     * @param statement    The failed MongoStatement.
     * @param exception    Cause of the problem.
     */
    public FlywayMongoScriptException(Resource resource, MongoStatement statement, Exception exception) {
        super(exception);
        this.resource = resource;
        this.statement = statement;
    }

    /**
     * Returns the line number in migration Mongo script where exception occurred.
     *
     * @return The line number.
     */
    public int getLineNumber() {
        return statement.getLineNumber();
    }

    /**
     * Returns the failed statement in MongoScript.
     *
     * @return The failed statement.
     */
    public String getStatement() {
        return statement.getJson();
    }

    @Override
    public String getMessage() {
        String title = resource == null ? "Script failed" : "Migration " + resource.getFilename() + " failed";
        String underline = StringUtils.trimOrPad("", title.length(), '-');

        MongoException cause = (MongoException) getCause();
        String message = "\n" + title + "\n" + underline + "\n";
        message += "Error Code : " + cause.getCode() + "\n";
        if (cause.getMessage() != null) {
            message += "Message    : " + cause.getMessage().trim() + "\n";
        }
        if (resource != null) {
            message += "Location   : " + resource.getLocation() + " (" + resource.getLocationOnDisk() + ")\n";
        }
        message += "Line       : " + getLineNumber() + "\n";
        message += "Statement  : " + getStatement() + "\n";

        return message;
    }
}
