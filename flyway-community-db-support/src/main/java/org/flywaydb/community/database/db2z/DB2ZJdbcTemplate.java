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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.JdbcUtils;
import org.flywaydb.core.internal.jdbc.Results;

public class DB2ZJdbcTemplate extends JdbcTemplate {

  public DB2ZJdbcTemplate(Connection connection) {
    super(connection);
  }

  /**
   * Executes this callable sql statement using a PreparedStatement.
   *
   * @param sql    The statement to execute.
   * @param params The statement parameters.
   * @return the results of the execution.
   */
  public Results executeCallableStatement(String sql, Object... params) {
    Results results = new Results();
    PreparedStatement statement = null;
    try {
      statement = prepareStatement(sql, params);
      boolean hasResults = statement.execute();
      extractResults(results, statement, sql, hasResults);
      extractWarnings(results, statement);
    } catch (final SQLException e) {
      extractErrors(results, e);
    } finally {
      JdbcUtils.closeStatement(statement);
    }
    return results;
  }

}
