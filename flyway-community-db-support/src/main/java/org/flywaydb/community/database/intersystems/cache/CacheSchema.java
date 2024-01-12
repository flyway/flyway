/*
 * Copyright (C) Red Gate Software Ltd 2010-2023
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
package org.flywaydb.community.database.intersystems.cache;

import java.sql.SQLException;
import java.util.Collection;
import java.util.stream.Stream;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

/**
 * @author Oliver Stahl
 */
public class CacheSchema extends Schema<CacheDatabase, CacheTable>
{
   private static final Log LOG = LogFactory.getLog(CacheSchema.class);

   /**
    * Creates a new Cache schema.
    *
    * @param jdbcTemplate The Jdbc Template for communicating with the DB.
    * @param database     The database-specific support.
    * @param name         The name of the schema.
    */
   CacheSchema(JdbcTemplate jdbcTemplate, CacheDatabase database, String name) {
      super(jdbcTemplate, database, name);
   }

   @Override
   protected boolean doExists() {
      return true; // Schemas don't need to be created beforehand in Cach�; hence a schema always "exists".
   }

   @Override
   protected boolean doEmpty() throws SQLException {
      return findAllTableNames().isEmpty();
   }

   @Override
   protected void doCreate() {
      LOG.debug("Schema '" + name + "' not created - Cache does not support creating schemas");
   }

   @Override
   protected void doDrop() {
      LOG.debug("Schema '" + name + "' not dropped - Cache does not support dropping schemas");
   }

   @Override
   protected void doClean() {
      Stream.of(allTables()).forEach(Table::drop);
   }

   @Override
   protected CacheTable[] doAllTables() throws SQLException {
      return findAllTableNames().stream().map(this::getTable).toArray(CacheTable[]::new);
   }

   @Override
   public CacheTable getTable(String tableName) {
      return new CacheTable(jdbcTemplate, database, this, tableName);
   }

   private Collection<String> findAllTableNames() throws SQLException {
      return jdbcTemplate.queryForStringList(
            "SELECT SqlTableName from %dictionary.compiledclass where SqlSchemaName = ?",
            name
      );
   }
}