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
package org.flywaydb.core.internal.database.oracle;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.errorhandler.Error;
import org.flywaydb.core.api.errorhandler.ErrorHandler;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.sqlscript.SqlScript;
import org.flywaydb.core.internal.sqlscript.SqlStatementBuilder;
import org.flywaydb.core.internal.sqlscript.SqlStatementBuilderFactory;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.sqlscript.SqlStatement;
import org.flywaydb.core.internal.util.AsciiTable;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.util.jdbc.JdbcUtils;
import org.flywaydb.core.internal.util.jdbc.Result;
import org.flywaydb.core.internal.util.placeholder.PlaceholderReplacer;
import org.flywaydb.core.internal.util.scanner.LoadableResource;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Oracle-specific SQL script.
 */
class OracleSqlScript extends SqlScript<OracleContextImpl> {
    private static final Log LOG = LogFactory.getLog(OracleSqlScript.class);








    OracleSqlScript(final Configuration configuration,
                    LoadableResource sqlScriptResource, boolean mixed




            , final PlaceholderReplacer placeholderReplacer) {
        super(configuration, new SqlStatementBuilderFactory() {
                    @Override
                    public SqlStatementBuilder createSqlStatementBuilder() {
                        return new OracleSqlStatementBuilder(configuration



                        );
                    }
                }, sqlScriptResource, mixed,



                placeholderReplacer
        );
    }

    @Override
    protected void handleException(SQLException e, SqlStatement sqlStatement, OracleContextImpl context) {









        super.handleException(e, sqlStatement, context);
    }

    @Override
    protected OracleContextImpl createContext() {
        return new OracleContextImpl();
    }

































































}