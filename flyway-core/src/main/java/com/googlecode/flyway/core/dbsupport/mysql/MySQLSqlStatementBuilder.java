/**
 * Copyright 2010-2013 Axel Fontaine and the many contributors.
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
package com.googlecode.flyway.core.dbsupport.mysql;

import com.googlecode.flyway.core.dbsupport.Delimiter;
import com.googlecode.flyway.core.dbsupport.SqlStatementBuilder;
import com.googlecode.flyway.core.util.StringUtils;

import java.util.regex.Pattern;

/**
 * SqlStatementBuilder supporting MySQL-specific delimiter changes.
 */
public class MySQLSqlStatementBuilder extends SqlStatementBuilder {
    /**
     * The keyword that indicates a change in delimiter.
     */
    private static final String DELIMITER_KEYWORD = "DELIMITER";

    @Override
    public Delimiter extractNewDelimiterFromLine(String line) {
        if (line.toUpperCase().startsWith(DELIMITER_KEYWORD)) {
            return new Delimiter(line.substring(DELIMITER_KEYWORD.length()).trim(), false);
        }

        return null;
    }

    @Override
    protected Delimiter changeDelimiterIfNecessary(String line, Delimiter delimiter) {
        if (line.toUpperCase().startsWith(DELIMITER_KEYWORD)) {
            return new Delimiter(line.substring(DELIMITER_KEYWORD.length()).trim(), false);
        }

        return delimiter;
    }

    @Override
    public boolean isCommentDirective(String line) {
        return line.matches("^" + Pattern.quote("/*!") + "\\d{5} .*" + Pattern.quote("*/") + ";?");
    }

    @Override
    public boolean isSingleLineComment(String line) {
        return line.startsWith("--") || line.startsWith("#");
    }

    @Override
    protected String removeEscapedQuotes(String token) {
        String noEscapedBackslashes = StringUtils.replaceAll(token, "\\\\","");
        String noBackslashEscapes = StringUtils.replaceAll(StringUtils.replaceAll(noEscapedBackslashes, "\\'", ""), "\\\"", "");
        return StringUtils.replaceAll(noBackslashEscapes, "''", "");
    }

    @Override
    protected String extractAlternateOpenQuote(String token) {
        if (token.startsWith("\"")) {
            return "\"";
        }
        if (token.startsWith("B'")) {
            return "B'";
        }
        if (token.startsWith("X'")) {
            return "X'";
        }
        return null;
    }

    @Override
    protected String computeAlternateCloseQuote(String openQuote) {
        if ("B'".equals(openQuote) || "X'".equals(openQuote)) {
            return "'";
        }
        return openQuote;
    }
}
