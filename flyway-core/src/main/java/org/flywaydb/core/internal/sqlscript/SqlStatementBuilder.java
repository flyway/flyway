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
package org.flywaydb.core.internal.sqlscript;

import org.flywaydb.core.internal.line.Line;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Builds a SQL statement, one line at a time.
 */
public abstract class SqlStatementBuilder {







    /**
     * The current statement, as it is being built.
     */
    protected final List<Line> lines = new ArrayList<>();

    /**
     * Flag indicating whether the current statement is properly terminated.
     */
    private boolean terminated;

    /**
     * Are we currently inside a ' multi-line string literal.
     */
    protected boolean insideQuoteStringLiteral = false;

    /**
     * Are we currently inside an alternate multi-line string literal.
     */
    protected boolean insideAlternateQuoteStringLiteral = false;

    /**
     * The alternate quote that is expected to close the string literal.
     */
    private String alternateQuote;

    /**
     * Whether the last processed line ended with a single line -- comment.
     */
    private boolean lineEndsWithSingleLineComment = false;

    /**
     * Are we inside a multi-line /*  *&#47; comment.
     */
    protected boolean insideMultiLineComment = false;

    /**
     * The first line where a non-comment part of a statement has been seen.
     */
    protected int firstNonCommentLine = -1;

    /**
     * How deeply nested are we within blocks.
     */
    private int nestedBlockDepth = 0;

    /**
     * Whether this statement should be executed within a transaction or not.
     */
    protected boolean executeInTransaction = true;

    /**
     * The default delimiter for this database.
     */
    protected final Delimiter defaultDelimiter;

    /**
     * The current delimiter to look for to terminate the statement.
     */
    protected Delimiter delimiter;

    /**
     * Creates a new SqlStatementBuilder.
     *
     * @param defaultDelimiter The default delimiter for this database.
     */
    public SqlStatementBuilder(Delimiter defaultDelimiter) {
        this.defaultDelimiter = defaultDelimiter;
        this.delimiter = defaultDelimiter;
    }

    /**
     * @param delimiter The current delimiter to look for to terminate the statement.
     */
    public void setDelimiter(Delimiter delimiter) {
        this.delimiter = delimiter;
    }

    /**
     * Checks whether the statement is still empty.
     *
     * @return {@code true} if it is, {@code false} if it isn't.
     */
    public final boolean isEmpty() {
        return lines.isEmpty();
    }

    /**
     * Checks whether the statement being built is now properly terminated.
     *
     * @return {@code true} if it is, {@code false} if it isn't.
     */
    public boolean isTerminated() {
        return terminated;
    }

    /**
     * @return Whether this statement contains more than just comments.
     */
    protected boolean hasNonCommentPart() {
        return firstNonCommentLine >= 0;
    }








    /**
     * @return The assembled statement, with the delimiter stripped off.
     */
    public SqlStatement getSqlStatement() {
        return new StandardSqlStatement(lines, delimiter



        );
    }

    /**
     * Analyses this line ánd extracts the new default delimiter.
     * This method is only called between statements and looks for explicit delimiter change directives.
     *
     * @return The new delimiter. {@code null} if it is the same as the current one.
     */
    @SuppressWarnings("UnusedParameters")
    public Delimiter extractNewDelimiterFromLine(String line) {
        return null;
    }

    /**
     * Checks whether this line is in fact a directive disguised as a comment.
     *
     * @param line The line to analyse.
     * @return {@code true} if it is a directive that should be processed by the database, {@code false} if not.
     */
    public boolean isCommentDirective(String line) {
        return false;
    }

    /**
     * Checks whether this line is just a single-line comment outside a statement or not.
     *
     * @param line The line to analyse.
     * @return {@code true} if it is, {@code false} if not.
     */
    protected boolean isSingleLineComment(String line) {
        return line.startsWith("--");
    }

