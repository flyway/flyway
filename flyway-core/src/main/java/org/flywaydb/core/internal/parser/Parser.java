/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.core.internal.parser;

import lombok.CustomLog;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.api.resource.Resource;
import org.flywaydb.core.internal.resource.ResourceName;
import org.flywaydb.core.internal.resource.ResourceNameParser;
import org.flywaydb.core.internal.sqlscript.*;
import org.flywaydb.core.internal.util.BomStrippingReader;
import org.flywaydb.core.internal.util.IOUtils;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.regex.Pattern;

/**
 * The main parser all database-specific parsers derive from.
 */
@CustomLog
public abstract class Parser {
    /**
     * Regex to determine whether this statement can be run as part of batch or whether it must be run individually.
     */
    private static final Pattern BATCHABLE_REGEX = Pattern.compile("^(INSERT|UPDATE|DELETE|UPSERT|MERGE)");

    public final Configuration configuration;
    private final int peekDepth;
    private final char identifierQuote;
    private final char alternativeIdentifierQuote;
    private final char alternativeStringLiteralQuote;
    private final Set<String> validKeywords;
    public final ParsingContext parsingContext;

    protected Parser(final Configuration configuration, final ParsingContext parsingContext, final int peekDepth) {
        this.configuration = configuration;
        this.peekDepth = peekDepth;
        this.identifierQuote = getIdentifierQuote();
        this.alternativeIdentifierQuote = getAlternativeIdentifierQuote();
        this.alternativeStringLiteralQuote = getAlternativeStringLiteralQuote();
        this.validKeywords = getValidKeywords();
        this.parsingContext = parsingContext;
    }

    protected Delimiter getDefaultDelimiter() {
        return Delimiter.SEMICOLON;
    }

    protected char getIdentifierQuote() {
        return '"';
    }

    protected char getAlternativeIdentifierQuote() {
        return 0;
    }

    protected char getAlternativeStringLiteralQuote() {
        return 0;
    }

    protected char getOpeningIdentifierSymbol() {
        return 0;
    }

    protected char getClosingIdentifierSymbol() {
        return 0;
    }

    protected Set<String> getValidKeywords() {
        return null;
    }

    protected boolean supportsPeekingMultipleLines() {
        return true;
    }

    public final SqlStatementIterator parse(final LoadableResource resource) {
        return parse(resource, null);
    }

    /**
     * Parses this resource into a stream of statements.
     *
     * @param resource The resource to parse.
     * @param metadata The resource's metadata.
     * @return The statements.
     */
    public final SqlStatementIterator parse(final LoadableResource resource, final SqlScriptMetadata metadata) {
        final PositionTracker tracker = new PositionTracker();
        final Recorder recorder = new Recorder();
        final ParserContext context = new ParserContext(getDefaultDelimiter());

        final String filename = resource.getFilename();
        LOG.debug("Parsing " + filename + " ...");

        final ResourceName result = new ResourceNameParser(configuration).parse(filename);
        parsingContext.updateFilenamePlaceholder(result, configuration);

        final PeekingReader peekingReader = new PeekingReader(new RecordingReader(recorder,
            new PositionTrackingReader(tracker,
                replacePlaceholders(new BomStrippingReader(new UnboundedReadAheadReader(new BufferedReader(resource.read(),
                    4096))), metadata))), supportsPeekingMultipleLines());

        return new ParserSqlStatementIterator(peekingReader, resource, recorder, tracker, context);
    }

    /**
     * Configures this reader for placeholder replacement.
     *
     * @param reader   The original reader.
     * @param metadata The resource's metadata.
     * @return The new reader with placeholder replacement.
     */
    protected Reader replacePlaceholders(final Reader reader, final SqlScriptMetadata metadata) {
        if ((metadata == null || metadata.placeholderReplacement() == null)
            ? configuration.isPlaceholderReplacement()
            : metadata.placeholderReplacement()) {
            return PlaceholderReplacingReader.create(configuration, parsingContext, reader);
        }
        return reader;
    }

