/**
 * Copyright 2010-2016 Boxfuse GmbH
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
package org.flywaydb.gradle

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.callback.FlywayCallback
import org.flywaydb.core.internal.util.jdbc.DriverDataSource
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class FlywayPluginSmallTest {

    private String defaultUrl = "jdbc:hsqldb:file:/db/flyway_test;shutdown=true"
    protected Project project;
    /**
     * it gets flyway instance created by project task based on current configuration
     * @return
     */
    private Flyway getFlyway() {
        // task is not relevant for this test, all use the same abstract implementation
        return project.tasks.flywayMigrate.createFlyway()
    }

    @Before
    public void setup() {
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'org.flywaydb.flyway'
    }

    @Test
    public void checkIfTaskArePresent() {
        assert project.tasks.findByName('flywayClean')
        assert project.tasks.findByName('flywayInfo')
        assert project.tasks.findByName('flywayBaseline')
        assert project.tasks.findByName('flywayMigrate')
        assert project.tasks.findByName('flywayRepair')
        assert project.tasks.findByName('flywayValidate')
    }

    @Test
    public void validateBasicExtensionProperties() {
        project.flyway {
            url = defaultUrl
        }

        DriverDataSource dataSource = getFlyway().getDataSource()

        assert dataSource.getUrl() == defaultUrl
    }

    @Test
    public void validateDataSourceWithCustomDriver() {
        String dbUrl = "jdbc:custom:file:/db/flyway_test;shutdown=true"
        project.flyway {
            url = dbUrl
            driver = 'org.hsqldb.jdbcDriver'
        }

        DriverDataSource dataSource = getFlyway().getDataSource()

        assert dataSource.getUrl() == dbUrl
        assert dataSource.getDriver() instanceof org.hsqldb.jdbcDriver
    }

    @Test
    public void validateDataSourceWithCredentials() {
        project.flyway {
            url = defaultUrl
            user = 'user'
            password = 'secret'
        }

        DriverDataSource dataSource = getFlyway().getDataSource()

        assert dataSource.getUrl() == defaultUrl
        assert dataSource.getUser() == 'user'
        assert dataSource.getPassword() == 'secret'
    }

    @Test
    public void validateExtensionTextProperties() {
        project.flyway {
            url = defaultUrl
            table = 'table'
            baselineDescription = 'baselineDescription'
            sqlMigrationPrefix = 'sqlMigrationPrefix'
            sqlMigrationSeparator = 'sqlMigrationSeparator'
            sqlMigrationSuffix = 'sqlMigrationSuffix'
            encoding = 'encoding'
            placeholderPrefix = 'placeholderPrefix'
            placeholderSuffix = 'placeholderSuffix'
        }

        Flyway flyway = getFlyway()
        assert flyway.table == 'table'
        assert flyway.baselineDescription == 'baselineDescription'
        assert flyway.sqlMigrationPrefix == 'sqlMigrationPrefix'
        assert flyway.sqlMigrationSeparator == 'sqlMigrationSeparator'
        assert flyway.sqlMigrationSuffix == 'sqlMigrationSuffix'
        assert flyway.encoding == 'encoding'
        assert flyway.placeholderPrefix == 'placeholderPrefix'
        assert flyway.placeholderSuffix == 'placeholderSuffix'
    }


    @Test
    public void validateExtensionVersionProperties() {
        // as strings
        project.flyway {
            url = defaultUrl
            baselineVersion = '1.3'
            target = '2.3'
        }

        Flyway flyway = getFlyway()

        assert flyway.baselineVersion.toString() == '1.3'
        assert flyway.target.toString() == '2.3'

        // as numbers
        project.flyway {
            url = defaultUrl
            baselineVersion = 2
            target = 3
        }

        flyway = getFlyway()

        assert flyway.baselineVersion.toString() == '2'
        assert flyway.target.toString() == '3'
    }

    @Test
    public void validateExtensionBooleanProperties() {
        project.flyway {
            url = defaultUrl
            outOfOrder = true
            validateOnMigrate = true
            cleanOnValidationError = true
            baselineOnMigrate = true
            placeholderReplacement = true
        }

        Flyway flyway = getFlyway()
        assert flyway.outOfOrder
        assert flyway.validateOnMigrate
        assert flyway.cleanOnValidationError
        assert flyway.baselineOnMigrate
        assert flyway.placeholderReplacement

        project.flyway {
            url = defaultUrl
            outOfOrder = false
            validateOnMigrate = false
            cleanOnValidationError = false
            baselineOnMigrate = false
            placeholderReplacement = false
        }

        flyway = getFlyway()
        assert !flyway.outOfOrder
        assert !flyway.validateOnMigrate
        assert !flyway.cleanOnValidationError
        assert !flyway.baselineOnMigrate
        assert !flyway.placeholderReplacement
    }

    @Test
    public void validateExtensionListProperties() {
        project.flyway {
            url = defaultUrl
            schemas = ['schemeA', 'schemeB']
            locations = ['classpath:migrations1', 'migrations2', 'filesystem:/sql-migrations']
            placeholders = ['placeholderA':'A', 'placeholderB':'B']
            callbacks = ['org.flywaydb.gradle.DefaultFlywayCallback']
        }

        Flyway flyway = getFlyway()
        assert flyway.schemas == ['schemeA', 'schemeB']
        assert flyway.locations == ['classpath:migrations1', 'classpath:migrations2', 'filesystem:/sql-migrations']
        assert flyway.placeholders == ['placeholderA':'A', 'placeholderB':'B']
        assert flyway.callbacks[0] instanceof FlywayCallback
    }

}