package org.flywaydb.core.internal.dbsupport.teradata;

import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;

public class TeradataSqlStatementBuilder extends SqlStatementBuilder {

  /**
   * Holds the beginning of the statement.
   */
  private String statementStart = "";

  @Override
  protected void applyStateChanges(String line) {
    super.applyStateChanges(line);

    if (!executeInTransaction) {
      return;
    }

    if (StringUtils.countOccurrencesOf(statementStart, " ") < 8) {
      statementStart += line;
      statementStart += " ";
      statementStart = statementStart.replaceAll("\\s+", " ");
    }

    // If a DDL transaction is detected we do not execute the migration in a transaction.
    if (statementStart.matches("(?i)(ALTER|BEGIN|CREATE|DROP|GRANT|RENAME|REPLACE|REVOKE|RESTART) .*")) {
      executeInTransaction = false;
    }
  }
}
