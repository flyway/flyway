/*
 * Copyright (C) Red Gate Software Ltd 2010-2024
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
package org.flywaydb.community.database.intersystems.iris;

import java.sql.SQLException;

import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

/**
 * @author Oliver Stahl
 */
public class IrisTable extends Table<IrisDatabase, IrisSchema>
{
   /**
    * Lock duration in seconds
    */
   private static final Integer DURATION = 2;

   /**
    * Creates a new Cache table.
    *
    * @param jdbcTemplate The Jdbc Template for communicating with the DB.
    * @param database     The database-specific support.
    * @param schema       The schema this table lives in.
    * @param name         The name of the table.
    */
   IrisTable(JdbcTemplate jdbcTemplate, IrisDatabase database, IrisSchema schema, String name)
   {
      super(jdbcTemplate, database, schema, name);
   }

   @Override
   protected void doDrop() throws SQLException
   {
      jdbcTemplate.executeStatement("SET OPTION COMPILEMODE = NOCHECK");
      jdbcTemplate.executeStatement("DROP TABLE " + database.quote(schema.getName(), name) + " CASCADE");
   }

   @Override
   protected boolean doExists() throws SQLException
   {
      return jdbcTemplate.queryForBoolean("SELECT DECODE((select 1 from %dictionary.compiledclass where SqlSchemaName = ? and SqlTableName = ?), 1, 1, 0)", schema.getName(), name);
   }

   @Override
   protected void doLock() throws SQLException
   {
      jdbcTemplate.executeStatement("LOCK TABLE " + database.quote(schema.getName(), name) + "IN EXCLUSIVE MODE WAIT " + DURATION);
   }

}