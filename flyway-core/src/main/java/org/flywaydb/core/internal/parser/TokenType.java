package org.flywaydb.core.internal.parser;

public enum TokenType {
    KEYWORD,
    /**
     * An identifier, referring to a schema object like a table or column.
     */
    IDENTIFIER,
    NUMERIC,
    STRING,
    /**
     * A comment in front of or within a statement. Can be single line (--) or multi-line (/* *&#47;).
     */
    COMMENT,
    /**
     * An actual statement disguised as a multi-line comment.
     */
    MULTI_LINE_COMMENT_DIRECTIVE,
    PARENS_OPEN,
    PARENS_CLOSE,
    DELIMITER,
    /**
     * The new delimiter that will be used from now on.
     */
    NEW_DELIMITER,
    /**
     * A symbol such as ! or #.
     */
    SYMBOL,
    BLANK_LINES,
    EOF,
    COPY_DATA
}