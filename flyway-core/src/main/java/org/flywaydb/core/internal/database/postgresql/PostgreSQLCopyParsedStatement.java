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
package org.flywaydb.core.internal.database.postgresql;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.Result;
import org.flywaydb.core.internal.jdbc.Results;
import org.flywaydb.core.internal.sqlscript.Delimiter;
import org.flywaydb.core.internal.sqlscript.ParsedSqlStatement;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutor;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * A PostgreSQL COPY FROM STDIN statement.
 */
public class PostgreSQLCopyParsedStatement extends ParsedSqlStatement {
    /**
     * Delimiter of COPY statements.
     */
    private static final Delimiter COPY_DELIMITER = new Delimiter("\\.", true



    );

    private final String copyData;

    /**
     * Creates a new PostgreSQL COPY ... FROM STDIN statement.
     */
    public PostgreSQLCopyParsedStatement(int pos, int line, int col, String sql, String copyData) {
        super(pos, line, col, sql, COPY_DELIMITER, true



        );
        this.copyData = copyData;
    }

    @Override
    public Results execute(JdbcTemplate jdbcTemplate



    ) {
        // #2355: Use reflection to ensure this works in cases where the PostgreSQL driver classes were loaded in a
        //        child URLClassLoader instead of the system classloader.
        Object baseConnection;
        Object copyManager;
        Method copyManagerCopyInMethod;
        try {
            Connection connection = jdbcTemplate.getConnection();
            ClassLoader classLoader = connection.getClass().getClassLoader();

            Class<?> baseConnectionClass = classLoader.loadClass("org.postgresql.core.BaseConnection");
            baseConnection = connection.unwrap(baseConnectionClass);

            Class<?> copyManagerClass = classLoader.loadClass("org.postgresql.copy.CopyManager");
            Constructor<?> copyManagerConstructor = copyManagerClass.getConstructor(baseConnectionClass);
            copyManagerCopyInMethod = copyManagerClass.getMethod("copyIn", String.class, Reader.class);

            copyManager = copyManagerConstructor.newInstance(baseConnection);
        } catch (Exception e) {
            throw new FlywayException("Unable to find PostgreSQL CopyManager class", e);
        }

        Results results = new Results();
        try {
            try {
                Long updateCount = (Long) copyManagerCopyInMethod.invoke(copyManager, getSql(), new StringReader(copyData));
                results.addResult(new Result(updateCount, null, null, getSql()));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new SQLException("Unable to execute COPY operation", e);
            }
        } catch (SQLException e) {
            jdbcTemplate.extractErrors(results, e);
        }
        return results;
    }
}