    protected SqlStatement getNextStatement(final Resource resource,
        final PeekingReader reader,
        final Recorder recorder,
        final PositionTracker tracker,
        final ParserContext context) {
        resetDelimiter(context);
        context.setStatementType(StatementType.UNKNOWN);

        int statementLine = tracker.getLine();
        int statementCol = tracker.getCol();

        try {
            final List<Token> tokens = new ArrayList<>();
            final List<Token> keywords = new ArrayList<>();

            int statementPos = -1;
            recorder.start();

            int nonCommentPartPos = -1;
            int nonCommentPartLine = -1;
            int nonCommentPartCol = -1;

            StatementType statementType = StatementType.UNKNOWN;
            Boolean canExecuteInTransaction = null;
            Boolean batchable = null;

            String simplifiedStatement = "";

            do {
                final Token token = readToken(reader, tracker, context);
                if (token == null) {
                    if (tokens.isEmpty()) {
                        recorder.start();
                        statementLine = tracker.getLine();
                        statementCol = tracker.getCol();
                        simplifiedStatement = "";
                    } else {
                        recorder.confirm();
                    }
                    continue;
                }

                final TokenType tokenType = token.getType();
                if (tokenType == TokenType.NEW_DELIMITER) {
                    if (!tokens.isEmpty() && nonCommentPartPos >= 0) {
                        final String sql = recorder.stop();
                        throw new FlywayException("Delimiter changed inside statement at line "
                            + statementLine
                            + " col "
                            + statementCol
                            + ": "
                            + sql);
                    }

                    context.setDelimiter(new Delimiter(token.getText(), false, null));
                    tokens.clear();
                    recorder.start();
                    statementLine = tracker.getLine();
                    statementCol = tracker.getCol();
                    simplifiedStatement = "";
                    continue;
                }

                if (shouldDiscard(token, nonCommentPartPos >= 0)) {
                    tokens.clear();
                    recorder.start();
                    statementLine = tracker.getLine();
                    statementCol = tracker.getCol();
                    simplifiedStatement = "";
                    continue;
                }

                if (shouldAdjustBlockDepth(context, tokens, token)) {
                    if (tokenType == TokenType.KEYWORD) {
                        keywords.add(token);
                    }
                    adjustBlockDepth(context, tokens, token, reader);
                }

                final int parensDepth = token.getParensDepth();
                final int blockDepth = context.getBlockDepth();
                if (TokenType.EOF == tokenType || (TokenType.DELIMITER == tokenType
                    && parensDepth == 0
                    && blockDepth == 0)) {
                    final String sql = recorder.stop();
                    if (TokenType.EOF == tokenType && (sql.trim().isEmpty()
                        || tokens.isEmpty()
                        || nonCommentPartPos < 0)) {
                        return null;
                    }
                    if (canExecuteInTransaction == null) {
                        canExecuteInTransaction = determineCanExecuteInTransaction(simplifiedStatement, keywords, true);
                    }
                    if (batchable == null) {
                        batchable = false;
                    }
                    if (TokenType.EOF == tokenType && (parensDepth > 0 || blockDepth > 0)) {
                        throw new FlywayException("Incomplete statement at line "
                            + statementLine
                            + " col "
                            + statementCol
                            + ": "
                            + sql);
                    }
                    return createStatement(reader,
                        recorder,
                        statementPos,
                        statementLine,
                        statementCol,
                        nonCommentPartPos,
                        nonCommentPartLine,
                        nonCommentPartCol,
                        statementType,
                        canExecuteInTransaction,
                        context.getDelimiter(),
                        sql.trim(),
                        discardBlankLines(tokens),
                        batchable);
                }

                if (tokens.isEmpty() || tokens.stream()
                    .allMatch(t -> t.getType() == TokenType.BLANK_LINES || t.getType() == TokenType.COMMENT)) {
                    nonCommentPartPos = -1;
                    nonCommentPartLine = -1;
                    nonCommentPartCol = -1;

                    statementPos = token.getPos();
                    statementLine = token.getLine();
                    statementCol = token.getCol();
                }
                tokens.add(token);
                recorder.confirm();
                if (nonCommentPartPos < 0
                    && TokenType.COMMENT != tokenType
                    && TokenType.DELIMITER != tokenType
                    && TokenType.BLANK_LINES != tokenType) {
                    nonCommentPartPos = token.getPos();
                    nonCommentPartLine = token.getLine();
                    nonCommentPartCol = token.getCol();
                }

                if (keywords.size() <= getTransactionalDetectionCutoff() && (tokenType == TokenType.KEYWORD
                    || "@".equals(token.getText())) && parensDepth == 0 && (statementType == StatementType.UNKNOWN
                    || canExecuteInTransaction == null)) {
                    if (!simplifiedStatement.isEmpty()) {
                        simplifiedStatement += " ";
                    }
                    simplifiedStatement += token.getText().toUpperCase(Locale.ENGLISH);

                    if (statementType == StatementType.UNKNOWN) {
                        if (keywords.size() > getTransactionalDetectionCutoff()) {
                            statementType = StatementType.GENERIC;
                        } else {
                            statementType = detectStatementType(simplifiedStatement, context, reader);
                            context.setStatementType(statementType);
                        }
                        adjustDelimiter(context, statementType);
                    }
                    if (canExecuteInTransaction == null) {
                        canExecuteInTransaction = determineCanExecuteInTransaction(simplifiedStatement, keywords, null);
                    }
                    if (batchable == null) {
                        batchable = BATCHABLE_REGEX.matcher(simplifiedStatement).matches();
                    }
                }
            } while (true);
        } catch (Exception e) {
            IOUtils.close(reader);
            throw new FlywayException("Unable to parse statement in "
                + resource.getAbsolutePath()
                + " at line "
                + statementLine
                + " col "
                + statementCol
                + ". See "
                + FlywayDbWebsiteLinks.KNOWN_PARSER_LIMITATIONS
                + " for more information. "
                + getAdditionalParsingErrorInfo()
                + e.getMessage(), e);
        }
    }

