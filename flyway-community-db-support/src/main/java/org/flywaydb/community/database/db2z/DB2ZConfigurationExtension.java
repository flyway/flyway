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

import java.util.Map;
import lombok.Data;
import org.flywaydb.core.extensibility.ConfigurationExtension;

@Data
public class DB2ZConfigurationExtension implements ConfigurationExtension {
  private static final String DATABASE_NAME = "flyway.db2z.databaseName";
  private static final String SQL_ID = "flyway.db2z.sqlId";

  /**
   * The database name for DB2 on z/OS (required for DB2 on z/OS)
   */
  private String databaseName = "";
  /**
   * The SQLID for DB2 on z/OS (does not necessarily match with schema)
   */
  private String sqlId = "";


  @Override
  public void extractParametersFromConfiguration(Map<String, String> configuration) {
    databaseName = configuration.getOrDefault(DATABASE_NAME, databaseName);
    sqlId = configuration.getOrDefault(SQL_ID, sqlId);
    configuration.remove(DATABASE_NAME);
    configuration.remove(SQL_ID);
  }

  @Override
  public String getConfigurationParameterFromEnvironmentVariable(String environmentVariable) {
    if ("FLYWAY_DB2Z_DATABASE_NAME".equals(environmentVariable)) {
      return DATABASE_NAME;
    }
    if ("FLYWAY_DB2Z_SQL_ID".equals(environmentVariable)) {
      return SQL_ID;
    }
    return null;
  }
}
