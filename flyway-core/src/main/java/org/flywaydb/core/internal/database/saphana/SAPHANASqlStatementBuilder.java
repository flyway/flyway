/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.core.internal.database.saphana;

import org.flywaydb.core.internal.database.Delimiter;
import org.flywaydb.core.internal.database.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;

/**
 * SqlStatementBuilder supporting SAP HANA-specific delimiter changes.
 */
public class SAPHANASqlStatementBuilder extends SqlStatementBuilder {
    private static final Log LOG = LogFactory.getLog(SAPHANASqlStatementBuilder.class);

    /**
     * Are we currently inside a BEGIN END; block? How deeply are begin/end nested?
     */
    private int beginEndNestedDepth = 0;
    private String statementStartNormalized = "";

    /**
     * Creates a new SqlStatementBuilder.
     *
     * @param defaultDelimiter The default delimiter for this database.
     */
    SAPHANASqlStatementBuilder(Delimiter defaultDelimiter) {
        super(defaultDelimiter);
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
        if (statementStartNormalized.length() < 16) {
            final String effectiveLine = cutCommentsFromEnd(line);
            statementStartNormalized += effectiveLine + " ";
            statementStartNormalized = StringUtils.trimLeadingWhitespace(StringUtils.collapseWhitespace(statementStartNormalized));
        }
        boolean insideStatementAllowingNestedBeginEndBlocks =
                statementStartNormalized.startsWith("CREATE PROCEDURE")
                || statementStartNormalized.startsWith("CREATE FUNCTION")
                || statementStartNormalized.startsWith("CREATE TRIGGER")
                || statementStartNormalized.startsWith("DO");

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
        if (null == line) {
            return line;
        }
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