    protected boolean shouldAdjustBlockDepth(final ParserContext context, final List<Token> tokens, final Token token) {
        return token.getType() == TokenType.KEYWORD && token.getParensDepth() == 0;
    }

    /**
     * Whether the current set of tokens should be discarded.
     *
     * @param token              The latest token.
     * @param nonCommentPartSeen Whether a non-comment part has already been seen.
     * @return {@code true} if it should, {@code false} if not.
     */
    protected boolean shouldDiscard(final Token token, final boolean nonCommentPartSeen) {
        return token.getType() == TokenType.DELIMITER && !nonCommentPartSeen;
    }

    /**
     * Resets the delimiter to its default value before parsing a new statement.
     */
    protected void resetDelimiter(final ParserContext context) {
        context.setDelimiter(getDefaultDelimiter());
    }

    /**
     * Adjusts the delimiter if necessary for this statement type.
     *
     * @param statementType The statement type.
     */
    protected void adjustDelimiter(final ParserContext context, final StatementType statementType) {}

    /**
     * @return The cutoff point in terms of number of tokens after which a statement can no longer be non-transactional.
     */
    protected int getTransactionalDetectionCutoff() {
        return 10;
    }

    protected void adjustBlockDepth(final ParserContext context,
        final List<Token> tokens,
        final Token keyword,
        final PeekingReader reader) throws IOException {}

    protected int getLastKeywordIndex(final List<Token> tokens) {
        return getLastKeywordIndex(tokens, tokens.size());
    }

    protected int getLastKeywordIndex(final List<Token> tokens, final int endIndex) {
        for (int i = endIndex - 1; i >= 0; i--) {
            final Token token = tokens.get(i);
            if (token.getType() == TokenType.KEYWORD) {
                return i;
            }
        }
        return -1;
    }

    protected static Token getPreviousToken(final List<Token> tokens, final int parensDepth) {
        for (int i = tokens.size() - 1; i >= 0; i--) {
            final Token previousToken = tokens.get(i);

            // Only consider tokens at the same parenthesis depth in same parenthesis group
            if (previousToken.getParensDepth() < parensDepth) {
                break;
            } else if (previousToken.getParensDepth() != parensDepth) {
                continue;
            }

            // Skip over comments and blank lines
            if (previousToken.getType() == TokenType.COMMENT || previousToken.getType() == TokenType.BLANK_LINES) {
                continue;
            }

            return previousToken;
        }

        return null;
    }

    protected static boolean lastTokenIs(final List<Token> tokens, final int parensDepth, final String tokenText) {
        final Token previousToken = getPreviousToken(tokens, parensDepth);
        if (previousToken == null) {
            return false;
        }

        return tokenText.equals(previousToken.getText());
    }

    protected static boolean lastTokenIsOnLine(final List<Token> tokens, final int parensDepth, final int line) {
        final Token previousToken = getPreviousToken(tokens, parensDepth);
        if (previousToken == null) {
            return false;
        }

        return previousToken.getLine() == line;
    }

