/*
 * Copyright 2010-2019 Boxfuse GmbH
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
package org.flywaydb.core.internal.parser;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.resource.LoadableResource;
import org.flywaydb.core.internal.resource.Resource;
import org.flywaydb.core.internal.sqlscript.Delimiter;
import org.flywaydb.core.internal.sqlscript.ParsedSqlStatement;
import org.flywaydb.core.internal.sqlscript.SqlStatement;
import org.flywaydb.core.internal.util.BomStrippingReader;
import org.flywaydb.core.internal.util.IOUtils;
import org.flywaydb.core.internal.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Pattern;

public abstract class Parser {
    private static final Log LOG = LogFactory.getLog(Parser.class);








    protected final Configuration configuration;
    private final int peekDepth;
    private final char identifierQuote;
    private final char alternativeStringLiteralQuote;
    private final char alternativeSingleLineComment;
    private final Set<String> validKeywords;

    protected Parser(Configuration configuration, int peekDepth) {
        this.configuration = configuration;
        this.peekDepth = peekDepth;
        this.identifierQuote = getIdentifierQuote();
        this.alternativeStringLiteralQuote = getAlternativeStringLiteralQuote();
        this.alternativeSingleLineComment = getAlternativeSingleLineComment();
        this.validKeywords = getValidKeywords();
    }

    protected char getIdentifierQuote() {
        return '"';
    }

    protected char getAlternativeStringLiteralQuote() {
        return 0;
    }

    protected char getAlternativeSingleLineComment() {
        return 0;
    }

    protected Set<String> getValidKeywords() {
        return null;
    }

    public Iterator<SqlStatement> parse(final LoadableResource resource) {
        final PositionTracker tracker = new PositionTracker();
        final Recorder recorder = new Recorder();
        final ParserContext context = new ParserContext();

        LOG.debug("Parsing " + resource.getFilename() + " ...");
        Reader r = new PositionTrackingReader(tracker, new BufferedReader(new BomStrippingReader(resource.read())));
        if (configuration.isPlaceholderReplacement()) {
            r = new PlaceholderReplacingReader(
                    configuration.getPlaceholderPrefix(),
                    configuration.getPlaceholderSuffix(),
                    configuration.getPlaceholders(),
                    r);
        }
        final PeekingReader peekingReader = new PeekingReader(new RecordingReader(recorder, r));

        return new Iterator<SqlStatement>() {
            private SqlStatement nextStatement = getNextStatement(resource, peekingReader, recorder, tracker, context);

            @Override
            public boolean hasNext() {
                return nextStatement != null;
            }

            @Override
            public SqlStatement next() {
                if (nextStatement == null) {
                    throw new NoSuchElementException("No more statements in " + resource.getFilename());
                }

                SqlStatement result = nextStatement;
                nextStatement = getNextStatement(resource, peekingReader, recorder, tracker, context);
                if (nextStatement == null) {
                    IOUtils.close(peekingReader);
                }
                return result;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove");
            }
        };
    }

    private SqlStatement getNextStatement(Resource resource, PeekingReader reader, Recorder recorder, PositionTracker tracker, ParserContext context) {
        int statementLine = tracker.getLine();
        int statementCol = tracker.getCol();

        try {
            List<Token> tokens = new ArrayList<>();
            List<Token> keywords = new ArrayList<>();

            int statementPos = -1;
            recorder.start();

            int nonCommentPartPos = -1;
            int nonCommentPartLine = -1;
            int nonCommentPartCol = -1;

            StatementType statementType = null;
            Boolean canExecuteInTransaction = null;




            String simplifiedStatement = "";

            do {
                Token token = readToken(reader, tracker, context);
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

                TokenType tokenType = token.getType();
                if (tokenType == TokenType.NEW_DELIMITER) {
                    context.setDelimiter(new Delimiter(token.getText(), false));
                    tokens.clear();
                    recorder.start();
                    statementLine = tracker.getLine();
                    statementCol = tracker.getCol();
                    simplifiedStatement = "";
                    continue;
                }

                if ((tokenType == TokenType.DELIMITER || tokenType == TokenType.BLANK_LINES) && nonCommentPartPos < 0) {
                    tokens.clear();
                    recorder.start();
                    statementLine = tracker.getLine();
                    statementCol = tracker.getCol();
                    simplifiedStatement = "";
                    continue;
                }

                int parensDepth = token.getParensDepth();
                if (tokenType == TokenType.KEYWORD && parensDepth == 0) {
                    keywords.add(token);
                    adjustBlockDepth(context, keywords);
                }

                int blockDepth = context.getBlockDepth();
                if (TokenType.EOF == tokenType
                        || (TokenType.DELIMITER == tokenType && parensDepth == 0 && blockDepth == 0)) {
                    String sql = recorder.stop();
                    if (TokenType.EOF == tokenType && (sql.length() == 0 || tokens.isEmpty() || nonCommentPartPos < 0)) {
                        return null;
                    }
                    if (canExecuteInTransaction == null) {
                        canExecuteInTransaction = true;
                    }





                    if (TokenType.EOF == tokenType && (parensDepth > 0 || blockDepth > 0)) {
                        throw new FlywayException("Incomplete statement at line " + statementLine
                                + " col " + statementCol + ": " + sql);
                    }
                    return createStatement(reader, recorder, statementPos, statementLine, statementCol,
                            nonCommentPartPos, nonCommentPartLine, nonCommentPartCol,
                            statementType, canExecuteInTransaction, context.getDelimiter(), sql



                    );
                }

                if (nonCommentPartPos < 0 && TokenType.COMMENT != tokenType) {
                    nonCommentPartPos = token.getPos();
                    nonCommentPartLine = token.getLine();
                    nonCommentPartCol = token.getCol();
                }
                if (tokens.isEmpty()) {
                    statementPos = token.getPos();
                    statementLine = token.getLine();
                    statementCol = token.getCol();
                }
                tokens.add(token);
                recorder.confirm();

                if (keywords.size() <= 10 && tokenType == TokenType.KEYWORD && parensDepth == 0
                        && (statementType == null || canExecuteInTransaction == null)) {
                    if (!simplifiedStatement.isEmpty()) {
                        simplifiedStatement += " ";
                    }
                    simplifiedStatement += keywordToUpperCase(token.getText());

                    if (statementType == null) {
                        if (keywords.size() > 10) {
                            statementType = StatementType.GENERIC;
                        } else {
                            statementType = detectStatementType(simplifiedStatement);
                        }
                    }
                    if (canExecuteInTransaction == null) {
                        if (keywords.size() > 10) {
                            canExecuteInTransaction = true;
                        } else {
                            canExecuteInTransaction = detectCanExecuteInTransaction(simplifiedStatement);
                        }
                    }





                }
            } while (true);
        } catch (Exception e) {
            IOUtils.close(reader);
            throw new FlywayException("Unable to parse statement in " + resource.getAbsolutePath()
                    + " at line " + statementLine + " col " + statementCol + ": " + e.getMessage(), e);
        }
    }

    protected void adjustBlockDepth(ParserContext context, List<Token> keywords) {}

    static String keywordToUpperCase(String text) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                result.append(c);
            } else {
                result.append((char) (c - ('a' - 'A')));
            }
        }
        return result.toString();
    }

    protected ParsedSqlStatement createStatement(PeekingReader reader, Recorder recorder,
                                                 int statementPos, int statementLine, int statementCol,
                                                 int nonCommentPartPos, int nonCommentPartLine, int nonCommentPartCol,
                                                 StatementType statementType, boolean canExecuteInTransaction,
                                                 Delimiter delimiter, String sql



    ) throws IOException {
        return new ParsedSqlStatement(statementPos, statementLine, statementCol,
                sql, delimiter, canExecuteInTransaction



        );
    }

    protected StatementType detectStatementType(String simplifiedStatement) {
        return null;
    }

    protected Boolean detectCanExecuteInTransaction(String simplifiedStatement) {
        return true;
    }

    private Token readToken(PeekingReader reader, PositionTracker tracker, ParserContext context) throws IOException {
        int pos = tracker.getPos();
        int line = tracker.getLine();
        int col = tracker.getCol();

        String peek = reader.peek(peekDepth);
        if (peek == null) {
            return new Token(TokenType.EOF, pos, line, col, null, 0);
        }
        char c = peek.charAt(0);
        if (alternativeStringLiteralQuote != 0 && c == alternativeStringLiteralQuote) {
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
        if (c == identifierQuote) {
            reader.swallow();
            String text = reader.readUntilExcludingWithEscape(identifierQuote, true);
            if (reader.peek('.')) {
                text = readAdditionalIdentifierParts(reader);
            }
            return new Token(TokenType.IDENTIFIER, pos, line, col, text, context.getParensDepth());
        }
        if (isCommentDirective(peek)) {
            return handleCommentDirective(reader, context, pos, line, col);
        }
        if (peek.startsWith("--") || (alternativeSingleLineComment != 0 && c == alternativeSingleLineComment)) {
            reader.swallowUntilExcluding('\n', '\r');
            return new Token(TokenType.COMMENT, pos, line, col, null, context.getParensDepth());
        }
        if (peek.startsWith("/*")) {
            reader.swallow(2);
            reader.swallowUntilExcluding("*/");
            reader.swallow(2);
            return new Token(TokenType.COMMENT, pos, line, col, null, context.getParensDepth());
        }
        if (isDigit(c)) {
            reader.swallowNumeric();
            return new Token(TokenType.NUMERIC, pos, line, col, null, context.getParensDepth());
        }
        if (peek.startsWith("B'") || peek.startsWith("E'") || peek.startsWith("X'")) {
            reader.swallow(2);
            reader.swallowUntilExcludingWithEscape('\'', true);
            return new Token(TokenType.STRING, pos, line, col, null, context.getParensDepth());
        }
        if (peek.startsWith("U&'")) {
            reader.swallow(3);
            reader.swallowUntilExcludingWithEscape('\'', true);
            return new Token(TokenType.STRING, pos, line, col, null, context.getParensDepth());
        }
        if (c == '_' || Character.isLetter(c)) {
            String text = "" + (char) reader.read() + reader.readKeywordPart();
            if (reader.peek('.')) {
                text += readAdditionalIdentifierParts(reader);
                return new Token(TokenType.IDENTIFIER, pos, line, col, text, context.getParensDepth());
            }
            if (!isKeyword(text)) {
                return new Token(TokenType.IDENTIFIER, pos, line, col, text, context.getParensDepth());
            }
            return handleKeyword(reader, context, pos, line, col, text);
        }
        if (peek.startsWith(context.getDelimiter().getDelimiter())) {
            reader.swallow(context.getDelimiter().getDelimiter().length());
            return new Token(TokenType.DELIMITER, pos, line, col, context.getDelimiter().getDelimiter(), context.getParensDepth());
        }
        if (StringUtils.isCharAnyOf(c, ",=*.:;[]~+-/%^|!@&#<>'{}")) {
            reader.swallow();
            return null;
        }
        if (Character.isWhitespace(c)) {
            String text = reader.readWhitespace();
            if (containsAtLeast(text, '\n', 2)) {
                return new Token(TokenType.BLANK_LINES, pos, line, col, null, context.getParensDepth());
            }
            return null;
        }
        throw new FlywayException("Unknown char " + (char) reader.read() + " encountered on line " + line + " at column " + col);
    }

    final boolean isKeyword(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_')) {
                return false;
            }
        }
        if (validKeywords != null) {
            return validKeywords.contains(text);
        }
        return true;
    }

    @SuppressWarnings("Duplicates")
    private String readAdditionalIdentifierParts(PeekingReader reader) throws IOException {
        String result = "";
        reader.swallow();
        result += ".";
        if (reader.peek(identifierQuote)) {
            reader.swallow();
            result += reader.readUntilExcludingWithEscape(identifierQuote, true);
        } else {
            result += reader.readKeywordPart();
        }
        if (reader.peek('.')) {
            reader.swallow();
            result += ".";
            if (reader.peek(identifierQuote)) {
                reader.swallow();
                result += reader.readUntilExcludingWithEscape(identifierQuote, true);
            } else {
                result += reader.readKeywordPart();
            }
        }
        return result;
    }

    protected boolean isCommentDirective(String peek) {
        return false;
    }

    protected Token handleCommentDirective(PeekingReader reader, ParserContext context, int pos, int line, int col) throws IOException {
        return null;
    }

    protected Token handleStringLiteral(PeekingReader reader, ParserContext context, int pos, int line, int col) throws IOException {
        reader.swallow();
        reader.swallowUntilExcludingWithEscape('\'', true);
        return new Token(TokenType.STRING, pos, line, col, null, context.getParensDepth());
    }

    protected Token handleAlternativeStringLiteral(PeekingReader reader, ParserContext context, int pos, int line, int col) throws IOException {
        return null;
    }

    protected Token handleKeyword(PeekingReader reader, ParserContext context, int pos, int line, int col, String keyword) throws IOException {
        return new Token(TokenType.KEYWORD, pos, line, col, keywordToUpperCase(keyword), context.getParensDepth());
    }

    private static boolean containsAtLeast(String str, char c, int min) {
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

    protected static boolean keywordIs(String expected, String actual) {
        if (expected.length() != actual.length()) {
            return false;
        }
        for (int i = 0; i < expected.length(); i++) {
            char ce = expected.charAt(i);
            char ca = actual.charAt(i);

            if (ce != ca && ce + ('a' - 'A') != ca) {
                return false;
            }
        }

        return true;
    }

    protected static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
}