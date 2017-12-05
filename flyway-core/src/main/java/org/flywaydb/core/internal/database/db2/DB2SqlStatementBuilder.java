/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.database.db2;

import org.flywaydb.core.internal.database.Delimiter;
import org.flywaydb.core.internal.database.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.Deque;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SqlStatementBuilder supporting DB2-specific delimiter changes.
 */
public class DB2SqlStatementBuilder extends SqlStatementBuilder {
    /**
     * The keyword that indicates a change in delimiter.
     */
    private static final String DELIMITER_KEYWORD = "--#SET TERMINATOR";

    /**
     * Regex to check for a BEGIN statement of a SQL PL block (Optional label followed by BEGIN).
     */
    private static final Pattern BEGIN_REGEX = Pattern.compile("((([A-Z]+[A-Z0-9]*)\\s?:\\s?)|(.*\\s))?BEGIN(\\sATOMIC)?(\\s.*)?");

    /**
     * Regex for keywords that can appear after a string literal without being separated by a space.
     */
    private static final Pattern KEYWORDS_AFTER_STRING_LITERAL_REGEX = Pattern.compile("(.*')(DO)(?!.)");

    /**
     * The labels associated with nested BEGIN ... END blocks.
     */
    private Deque<String> beginEndLabels = new LinkedList<String>();

    /**
     * Holds the beginning of the statement.
     */
    private String statementStart = "";

    /**
     * The current delimiter to use. This delimiter can be changed
     * as well as temporarily disabled inside BEGIN END; blocks.
     */
    private Delimiter currentDelimiter = defaultDelimiter;

    /**
     * Creates a new SqlStatementBuilder.
     *
     * @param defaultDelimiter The default delimiter for this database.
     */
    public DB2SqlStatementBuilder(Delimiter defaultDelimiter) {
        super(defaultDelimiter);
    }

    @Override
    public Delimiter extractNewDelimiterFromLine(String line) {
        if (line.toUpperCase().startsWith(DELIMITER_KEYWORD)) {
            return new Delimiter(line.substring(DELIMITER_KEYWORD.length()).trim(), false);
        }

        return null;
    }

    @Override
    protected boolean isSingleLineComment(String line) {
        return line.startsWith("--") && !line.startsWith(DELIMITER_KEYWORD);
    }

    @Override
    protected String cleanToken(String token) {
        if (token.startsWith("X'")) {
            return token.substring(token.indexOf("'"));
        }

        Matcher afterMatcher = KEYWORDS_AFTER_STRING_LITERAL_REGEX.matcher(token);
        if (afterMatcher.find()) {
            token = afterMatcher.group(1);
        }

        return super.cleanToken(token);
    }

    @Override
    protected Delimiter changeDelimiterIfNecessary(String line, Delimiter delimiter) {
        if (delimiter != null && !delimiter.equals(currentDelimiter)) {
            // Synchronize current delimiter with main delimiter in case it was changed
            // due to a --#SET TERMINATOR directive earlier in the SQL script
            currentDelimiter = delimiter;
        }

        if (StringUtils.countOccurrencesOf(statementStart, " ") < 4) {
            statementStart += line;
            statementStart += " ";
        }

        if (!";".equals(currentDelimiter.getDelimiter())) {
            return currentDelimiter;
        }

        if (statementStart.matches("^CREATE( OR REPLACE)? (FUNCTION|PROCEDURE|TRIGGER)(\\s.*)?")) {
            if (isBegin(line) || line.matches("(.*\\s)?CASE(\\sWHEN)?(\\s.*)?")) {
                beginEndLabels.addLast(extractLabel(line));
            }

            if (isEnd(line, beginEndLabels.isEmpty() ? null : beginEndLabels.getLast(), currentDelimiter, beginEndLabels.size())) {
                beginEndLabels.removeLast();
            }
        }

        if (!beginEndLabels.isEmpty()) {
            return null;
        }
        return currentDelimiter;
    }

    static boolean isBegin(String line) {
        return BEGIN_REGEX.matcher(line).find();
    }

    static String extractLabel(String line) {
        Matcher matcher = BEGIN_REGEX.matcher(line);
        return line.contains(":") && matcher.matches() ? matcher.group(3) : null;
    }

    static boolean isEnd(String line, String label, Delimiter currentDelimiter, int beginEndDepth) {
        String actualDelimiter = beginEndDepth > 1 ? ";" : currentDelimiter.getDelimiter();

        return line.matches(
                // First optionally match preceding part of statement
                "(.*\\s)?"
                        // Then require END
                        + "END"
                        // Now optionally match label
                        + (label == null ? "" : "(\\s" + Pattern.quote(label) + ")?")
                        // Finally optionally match delimitert
                        + "\\s?(" + Pattern.quote(actualDelimiter) + ")?");
    }
}