    /**
     * Adds this line to the current statement being built.
     *
     * @param sqlLine The line to add.
     */
    public void addLine(Line sqlLine) {
        String line = sqlLine.getLine();
        String lineTrimmed = line.trim();
        String lineSimplified = simplifyLine(lineTrimmed);

        // Skip empty lone ; or GO statements
        if (isEmpty() && delimiter != null && lineSimplified.equals(delimiter.getDelimiter().toUpperCase())) {
            return;
        }

        if (isCommentDirective(lineTrimmed)) {
            firstNonCommentLine = lines.size();
        }

        applyStateChanges(lineSimplified);
        if (endWithOpenMultilineStringLiteral() || insideMultiLineComment) {
            lines.add(sqlLine);
            return;
        }

        delimiter = changeDelimiterIfNecessary(lineSimplified, delimiter);

        if (!lineEndsWithSingleLineComment && lineTerminatesStatement(lineSimplified, delimiter)) {
            terminated = true;
        }







        lines.add(sqlLine);
    }

    /**
     * Checks whether the statement currently ends with an open multiline string literal.
     *
     * @return {@code true} if it does, {@code false} if it doesn't.
     */
    /* protected -> for testing */ boolean endWithOpenMultilineStringLiteral() {
        return insideQuoteStringLiteral || insideAlternateQuoteStringLiteral;
    }

    /**
     * @return Whether the current statement is only closed comments so far and can be discarded.
     */
    public boolean canDiscard() {
        return !insideAlternateQuoteStringLiteral && !insideQuoteStringLiteral && !insideMultiLineComment
                && (firstNonCommentLine < 0)
                && (lines.isEmpty() || !StringUtils.hasText(lines.get(lines.size() - 1).getLine()));
    }

    /**
     * Simplifies this line to make it easier to parse.
     *
     * @param line The line to simplify.
     * @return The simplified line.
     */
    protected String simplifyLine(String line) {
        String cleanLine = removeEscapedQuotes(line)
                .replace("--", " -- ")
                .replace("/*", " /* ")
                .replace("*/", " */ ");
        return StringUtils.collapseWhitespace(cleanLine).trim().toUpperCase();
    }

    /**
     * Checks whether this line in the sql script indicates that the statement delimiter will be different from the
     * current one. Useful for database-specific stored procedures and block constructs.
     *
     * @param line      The simplified line to analyse.
     * @param delimiter The current delimiter.
     * @return The new delimiter to use (can be the same as the current one) or {@code null} for no delimiter.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    protected Delimiter changeDelimiterIfNecessary(String line, Delimiter delimiter) {
        return delimiter;
    }

    /**
     * Checks whether this line terminates the current statement.
     *
     * @param line      The line to check.
     * @param delimiter The current delimiter.
     * @return {@code true} if it does, {@code false} if it doesn't.
     */
    private boolean lineTerminatesStatement(String line, Delimiter delimiter) {
        if (delimiter == null || (defaultDelimiter.equals(delimiter) && nestedBlockDepth > 0)) {
            return false;
        }

        String upperCaseDelimiter = delimiter.getDelimiter().toUpperCase();

        if (delimiter.isAloneOnLine()) {
            return line.equals(upperCaseDelimiter);
        }

        return line.endsWith(upperCaseDelimiter);
    }

    /**
     * Extracts the alternate open quote from this token (if any).
     *
     * @param token The token to check.
     * @return The alternate open quote. {@code null} if none.
     */
    protected String extractAlternateOpenQuote(String token) {
        return null;
    }

    /**
     * Computes the alternate closing quote for this open quote.
     *
     * @param openQuote The alternate open quote.
     * @return The close quote.
     */
    protected String computeAlternateCloseQuote(String openQuote) {
        return openQuote;
    }