    protected static boolean tokenAtIndexIs(final List<Token> tokens, final int index, final String tokenText) {
        return tokens.get(index).getText().equals(tokenText);
    }

    protected boolean doTokensMatchPattern(final List<Token> previousTokens, final Token current, final Pattern regex) {
        final ArrayList<String> tokenStrings = new ArrayList<>();
        tokenStrings.add(current.getText());

        for (int i = previousTokens.size() - 1; i >= 0; i--) {
            final Token prevToken = previousTokens.get(i);
            if (prevToken.getParensDepth() != current.getParensDepth()) {
                break;
            }

            if (prevToken.getType() == TokenType.KEYWORD) {
                tokenStrings.add(prevToken.getText());
            }
        }

        final StringBuilder builder = new StringBuilder();
        for (int i = tokenStrings.size() - 1; i >= 0; i--) {
            builder.append(tokenStrings.get(i));
            if (i != 0) {
                builder.append(" ");
            }
        }

        return regex.matcher(builder.toString()).matches();
    }

    protected ParsedSqlStatement createStatement(final PeekingReader reader,
        final Recorder recorder,
        final int statementPos,
        final int statementLine,
        final int statementCol,
        final int nonCommentPartPos,
        final int nonCommentPartLine,
        final int nonCommentPartCol,
        final StatementType statementType,
        final boolean canExecuteInTransaction,
        final Delimiter delimiter,
        final String sql,
        final List<Token> tokens,
        final boolean batchable) throws IOException {
        return new ParsedSqlStatement(statementPos,
            statementLine,
            statementCol,
            sql,
            delimiter,
            canExecuteInTransaction,
            batchable);
    }

    protected StatementType detectStatementType(final String simplifiedStatement,
        final ParserContext context,
        final PeekingReader reader) {
        return StatementType.UNKNOWN;
    }

    private Boolean determineCanExecuteInTransaction(final String simplifiedStatement,
        final List<Token> keywords,
        final Boolean defaultValue) {
        if (keywords.size() > getTransactionalDetectionCutoff()) {
            return true;
        } else {
            Boolean canExecuteInTransaction = detectCanExecuteInTransaction(simplifiedStatement, keywords);
            if (canExecuteInTransaction == null) {
                canExecuteInTransaction = defaultValue;
            }
            return canExecuteInTransaction;
        }
    }

    protected Boolean detectCanExecuteInTransaction(final String simplifiedStatement, final List<Token> keywords) {
        return true;
    }

    public boolean supportsReferencedSqlScripts() {
        return false;
    }

    public boolean includeReferencedScriptsInChecksum() {
        return true;
    }

