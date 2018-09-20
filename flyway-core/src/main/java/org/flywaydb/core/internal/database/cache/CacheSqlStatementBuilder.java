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
package org.flywaydb.core.internal.database.Cache;

import org.flywaydb.core.internal.sqlscript.Delimiter;
import org.flywaydb.core.internal.sqlscript.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.Collection;

/**
 * SqlStatementBuilder supporting Cache specific syntax.
 */
public class CacheSqlStatementBuilder extends SqlStatementBuilder {

    /**
     * Holds the beginning of the statement.
     */
    private String statementStart = "";

    CacheSqlStatementBuilder() {
        super(Delimiter.SEMICOLON);
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
    }

    @Override
    protected Collection<String> tokenizeLine(String line) {
        return StringUtils.tokenizeToStringCollection(line, " @<>;:=|(),+{}[]");
    }
}