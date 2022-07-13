/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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
package org.flywaydb.community.database.db2z;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
// import org.flywaydb.community.jdbc.JdbcTemplate;
// import org.flywaydb.community.jdbc.DB2ZJdbcTemplate;
import org.flywaydb.core.internal.jdbc.Result;
import org.flywaydb.core.internal.jdbc.Results;
import org.flywaydb.core.internal.sqlscript.Delimiter;
import org.flywaydb.core.internal.sqlscript.ParsedSqlStatement;
// import org.flywaydb.community.sqlscript.DB2ZParsedSqlStatement;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutor;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;


/**
 * A DB2Z CALL PROCEDURE statement.
 */
public class DB2ZCallProcedureParsedStatement extends ParsedSqlStatement {

    private final String procedureName;
    private final Object[] parms;

    private static final Pattern DB2Z_DSNUTILU_PROCNAME = Pattern.compile(
            "\"?SYSPROC\"?\\.\"?DSNUTILU\"?", Pattern.CASE_INSENSITIVE);
    /**
     * Creates a new DB2Z CALL PROCEDURE statement.
     */
    public DB2ZCallProcedureParsedStatement(int pos, int line, int col, String sql, Delimiter delimiter,
                              boolean canExecuteInTransaction, String procedureName, Object[] parms) {
        super(pos, line, col, sql, delimiter, canExecuteInTransaction);
        this.procedureName = procedureName;
		this.parms = parms;
    }

    @Override
    public Results execute(JdbcTemplate jdbcTemplate) {
        Results results;
		String callStmt = "CALL " + procedureName + "(";
		for(int i=0; i < parms.length; i++) {
			callStmt += (i > 0 ? ", ?" : "?");
		}
		callStmt += ")";

        results = jdbcTemplate.executeCallableStatement(callStmt, parms);
        // results = jdbcTemplate.executeStatement("Zomaar");
		
		//For SYSPROC.DSNUTILU invocations, check last result row to detect any error
		if(DB2Z_DSNUTILU_PROCNAME.matcher(procedureName).matches()) {
			List<Result> resultList = results.getResults();
			if(resultList.size() > 0) {
				Result result = resultList.get(0);
				if(result != null) {
					List<List<String>> resultData = result.getData(); 
					if(resultData != null && resultData.size() > 0) {
						List<String> lastResultRow = resultData.get(resultData.size()-1);
						if(lastResultRow != null && lastResultRow.size() > 0 ) {
							String lastMessage = lastResultRow.get(lastResultRow.size()-1);
							if(lastMessage != null && (
								lastMessage.contains("DSNUGBAC - UTILITY EXECUTION TERMINATED, HIGHEST RETURN CODE=") ||
								lastMessage.contains("DSNUGBAC - UTILITY BATCH MEMORY EXECUTION ABENDED"))) {
								String message = "DSNUTILU TERMINATED WITH OUTPUT:\n";
								for(List<String> row : resultData) {
									message += row.get(row.size()-1) + "\n";
								}
								results.setException(new SQLException(message));
							} else {
								//In case of successful completion, only log last message
								resultData.clear();
								resultData.add(lastResultRow);
							}
						}
					}
				}				
			}
		}
		
        return results;
    }
}