    /**
     * Applies any state changes resulting from this line being added.
     *
     * @param line The line that was just added to the statement.
     */
    protected void applyStateChanges(String line) {
        Collection<String> tokens = tokenizeLine(line);

        List<TokenType> delimitingTokens = extractStringLiteralDelimitingTokens(tokens);

        lineEndsWithSingleLineComment = false;
        for (TokenType delimitingToken : delimitingTokens) {
            if (!insideQuoteStringLiteral && !insideAlternateQuoteStringLiteral
                    && TokenType.MULTI_LINE_COMMENT_OPEN.equals(delimitingToken)) {
                insideMultiLineComment = true;
            }
            if (!insideQuoteStringLiteral && !insideAlternateQuoteStringLiteral
                    && TokenType.MULTI_LINE_COMMENT_CLOSE.equals(delimitingToken)) {
                insideMultiLineComment = false;
            }

            if (!insideQuoteStringLiteral && !insideAlternateQuoteStringLiteral && !insideMultiLineComment
                    && TokenType.SINGLE_LINE_COMMENT.equals(delimitingToken)) {
                lineEndsWithSingleLineComment = true;
                return;
            }

            if (!insideMultiLineComment && !insideQuoteStringLiteral &&
                    TokenType.ALTERNATE_QUOTE.equals(delimitingToken)) {
                insideAlternateQuoteStringLiteral = !insideAlternateQuoteStringLiteral;
            }

            if (!insideMultiLineComment && !insideAlternateQuoteStringLiteral &&
                    TokenType.QUOTE.equals(delimitingToken)) {
                insideQuoteStringLiteral = !insideQuoteStringLiteral;
            }

            if (!insideMultiLineComment && !insideQuoteStringLiteral && !insideAlternateQuoteStringLiteral &&
                    (TokenType.OTHER.equals(delimitingToken)
                            || TokenType.BLOCK_BEGIN.equals(delimitingToken)
                            || TokenType.BLOCK_END.equals(delimitingToken))) {
                if (!hasNonCommentPart()) {
                    firstNonCommentLine = lines.size();
                }
                if (isBlockStatement()) {
                    if (TokenType.BLOCK_BEGIN.equals(delimitingToken)) {
                        nestedBlockDepth++;
                    } else if (TokenType.BLOCK_END.equals(delimitingToken)) {
                        nestedBlockDepth--;
                    }
                }
            }
        }
    }

    /**
     * @return Whether this is a statement that can contain blocks.
     */
    protected boolean isBlockStatement() {
        return false;
    }

    /**
     * Ignore all special characters that naturally occur in SQL, but are not opening or closing string literals.
     *
     * @param line The line to tokenize.
     * @return The tokens.
     */
    protected Collection<String> tokenizeLine(String line) {
        return StringUtils.tokenizeToStringCollection(line, " @<>;:=|(),+{}");
    }

