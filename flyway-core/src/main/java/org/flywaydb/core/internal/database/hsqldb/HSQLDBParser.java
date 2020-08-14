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
package org.flywaydb.core.internal.database.hsqldb;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HSQLDBParser extends Parser {
    /**
     * List of objects which can be dropped with IF EXISTS
     */
    private static final List<String> CONDITIONALLY_CREATABLE_OBJECTS = Arrays.asList(
            "CONSTRAINT", "TABLE", "COLUMN", "INDEX", "SEQUENCE", "VIEW", "SCHEMA"
    );

    public HSQLDBParser(Configuration configuration, ParsingContext parsingContext) {
        super(configuration, parsingContext,2);
    }

    @Override
    protected Set<String> getValidKeywords() {
        return new HashSet<>(Arrays.asList(
                "ABS", "ALL", "ALLOCATE", "ALTER", "AND", "ANY", "ARE", "ARRAY", "AS", "ASENSITIVE", "ASYMMETRIC", "AT", "ATOMIC", "AUTHORIZATION", "AVG",
                "BEGIN", "BETWEEN", "BIGINT", "BINARY", "BLOB", "BOOLEAN", "BOTH", "BY",
                "CALL", "CALLED", "CARDINALITY", "CASCADED", "CASE", "CAST", "CEIL", "CEILING", "CHAR", "CHAR_LENGTH", "CHARACTER", "CHARACTER_LENGTH", "CHECK", "CLOB", "CLOSE", "COALESCE", "COLLATE", "COLLECT", "COLUMN", "COMMIT", "COMPARABLE", "CONDITION", "CONNECT", "CONSTRAINT", "CONVERT", "CORR", "CORRESPONDING", "COUNT", "COVAR_POP", "COVAR_SAMP", "CREATE", "CROSS", "CUBE", "CUME_DIST", "CURRENT", "CURRENT_CATALOG", "CURRENT_DATE", "CURRENT_DEFAULT_TRANSFORM_GROUP", "CURRENT_PATH", "CURRENT_ROLE", "CURRENT_SCHEMA", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_TRANSFORM_GROUP_FOR_TYPE", "CURRENT_USER", "CURSOR", "CYCLE",
                "DATE", "DAY", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DELETE", "DENSE_RANK", "DEREF", "DESCRIBE", "DETERMINISTIC", "DISCONNECT", "DISTINCT", "DO", "DOUBLE", "DROP", "DYNAMIC",
                "EACH", "ELEMENT", "ELSE", "ELSEIF", "END", "END_EXEC", "ESCAPE", "EVERY", "EXCEPT", "EXEC", "EXECUTE", "EXISTS", "EXIT", "EXP", "EXTERNAL", "EXTRACT",
                "FALSE", "FETCH", "FILTER", "FIRST_VALUE", "FLOAT", "FLOOR", "FOR", "FOREIGN", "FREE", "FROM", "FULL", "FUNCTION", "FUSION",
                "GET", "GLOBAL", "GRANT", "GROUP", "GROUPING",
                "HANDLER", "HAVING", "HOLD", "HOUR",
                "IDENTITY", "IF", "IN", "INDEX", "INDICATOR", "INNER", "INOUT", "INSENSITIVE", "INSERT", "INT", "INTEGER", "INTERSECT", "INTERSECTION", "INTERVAL", "INTO", "IS", "ITERATE",
                "JOIN",
                "LAG",
                "LANGUAGE", "LARGE", "LAST_VALUE", "LATERAL", "LEAD", "LEADING", "LEAVE", "LEFT", "LIKE", "LIKE_REGEX", "LN", "LOCAL", "LOCALTIME", "LOCALTIMESTAMP", "LOOP", "LOWER",
                "MATCH", "MAX", "MAX_CARDINALITY", "MEMBER", "MERGE", "METHOD", "MIN", "MINUTE", "MOD", "MODIFIES", "MODULE", "MONTH", "MULTISET",
                "NATIONAL", "NATURAL", "NCHAR", "NCLOB", "NEW", "NO", "NONE", "NORMALIZE", "NOT", "NTH_VALUE", "NTILE", "NULL", "NULLIF", "NUMERIC",
                "OCCURRENCES_REGEX", "OCTET_LENGTH", "OF", "OFFSET", "OLD", "ON", "ONLY", "OPEN", "OR", "ORDER", "OUT", "OUTER", "OVER", "OVERLAPS", "OVERLAY",
                "PARAMETER", "PARTITION", "PERCENT_RANK", "PERCENTILE_CONT", "PERCENTILE_DISC", "PERIOD", "POSITION", "POSITION_REGEX", "POWER", "PRECISION", "PREPARE", "PRIMARY", "PROCEDURE",
                "RANGE", "RANK", "READS", "REAL", "RECURSIVE", "REF", "REFERENCES", "REFERENCING", "REGR_AVGX", "REGR_AVGY", "REGR_COUNT", "REGR_INTERCEPT", "REGR_R2", "REGR_SLOPE", "REGR_SXX", "REGR_SXY", "REGR_SYY", "RELEASE", "REPEAT", "RESIGNAL", "RESULT", "RETURN", "RETURNS", "REVOKE", "RIGHT", "ROLLBACK", "ROLLUP", "ROW", "ROW_NUMBER", "ROWS",
                "SAVEPOINT", "SCHEMA", "SCOPE", "SCROLL", "SEARCH", "SECOND", "SELECT", "SENSITIVE", "SEQUENCE", "SESSION_USER", "SET", "SIGNAL", "SIMILAR", "SMALLINT", "SOME", "SPECIFIC", "SPECIFICTYPE", "SQL", "SQLEXCEPTION", "SQLSTATE", "SQLWARNING", "SQRT", "STACKED", "START", "STATIC", "STDDEV_POP", "STDDEV_SAMP", "SUBMULTISET", "SUBSTRING", "SUBSTRING_REGEX", "SUM", "SYMMETRIC", "SYSTEM", "SYSTEM_USER",
                "TABLE", "TABLESAMPLE", "THEN", "TIME", "TIMESTAMP", "TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TO", "TRAILING", "TRANSLATE", "TRANSLATE_REGEX", "TRANSLATION", "TREAT", "TRIGGER", "TRIM", "TRIM_ARRAY", "TRUE", "TRUNCATE",
                "UESCAPE", "UNDO", "UNION", "UNIQUE", "UNKNOWN", "UNNEST", "UNTIL", "UPDATE", "UPPER", "USER", "USING",
                "VALUE", "VALUES", "VAR_POP", "VAR_SAMP", "VARBINARY", "VARCHAR", "VARYING", "VIEW",
                "WHEN", "WHENEVER", "WHERE", "WIDTH_BUCKET", "WINDOW", "WITH", "WITHIN", "WITHOUT", "WHILE",
                "YEAR"
        ));
    }

    @Override
    protected void adjustBlockDepth(ParserContext context, List<Token> tokens, Token keyword, PeekingReader reader) throws IOException {
        int lastKeywordIndex = getLastKeywordIndex(tokens);
        Token previousKeyword = lastKeywordIndex >= 0 ? tokens.get(lastKeywordIndex) : null;
        String keywordText = keyword.getText();
        String previousKeywordText = previousKeyword != null ? previousKeyword.getText() : "";

        if ("BEGIN".equals(keywordText)
                || ((("IF".equals(keywordText) && !CONDITIONALLY_CREATABLE_OBJECTS.contains(previousKeywordText))  // excludes the IF in eg. CREATE TABLE IF EXISTS
                || "FOR".equals(keywordText)
                || "CASE".equals(keywordText))
                && previousKeyword != null && !"END".equals(previousKeywordText))) {
            context.increaseBlockDepth(keywordText);
        } else if (("EACH".equals(keywordText) || "SQLEXCEPTION".equals(keywordText))
                && previousKeyword != null
                && "FOR".equals(previousKeywordText)) {
            context.decreaseBlockDepth();
        } else if ("END".equals(keywordText)) {
            context.decreaseBlockDepth();
        }
    }
}