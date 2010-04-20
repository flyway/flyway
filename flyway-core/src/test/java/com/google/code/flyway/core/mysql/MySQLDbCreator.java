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

package com.google.code.flyway.core.mysql;

import com.google.code.flyway.core.util.MigrationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Utility for creating and dropping MySQL database schemas and users.
 */
public class MySQLDbCreator {
    @Autowired
    @Qualifier("root-simpleJdbcTemplate")
    private SimpleJdbcTemplate rootSimpleJdbcTemplate;

    @PostConstruct
    public void createDatabase() {
        MigrationUtils.executeSqlScript(rootSimpleJdbcTemplate, new ClassPathResource("migration/mysql/createDatabase.sql"));
    }

    @PreDestroy
    public void dropDatabase() {
        MigrationUtils.executeSqlScript(rootSimpleJdbcTemplate, new ClassPathResource("migration/mysql/dropDatabase.sql"));
    }
}
