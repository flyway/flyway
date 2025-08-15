/*-
 * ========================LICENSE_START=================================
 * flyway-database-gaussdb
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
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
package org.flywaydb.database.gaussdb;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
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
 * A GaussDB COPY FROM STDIN statement.
 *
 * @author chen zhida
 * Notes: Original code of this class is based on PostgreSQLCopyParsedStatement.
 *
 */
public class GaussDBCopyParsedStatement extends ParsedSqlStatement {
    /**
     * Delimiter of COPY statements.
     */
    private static final Delimiter COPY_DELIMITER = new Delimiter("\\.", true



    );

    private final String copyData;

    /**
     * Creates a new GaussDB COPY ... FROM STDIN statement.
     */
    public GaussDBCopyParsedStatement(int pos, int line, int col, String sql, String copyData) {
        super(pos, line, col, sql, COPY_DELIMITER, true, false);
        this.copyData = copyData;
    }

    @Override
    public Results execute(JdbcTemplate jdbcTemplate, SqlScriptExecutor sqlScriptExecutor, Configuration config) {
        // #2355: Use reflection to ensure this works in cases where the GaussDB driver classes were loaded in a
        //        child URLClassLoader instead of the system classloader.
        Object baseConnection;
        Object copyManager;
        Method copyManagerCopyInMethod;
        try {
            Connection connection = jdbcTemplate.getConnection();
            ClassLoader classLoader = connection.getClass().getClassLoader();

            Class<?> baseConnectionClass = classLoader.loadClass("com.huawei.gaussdb.jdbc.core.BaseConnection");
            baseConnection = connection.unwrap(baseConnectionClass);

            Class<?> copyManagerClass = classLoader.loadClass("com.huawei.gaussdb.jdbc.copy.CopyManager");
            Constructor<?> copyManagerConstructor = copyManagerClass.getConstructor(baseConnectionClass);
            copyManagerCopyInMethod = copyManagerClass.getMethod("copyIn", String.class, Reader.class);

            copyManager = copyManagerConstructor.newInstance(baseConnection);
        } catch (Exception e) {
            throw new FlywayException("Unable to find GaussDB CopyManager class", e);
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
