/**
 * Copyright (C) 2009-2010 the original author or authors.
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

package com.googlecode.flyway.core.migration.java;

import com.googlecode.flyway.core.migration.BaseMigration;
import com.googlecode.flyway.core.dbsupport.DbSupport;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.ClassUtils;

/**
 * Base class for java migration classes whose name conforms to the Flyway
 * standard. Example: V1_2__Change_values
 */
public abstract class BaseJavaMigration extends BaseMigration {
    /**
     * Initializes this Migration with this standard Flyway name.
     */
    protected BaseJavaMigration() {
        String nameWithoutV = ClassUtils.getShortName(getClass()).substring(1);
        initVersion(nameWithoutV);
        scriptName = "Java Class: " + ClassUtils.getShortName(getClass());
    }

    /**
     * Performs the migration.
     *
     * @param transactionTemplate The transaction template to use.
     * @param jdbcTemplate        To execute the migration statements.
     * @param dbSupport           The support for database-specific extensions.
     * @throws org.springframework.dao.DataAccessException
     *          Thrown when the migration failed.
     */
    @Override
    protected void doMigrate(TransactionTemplate transactionTemplate, final JdbcTemplate jdbcTemplate, final DbSupport dbSupport) throws DataAccessException {
        transactionTemplate.execute(new TransactionCallback() {
            @Override
            public Void doInTransaction(TransactionStatus status) {
                doMigrateInTransaction(jdbcTemplate);
                return null;
            }
        });

    }

    /**
     * Performs the migration inside a transaction.
     *
     * @param jdbcTemplate To execute the migration statements.
     * @throws org.springframework.dao.DataAccessException
     *          Thrown when the migration failed.
     */
    protected abstract void doMigrateInTransaction(JdbcTemplate jdbcTemplate) throws DataAccessException;
}