    /**
     * Extract the type of all tokens that potentially delimit string literals.
     *
     * @param tokens The tokens to analyse.
     * @return The list of potentially delimiting string literals token types per token. Tokens that do not have any
     * impact on string delimiting are discarded.
     */
    private List<TokenType> extractStringLiteralDelimitingTokens(Collection<String> tokens) {
        List<TokenType> delimitingTokens = new ArrayList<>();
        for (String token : tokens) {
            String cleanToken = cleanToken(token);
            boolean handled = false;

            if (alternateQuote == null) {
                String alternateQuoteFromToken = extractAlternateOpenQuote(cleanToken);
                if (alternateQuoteFromToken != null) {
                    String closeQuote = computeAlternateCloseQuote(alternateQuoteFromToken);
                    if (cleanToken.length() >= (alternateQuoteFromToken.length() + closeQuote.length())
                            && cleanToken.startsWith(alternateQuoteFromToken) && cleanToken.endsWith(closeQuote)) {
                        //Skip $$abc$$, ...
                        continue;
                    }

                    alternateQuote = closeQuote;
                    delimitingTokens.add(TokenType.ALTERNATE_QUOTE);

                    continue;
                }
            }
            if ((alternateQuote != null) && cleanToken.endsWith(alternateQuote)) {
                alternateQuote = null;
                delimitingTokens.add(TokenType.ALTERNATE_QUOTE);
                continue;
            }

            if ((cleanToken.length() >= 2) && cleanToken.startsWith("'") && cleanToken.endsWith("'")) {
                //Skip '', 'abc', ...
                continue;
            }
            if ((cleanToken.length() >= 4)) {
                int numberOfOpeningMultiLineComments = StringUtils.countOccurrencesOf(cleanToken, "/*");
                int numberOfClosingMultiLineComments = StringUtils.countOccurrencesOf(cleanToken, "*/");
                if (numberOfOpeningMultiLineComments > 0 && numberOfOpeningMultiLineComments == numberOfClosingMultiLineComments) {
                    //Skip /**/, /*comment*/, ...
                    continue;
                }
            }

            if (isSingleLineComment(cleanToken)) {
                delimitingTokens.add(TokenType.SINGLE_LINE_COMMENT);
                handled = true;
            }

            if (cleanToken.contains("/*")) {
                delimitingTokens.add(TokenType.MULTI_LINE_COMMENT_OPEN);
                handled = true;
            } else if (cleanToken.startsWith("'")) {
                delimitingTokens.add(TokenType.QUOTE);
                handled = true;
            }

            if (!cleanToken.contains("/*") && cleanToken.contains("*/")) {
                delimitingTokens.add(TokenType.MULTI_LINE_COMMENT_CLOSE);
                handled = true;
            } else if (!cleanToken.startsWith("'") && cleanToken.endsWith("'")) {
                delimitingTokens.add(TokenType.QUOTE);
                handled = true;
            }

            if (!handled) {
                if (isBlockBeginToken(cleanToken)) {
                    delimitingTokens.add(TokenType.BLOCK_BEGIN);
                } else if (isBlockEndToken(cleanToken)) {
                    delimitingTokens.add(TokenType.BLOCK_END);
                } else {
                    delimitingTokens.add(TokenType.OTHER);
                }
            }
        }

        return delimitingTokens;
    }

    protected boolean isBlockBeginToken(String token) {
        return false;
    }

    protected boolean isBlockEndToken(String token) {
        return false;
    }

    /**
     * Removes escaped quotes from this token.
     *
     * @param token The token to parse.
     * @return The cleaned token.
     */
    protected String removeEscapedQuotes(String token) {
        return token.replace("''", "");
    }

    /**
     * Performs additional cleanup on this token, such as removing charset casting that prefixes string literals.
     * Must be implemented in dialect specific sub classes.
     *
     * @param token The token to clean.
     * @return The cleaned token.
     */
    protected String cleanToken(String token) {
        return token;
    }

    /**
     * Whether the execution should take place inside a transaction. This is useful for databases
     * like PostgreSQL or SQL Server where certain statements can only execute outside a transaction.
     *
     * @return {@code true} if a transaction should be used (highly recommended), or {@code false} if not.
     */
    public boolean executeInTransaction() {
        return executeInTransaction;
    }

    /**
     * The types of tokens relevant for string delimiter related parsing.
     */
    private enum TokenType {
        /**
         * Some other token.
         */
        OTHER,

        /**
         * Token opens or closes a ' string literal
         */
        QUOTE,

        /**
         * Token opens or closes an alternate string literal
         */
        ALTERNATE_QUOTE,

        /**
         * Token starts end of line comment
         */
        SINGLE_LINE_COMMENT,

        /**
         * Token opens a multi-line comment
         */
        MULTI_LINE_COMMENT_OPEN,

        /**
         * Token closes a multi-line comment
         */
        MULTI_LINE_COMMENT_CLOSE,

        /**
         * Token begins a block
         */
        BLOCK_BEGIN,

        /**
         * Token ends a block
         */
        BLOCK_END
    }
}