    private Token readToken(final PeekingReader reader, final PositionTracker tracker, final ParserContext context)
        throws IOException {
        final int pos = tracker.getPos();
        final int line = tracker.getLine();
        final int col = tracker.getCol();
        final int colIgnoringWhitepace = tracker.getColIgnoringWhitespace();

        final String peek = reader.peek(peekDepth);
        if (peek == null) {
            return new Token(TokenType.EOF, pos, line, col, null, null, 0);
        }
        final char c = peek.charAt(0);
        if (isAlternativeStringLiteral(peek)) {
            return handleAlternativeStringLiteral(reader, context, pos, line, col);
        }
        if (c == '\'') {
            return handleStringLiteral(reader, context, pos, line, col);
        }
        if (c == '(') {
            context.increaseParensDepth();
            reader.swallow();
            return null;
        }
        if (c == ')') {
            context.decreaseParensDepth();
            reader.swallow();
            return null;
        }
        if (c == identifierQuote || c == alternativeIdentifierQuote) {
            reader.swallow();
            String text = reader.readUntilExcludingWithEscape(c, true);
            if (reader.peek('.')) {
                text = readAdditionalIdentifierParts(reader, c, context.getDelimiter(), context);
            }
            return new Token(TokenType.IDENTIFIER, pos, line, col, text, text, context.getParensDepth());
        }
        if (isCommentDirective(peek)) {
            return handleCommentDirective(reader, context, pos, line, col);
        }
        if (isSingleLineComment(peek, context, col)) {
            final String text = reader.readUntilExcluding('\n', '\r');
            return new Token(TokenType.COMMENT, pos, line, col, text, text, context.getParensDepth());
        }
        if (peek.startsWith("/*")) {
            return handleMultilineComment(reader, context, pos, line, col);
        }
        if (Character.isDigit(c)) {
            final String text = reader.readNumeric();
            return new Token(TokenType.NUMERIC, pos, line, col, text, text, context.getParensDepth());
        }
        if (peek.startsWith("B'") || peek.startsWith("E'") || peek.startsWith("X'")) {
            reader.swallow(2);
            reader.swallowUntilIncludingWithEscape('\'', true, '\\');
            return new Token(TokenType.STRING, pos, line, col, null, null, context.getParensDepth());
        }
        if (peek.startsWith("U&'")) {
            reader.swallow(3);
            reader.swallowUntilIncludingWithEscape('\'', true);
            return new Token(TokenType.STRING, pos, line, col, null, null, context.getParensDepth());
        }
        if (isDelimiter(peek, context, col, colIgnoringWhitepace)) {
            return handleDelimiter(reader, context, pos, line, col);
        }
        if (isOpeningIdentifier(c)) {
            final String text = readIdentifier(reader);
            return new Token(TokenType.IDENTIFIER, pos, line, col, text, text, context.getParensDepth());
        }
        if (isLetter(c, context)) {
            String text = readKeyword(reader, context.getDelimiter(), context);
            if (reader.peek('.')) {
                text += readAdditionalIdentifierParts(reader, identifierQuote, context.getDelimiter(), context);
            }
            if (!isKeyword(text)) {
                return new Token(TokenType.IDENTIFIER, pos, line, col, text, text, context.getParensDepth());
            }
            return handleKeyword(reader, context, pos, line, col, text);
        }
        if (c == ' ' || c == '\r' || c == '\u00A0' /* Non-linebreaking space */) {
            reader.swallow();
            return null;
        }
        if (Character.isWhitespace(c)) {
            final String text = reader.readWhitespace();
            if (containsAtLeast(text, '\n', 2)) {
                return new Token(TokenType.BLANK_LINES, pos, line, col, text, text, context.getParensDepth());
            }
            return null;
        }

        String text = "" + (char) reader.read();
        return new Token(TokenType.SYMBOL, pos, line, col, text, text, context.getParensDepth());
    }

    protected String readKeyword(final PeekingReader reader, final Delimiter delimiter, final ParserContext context)
        throws IOException {
        return "" + (char) reader.read() + reader.readKeywordPart(delimiter, context);
    }

    protected String readIdentifier(final PeekingReader reader) throws IOException {
        return "" + (char) reader.read() + reader.readUntilIncluding(getClosingIdentifierSymbol());
    }

    protected Token handleDelimiter(final PeekingReader reader,
        final ParserContext context,
        final int pos,
        final int line,
        final int col) throws IOException {
        final String text = context.getDelimiter().getDelimiter();
        reader.swallow(text.length());
        return new Token(TokenType.DELIMITER, pos, line, col, text, text, context.getParensDepth());
    }

    protected boolean isAlternativeStringLiteral(final String peek) {
        return alternativeStringLiteralQuote != 0 && peek.charAt(0) == alternativeStringLiteralQuote;
    }

    protected boolean isDelimiter(final String peek,
        final ParserContext context,
        final int col,
        final int colIgnoringWhitespace) {
        return peek.startsWith(context.getDelimiter().getDelimiter());
    }

    protected boolean isLetter(final char c, final ParserContext context) {
        return (c == '_' || context.isLetter(c));
    }

    private boolean isOpeningIdentifier(final char c) {
        return c == getOpeningIdentifierSymbol();
    }

    protected boolean isSingleLineComment(final String peek, final ParserContext context, final int col) {
        return peek.startsWith("--");
    }

    protected boolean isKeyword(final String text) {
        for (int i = 0; i < text.length(); i++) {
            final char c = text.charAt(i);
            if (!(Character.isLetter(c) || c == '_')) {
                return false;
            }
        }
        if (validKeywords != null) {
            return validKeywords.contains(text.toUpperCase(Locale.ENGLISH));
        }
        return true;
    }

    @SuppressWarnings("Duplicates")
    private String readAdditionalIdentifierParts(final PeekingReader reader,
        final char quote,
        final Delimiter delimiter,
        final ParserContext context) throws IOException {
        String result = "";
        reader.swallow();
        result += ".";
        if (reader.peek(quote)) {
            reader.swallow();
            result += reader.readUntilExcludingWithEscape(quote, true);
        } else {
            result += reader.readKeywordPart(delimiter, context);
        }
        if (reader.peek('.')) {
            reader.swallow();
            result += ".";
            if (reader.peek(quote)) {
                reader.swallow();
                result += reader.readUntilExcludingWithEscape(quote, true);
            } else {
                result += reader.readKeywordPart(delimiter, context);
            }
        }
        return result;
    }

