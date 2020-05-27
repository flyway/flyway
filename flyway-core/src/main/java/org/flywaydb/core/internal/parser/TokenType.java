/*
 * Copyright 2010-2020 Redgate Software Ltd
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

public enum TokenType {
    KEYWORD,

    /**
     * An identifier, referring to a schema object like a table or column.
     */
    IDENTIFIER,

    NUMERIC,

    /**
     * A string literal.
     */
    STRING,

    /**
     * A comment in front of or within a statement. Can be single line (--) or multi-line (/* *&#47;).
     */
    COMMENT,

    /**
     * An actual statement disguised as a multi-line comment.
     */
    MULTI_LINE_COMMENT_DIRECTIVE,

    /**
     * (
     */
    PARENS_OPEN,

    /**
     * )
     */
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