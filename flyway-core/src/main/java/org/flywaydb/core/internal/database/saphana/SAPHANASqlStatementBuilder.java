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
package org.flywaydb.core.internal.database.saphana;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.sqlscript.Delimiter;
import org.flywaydb.core.internal.sqlscript.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.regex.Pattern;

/**
 * SqlStatementBuilder supporting SAP HANA-specific delimiter changes.
 */
public class SAPHANASqlStatementBuilder extends SqlStatementBuilder {
    private static final Log LOG = LogFactory.getLog(SAPHANASqlStatementBuilder.class);

    private static final Pattern BLOCK_STATEMENT_REGEX = Pattern.compile(
            "^((((CREATE( OR REPLACE)?)|ALTER) (PROCEDURE|FUNCTION))|CREATE TRIGGER|DO).*");

    /**
     * Are we currently inside a BEGIN END; block? How deeply are begin/end nested?
     */
    private int beginEndNestedDepth = 0;
    private String statementStartNormalized = "";

    /**
     * Creates a new SqlStatementBuilder.
     */
    SAPHANASqlStatementBuilder() {
        super(Delimiter.SEMICOLON);
    }

    @Override
    protected String cleanToken(String token) {
        if (token.startsWith("N'") || token.startsWith("X'")
                || token.startsWith("DATE'") || token.startsWith("TIME'") || token.startsWith("TIMESTAMP'")) {
            return token.substring(token.indexOf("'"));
        }
        return super.cleanToken(token);
    }

    @Override
    protected Delimiter changeDelimiterIfNecessary(String line, Delimiter delimiter) {
        // need only accumulate 16 characters of normalized statement start in order to determine if it is an 'interesting' statement
        if (hasNonCommentPart() && statementStartNormalized.length() < 16) {
            final String effectiveLine = cutCommentsFromEnd(line);
            statementStartNormalized += effectiveLine + " ";
            statementStartNormalized = StringUtils.trimLeadingWhitespace(StringUtils.collapseWhitespace(statementStartNormalized));
        }
        boolean insideStatementAllowingNestedBeginEndBlocks = BLOCK_STATEMENT_REGEX.matcher(statementStartNormalized).matches();

        if (insideStatementAllowingNestedBeginEndBlocks) {
            if (line.startsWith("BEGIN")) {
                beginEndNestedDepth++;
            }

            if (line.endsWith("END;")) {
                beginEndNestedDepth--;
                if (beginEndNestedDepth < 0) {
                    LOG.warn("SQL statement parsed unsuccessfully: found unpaired 'END;' in statement");
                }
                if (beginEndNestedDepth <= 0) {
                    insideStatementAllowingNestedBeginEndBlocks = false;
                }
            }
        }

        if (insideStatementAllowingNestedBeginEndBlocks) {
            return null;
        }
        return defaultDelimiter;
    }

    private static String cutCommentsFromEnd(String line) {
        final int beginOfLineComment = line.indexOf("--");
        final int beginOfBlockComment = line.indexOf("/*");
        if (-1 != beginOfLineComment) {
            if (-1 != beginOfBlockComment) {
                return line.substring(0, Math.min(beginOfBlockComment, beginOfLineComment));
            } else {
                return line.substring(0, beginOfLineComment);
            }
        } else {
            if (-1 != beginOfBlockComment) {
                return line.substring(0, beginOfBlockComment);
            }
        }
        return line;
    }
}