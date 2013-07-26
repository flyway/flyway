/**
 * Copyright 2010-2013 Axel Fontaine and the many contributors.
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
package com.googlecode.flyway.ant;

import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.validation.ValidationMode;

/**
 * Ant task that triggers the migration of the configured database to the latest version.
 */
@SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
public class MigrateTask extends AbstractMigrationLoadingTask {
    /**
     * Ignores failed future migrations when reading the metadata table. These are migrations that we performed by a
     * newer deployment of the application that are not yet available in this version. For example: we have migrations
     * available on the classpath up to version 3.0. The metadata table indicates that a migration to version 4.0
     * (unknown to us) has already been attempted and failed. Instead of bombing out (fail fast) with an exception, a
     * warning is logged and Flyway terminates normally. This is useful for situations where a database rollback is not
     * an option. An older version of the application can then be redeployed, even though a newer one failed due to a
     * bad migration. (default: false)<br/>Also configurable with Ant Property: ${flyway.ignoreFailedFutureMigration}
     */
    private boolean ignoreFailedFutureMigration;

    /**
     * The type of validation to be performed before migrating.<br/> <br/> Possible values are:<br/> <br/> <b>NONE</b>
     * (default)<br/> No validation is performed.<br/> <br/> <b>ALL</b><br/> For each sql migration a CRC32 checksum is
     * calculated when the sql script is executed. The validate mechanism checks if the sql migrations in the classpath
     * still has the same checksum as the sql migration already executed in the database.<br/> <br/>Also configurable with Ant Property: ${flyway.validationMode}
     *
     * @deprecated Use validateOnMigrate instead. Will be removed in Flyway 3.0.
     */
    @Deprecated
    private String validationMode;

    /**
     * Whether to automatically call validate or not when running migrate. (default: {@code false})<br/>
     * Also configurable with Ant Property: ${flyway.validateOnMigrate}
     */
    private boolean validateOnMigrate;

    /**
     * Flag to disable the check that a non-empty schema has been properly initialized with init. This check ensures
     * Flyway does not migrate or clean the wrong database in case of a configuration mistake. Be careful when disabling
     * this! (default: false)<br/>Also configurable with Ant Property: ${flyway.disableInitCheck}
     *
     * @deprecated Use initOnMigrate instead. Will be removed in Flyway 3.0.
     */
    @Deprecated
    private boolean disableInitCheck;

    /**
     * <p>
     * Whether to automatically call init when migrate is executed against a non-empty schema with no metadata table.
     * This schema will then be initialized with the {@code initialVersion} before executing the migrations.
     * Only migrations above {@code initialVersion} will then be applied.
     * </p>
     * <p>
     * This is useful for initial Flyway production deployments on projects with an existing DB.
     * </p>
     * <p>
     * Be careful when enabling this as it removes the safety net that ensures
     * Flyway does not migrate the wrong database in case of a configuration mistake! (default: {@code false})
     * </p>
     * Also configurable with Ant Property: ${flyway.initOnMigrate}
     */
    private boolean initOnMigrate;

    /**
     * @param ignoreFailedFutureMigration Ignores failed future migrations when reading the metadata table. These are migrations that we performed by a
     *                                    newer deployment of the application that are not yet available in this version. For example: we have migrations
     *                                    available on the classpath up to version 3.0. The metadata table indicates that a migration to version 4.0
     *                                    (unknown to us) has already been attempted and failed. Instead of bombing out (fail fast) with an exception, a
     *                                    warning is logged and Flyway terminates normally. This is useful for situations where a database rollback is not
     *                                    an option. An older version of the application can then be redeployed, even though a newer one failed due to a
     *                                    bad migration. (default: false)<br/>Also configurable with Ant Property: ${flyway.ignoreFailedFutureMigration}
     */
    public void setIgnoreFailedFutureMigration(boolean ignoreFailedFutureMigration) {
        this.ignoreFailedFutureMigration = ignoreFailedFutureMigration;
    }

    /**
     * @param validationMode The type of validation to be performed before migrating.<br/> <br/> Possible values are:<br/> <br/> <b>NONE</b>
     *                       (default)<br/> No validation is performed.<br/> <br/> <b>ALL</b><br/> For each sql migration a CRC32 checksum is
     *                       calculated when the sql script is executed. The validate mechanism checks if the sql migrations in the classpath
     *                       still has the same checksum as the sql migration already executed in the database.<br/> <br/>Also configurable with Ant Property: ${flyway.validationMode}
     * @deprecated Use validateOnMigrate instead. Will be removed in Flyway 3.0.
     */
    @Deprecated
    public void setValidationMode(String validationMode) {
        this.validationMode = validationMode;
    }

    /**
     * @param validateOnMigrate Whether to automatically call validate or not when running migrate. (default: {@code false})<br/>
     *                          Also configurable with Ant Property: ${flyway.validateOnMigrate}
     */
    public void setValidateOnMigrate(boolean validateOnMigrate) {
        this.validateOnMigrate = validateOnMigrate;
    }

    /**
     * @param disableInitCheck Flag to disable the check that a non-empty schema has been properly initialized with init. This check ensures
     *                         Flyway does not migrate or clean the wrong database in case of a configuration mistake. Be careful when disabling
     *                         this! (default: false)<br/>Also configurable with Ant Property: ${flyway.disableInitCheck}
     * @deprecated Use initOnMigrate instead. Will be removed in Flyway 3.0.
     */
    @Deprecated
    public void setDisableInitCheck(boolean disableInitCheck) {
        this.disableInitCheck = disableInitCheck;
    }

    /**
     * <p>
     * Whether to automatically call init when migrate is executed against a non-empty schema with no metadata table.
     * This schema will then be initialized with the {@code initialVersion} before executing the migrations.
     * Only migrations above {@code initialVersion} will then be applied.
     * </p>
     * <p>
     * This is useful for initial Flyway production deployments on projects with an existing DB.
     * </p>
     * <p>
     * Be careful when enabling this as it removes the safety net that ensures
     * Flyway does not migrate the wrong database in case of a configuration mistake!
     * </p>
     * Also configurable with Ant Property: ${flyway.initOnMigrate}
     *
     * @param initOnMigrate {@code true} if init should be called on migrate for non-empty schemas, {@code false} if not. (default: {@code false})
     */
    public void setInitOnMigrate(boolean initOnMigrate) {
        this.initOnMigrate = initOnMigrate;
    }

    @Override
    protected void doExecuteWithMigrationConfig(Flyway flyway) throws Exception {
        String validationModeValue = useValueIfPropertyNotSet(validationMode, "validationMode");
        if (validationModeValue != null) {
            flyway.setValidationMode(ValidationMode.valueOf(validationModeValue.toUpperCase()));
        }
        flyway.setValidateOnMigrate(useValueIfPropertyNotSet(validateOnMigrate, "validateOnMigrate"));
        flyway.setDisableInitCheck(useValueIfPropertyNotSet(disableInitCheck, "disableInitCheck"));
        flyway.setInitOnMigrate(useValueIfPropertyNotSet(initOnMigrate, "initOnMigrate"));
        flyway.setIgnoreFailedFutureMigration(useValueIfPropertyNotSet(ignoreFailedFutureMigration, "ignoreFailedFutureMigration"));

        if (flyway.info().all().length == 0) {
            log.warn("Possible solution: run the Ant javac and copy tasks first so Flyway can find the migrations");
            return;
        }

        flyway.migrate();
    }
}
