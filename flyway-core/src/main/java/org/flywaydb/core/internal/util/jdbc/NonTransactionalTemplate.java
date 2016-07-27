/**
 * Copyright 2010-2016 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.flywaydb.core.internal.util.jdbc;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Spring-like template for executing transactions.
 */
public class NonTransactionalTemplate {

  private static final Log LOG = LogFactory.getLog(NonTransactionalTemplate.class);

  /**
   * The connection for the transaction.
   */
  private final Connection connection;

  /**
   * Creates a new transaction template for this connection.
   *
   * @param connection The connection for the transaction.
   */
  public NonTransactionalTemplate(Connection connection) {
    this.connection = connection;
  }

  /**
   * Executes this callback within a transaction.
   *
   * @param nontransactionCallback The callback to execute.
   * @return The result of the non-transactional code.
   */
  public <T> T execute(NonTransactionalCallback<T> nontransactionCallback) {
    try {
      T result = nontransactionCallback.execute();
      return result;
    } catch (SQLException e) {
      throw new FlywayException("Error in NonTransactionalTemplate", e);
    }
  }
}
