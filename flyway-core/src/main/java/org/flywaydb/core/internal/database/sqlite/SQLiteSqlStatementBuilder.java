/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.sqlite;

import org.flywaydb.core.internal.sqlscript.Delimiter;
import org.flywaydb.core.internal.sqlscript.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.regex.Pattern;

/**
 * SqlStatementBuilder supporting SQLite-specific delimiter changes.
 */
public class SQLiteSqlStatementBuilder extends SqlStatementBuilder {
    private static final Pattern PRAGMA_FOREIGNKEYS_REGEX = Pattern.compile("^PRAGMA FOREIGN_KEYS=.*");
    private static final Pattern CREATE_TRIGGER_REGEX = Pattern.compile("^CREATE( (TEMP|TEMPORARY))? TRIGGER.*");

    /**
     * Holds the beginning of the statement.
     */
    private String statementStart = "";

    SQLiteSqlStatementBuilder() {
        super(Delimiter.SEMICOLON);
    }

    @Override
    protected Delimiter changeDelimiterIfNecessary(String line, Delimiter delimiter) {
        boolean createTriggerStatement = CREATE_TRIGGER_REGEX.matcher(statementStart).matches();

        if (createTriggerStatement && !line.endsWith("END;")) {
            return null;
        }
        return defaultDelimiter;
    }

    @Override
    protected void applyStateChanges(String line) {
        super.applyStateChanges(line);

        if (!executeInTransaction || !hasNonCommentPart()) {
            return;
        }

        if (StringUtils.countOccurrencesOf(statementStart, " ") < 8) {
            statementStart += line;
            statementStart += " ";
            statementStart = StringUtils.collapseWhitespace(statementStart);
        }

        if (PRAGMA_FOREIGNKEYS_REGEX.matcher(statementStart).matches()) {
            executeInTransaction = false;
        }
    }

    @Override
    protected String cleanToken(String token) {
        if (token.startsWith("X'")) {
            // blob literal
            return token.substring(token.indexOf("'"));
        }
        return token;
    }

}