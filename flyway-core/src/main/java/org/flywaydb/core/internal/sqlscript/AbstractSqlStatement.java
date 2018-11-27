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

import java.lang.ref.SoftReference;
import java.util.List;
import java.util.Locale;

/**
 * A sql statement from a script that can be executed at once against a database.
 */
public abstract class AbstractSqlStatement implements SqlStatement {
    /**
     * The lines of the statement.
     */
    protected final List<Line> lines;

    /**
     * The delimiter of the statement.
     */
    private final Delimiter delimiter;








    private SoftReference<String> sqlRef;

    public AbstractSqlStatement(List<Line> lines, Delimiter delimiter



    ) {
        this.lines = lines;
        this.delimiter = delimiter;



    }

    @Override
    public final int getLineNumber() {
        return lines.get(0).getLineNumber();
    }

    @Override
    public final String getSql() {
        String sql = sqlRef == null ? null : sqlRef.get();
        if (sql == null) {
            StringBuilder sqlBuilder = new StringBuilder();
            for (Line line : lines) {
                sqlBuilder.append(getLine(line));
            }
            stripDelimiter(sqlBuilder, delimiter);

            sql = sqlBuilder.toString();
            sqlRef = new SoftReference<String>(sql);
        }
        return sql;
    }

    protected String getLine(Line line) {
        return line.getLine();
    }

    @Override
    public String getDelimiter() {
        return delimiter.toString();
    }

    /**
     * Strips this delimiter from this sql statement.
     *
     * @param sql       The statement to parse.
     * @param delimiter The delimiter to strip.
     */
    protected void stripDelimiter(StringBuilder sql, Delimiter delimiter) {
        stripTrailingWhitespace(sql);

        int length = delimiter.getDelimiter().length();
        if (length > sql.length()) {
            // String is shorter than delimiter
            return;
        }

        String actualDelimiter = sql.substring(sql.length() - length, sql.length());
        if (actualDelimiter.toUpperCase(Locale.ENGLISH).equals(delimiter.getDelimiter().toUpperCase(Locale.ENGLISH))) {
            sql.delete(sql.length() - length, sql.length());
        }
    }

    protected void stripTrailingWhitespace(StringBuilder sql) {
        while (Character.isWhitespace(sql.charAt(sql.length() - 1))) {
            sql.delete(sql.length() - 1, sql.length());
        }
    }












}