    private List<Token> discardBlankLines(final List<Token> tokens) {
        final List<Token> nonBlankLinesTokens = new ArrayList<>(tokens);
        while (nonBlankLinesTokens.get(0).getType() == TokenType.BLANK_LINES) {
            nonBlankLinesTokens.remove(0);
        }
        while (nonBlankLinesTokens.get(nonBlankLinesTokens.size() - 1).getType() == TokenType.BLANK_LINES) {
            nonBlankLinesTokens.remove(nonBlankLinesTokens.size() - 1);
        }
        return nonBlankLinesTokens;
    }

    protected boolean isCommentDirective(final String peek) {
        return false;
    }

    protected Token handleCommentDirective(final PeekingReader reader,
        final ParserContext context,
        final int pos,
        final int line,
        final int col) throws IOException {
        return null;
    }

    protected Token handleMultilineComment(final PeekingReader reader,
        final ParserContext context,
        final int pos,
        final int line,
        final int col) throws IOException {
        int commentDepth = 0;
        reader.swallow("/*".length());
        final StringBuilder text = new StringBuilder(reader.readUntilExcluding("*/", "/*"));
        while (reader.peek("/*") || commentDepth > 0) {
            if (reader.peek("/*")) {
                commentDepth++;
            } else {
                commentDepth--;
            }
            reader.swallow(2);
            text.append(reader.readUntilExcluding("*/", "/*"));
        }
        reader.swallow(2);
        return new Token(TokenType.COMMENT, pos, line, col, text.toString(), text.toString(), context.getParensDepth());
    }

    protected Token handleStringLiteral(final PeekingReader reader,
        final ParserContext context,
        final int pos,
        final int line,
        final int col) throws IOException {
        reader.swallow();
        reader.swallowUntilIncludingWithEscape('\'', true);
        return new Token(TokenType.STRING, pos, line, col, null, null, context.getParensDepth());
    }

    protected Token handleAlternativeStringLiteral(final PeekingReader reader,
        final ParserContext context,
        final int pos,
        final int line,
        final int col) throws IOException {
        return null;
    }

    protected Token handleKeyword(final PeekingReader reader,
        final ParserContext context,
        final int pos,
        final int line,
        final int col,
        final String keyword) throws IOException {
        return new Token(TokenType.KEYWORD,
            pos,
            line,
            col,
            keyword.toUpperCase(Locale.ENGLISH),
            keyword,
            context.getParensDepth());
    }

    protected String getAdditionalParsingErrorInfo() {
        return "";
    }

    private static boolean containsAtLeast(final String str, final char c, final int min) {
        if (min > str.length()) {
            return false;
        }

        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == c) {
                count++;
                if (count >= min) {
                    return true;
                }
            }
        }
        return false;
    }

    public class ParserSqlStatementIterator implements SqlStatementIterator {
        private final PeekingReader peekingReader;
        private final LoadableResource resource;
        private final Recorder recorder;
        private final PositionTracker tracker;
        private final ParserContext context;

        private SqlStatement nextStatement;
        private boolean needToRefreshNextStatement = true;

        public ParserSqlStatementIterator(final PeekingReader peekingReader,
            final LoadableResource resource,
            final Recorder recorder,
            final PositionTracker tracker,
            final ParserContext context) {
            this.peekingReader = peekingReader;
            this.resource = resource;
            this.recorder = recorder;
            this.tracker = tracker;
            this.context = context;
        }

        @Override
        public void close() {
            IOUtils.close(peekingReader);
        }

        @Override
        public boolean hasNext() {
            if (needToRefreshNextStatement) {
                nextStatement = getNextStatement(resource, peekingReader, recorder, tracker, context);
                needToRefreshNextStatement = false;
            }
            return nextStatement != null;
        }

        @Override
        public SqlStatement next() {
            if (needToRefreshNextStatement) {
                nextStatement = getNextStatement(resource, peekingReader, recorder, tracker, context);
            }
            needToRefreshNextStatement = true;
            return nextStatement;
        }
    }
}
