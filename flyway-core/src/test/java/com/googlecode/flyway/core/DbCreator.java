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

package com.googlecode.flyway.core;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.util.HashMap;

/**
 * Utility for creating and dropping database schemas and users.
 */
public class DbCreator {
    /**
     * The datasource with enough privileges to create/drop database schemas and users.
     */
    private DataSource rootDataSource;

    /**
     * The base directory where to find the database create/drop scripts.
     */
    private String baseDir;

    /**
     * @param rootDataSource The datasource with enough privileges to create/drop database schemas and users.
     */
    public void setRootDataSource(DataSource rootDataSource) {
        this.rootDataSource = rootDataSource;
    }

    /**
     * @param baseDir The base directory where to find the database create/drop scripts.
     */
    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    /**
     * Create the database and users.
     */
    @PostConstruct
    public void createDatabase() {
        SqlScript sqlScript =
                new SqlScript(new ClassPathResource(baseDir + "/createDatabase.sql"), new HashMap<String, String>());
        sqlScript.execute(new TransactionTemplate(new DataSourceTransactionManager(rootDataSource)), new JdbcTemplate(rootDataSource));
    }

    /**
     * Drop the database and users.
     */
    @PreDestroy
    public void dropDatabase() {
        SqlScript sqlScript =
                new SqlScript(new ClassPathResource(baseDir + "/dropDatabase.sql"), new HashMap<String, String>());
        sqlScript.execute(new TransactionTemplate(new DataSourceTransactionManager(rootDataSource)), new JdbcTemplate(rootDataSource));
    }
}
