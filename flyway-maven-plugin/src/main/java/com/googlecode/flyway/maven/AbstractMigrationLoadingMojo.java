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
package com.googlecode.flyway.maven;

import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.api.MigrationVersion;
import com.googlecode.flyway.core.util.Location;
import com.googlecode.flyway.core.util.StringUtils;
import com.googlecode.flyway.core.validation.ValidationErrorMode;

import java.io.File;

/**
 * Base class for mojos that rely on loading migrations from the classpath.
 *
 * @phase pre-integration-test
 */
@SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
abstract class AbstractMigrationLoadingMojo extends AbstractFlywayMojo {
    /**
     * Locations on the classpath to scan recursively for migrations. Locations may contain both sql
     * and java-based migrations. (default: db/migration)
     * <p>Also configurable with Maven or System Property: ${flyway.locations} (Comma-separated list)</p>
     *
     * @parameter
     */
    private String[] locations;

    /**
     * The encoding of Sql migrations. (default: UTF-8)<br> <p>Also configurable with Maven or System Property:
     * ${flyway.encoding}</p>
     *
     * @parameter property="flyway.encoding"
     */
    private String encoding;

    /**
     * The file name prefix for Sql migrations (default: V) <p>Also configurable with Maven or System Property:
     * ${flyway.sqlMigrationPrefix}</p>
     *
     * @parameter property="flyway.sqlMigrationPrefix"
     */
    private String sqlMigrationPrefix;

    /**
     * The file name suffix for Sql migrations (default: .sql) <p>Also configurable with Maven or System Property:
     * ${flyway.sqlMigrationSuffix}</p>
     *
     * @parameter property="flyway.sqlMigrationSuffix"
     */
    private String sqlMigrationSuffix;

    /**
     * The action to take when validation fails.<br/> <br/> Possible values are:<br/> <br/> <b>FAIL</b> (default)<br/>
     * Throw an exception and fail.<br/> <br/> <b>CLEAN (Warning ! Do not use in production !)</b><br/> Cleans the
     * database.<br/> <br/> This is exclusively intended as a convenience for development. Even tough we strongly
     * recommend not to change migration scripts once they have been checked into SCM and run, this provides a way of
     * dealing with this case in a smooth manner. The database will be wiped clean automatically, ensuring that the next
     * migration will bring you back to the state checked into SCM.<br/> <br/> This property has no effect when
     * <i>validationMode</i> is set to <i>NONE</i>.<br/> <br/> <p>Also configurable with Maven or System Property:
     * ${flyway.validationErrorMode}</p>
     *
     * @parameter property="flyway.validationErrorMode"
     * @deprecated Use cleanOnValidationError instead. Will be removed in Flyway 3.0.
     */
    @Deprecated
    private String validationErrorMode;

    /**
     * Whether to automatically call clean or not when a validation error occurs. (default: {@code false})<br/>
     * <p> This is exclusively intended as a convenience for development. Even tough we
     * strongly recommend not to change migration scripts once they have been checked into SCM and run, this provides a
     * way of dealing with this case in a smooth manner. The database will be wiped clean automatically, ensuring that
     * the next migration will bring you back to the state checked into SCM.</p>
     * <p><b>Warning ! Do not enable in production !</b></p><br/>
     * <p>Also configurable with Maven or System Property: ${flyway.cleanOnValidationError}</p>
     *
     * @parameter property="flyway.cleanOnValidationError"
     */
    private boolean cleanOnValidationError;

    /**
     * The target version up to which Flyway should run migrations. Migrations with a higher version number will not be
     * applied. (default: the latest version)
     * <p>Also configurable with Maven or System Property: ${flyway.target}</p>
     *
     * @parameter property="flyway.target"
     */
    private String target;

    /**
     * Allows migrations to be run "out of order" (default: {@code false}).
     * <p>If you already have versions 1 and 3 applied, and now a version 2 is found,
     * it will be applied too instead of being ignored.</p>
     * <p>Also configurable with Maven or System Property: ${flyway.outOfOrder}</p>
     *
     * @parameter property="flyway.outOfOrder"
     */
    private boolean outOfOrder;

    @Override
    protected final void doExecute(Flyway flyway) throws Exception {
        String locationsProperty = getProperty("flyway.locations");
        if (locationsProperty != null) {
            locations = StringUtils.tokenizeToStringArray(locationsProperty, ",");
        }
        if (locations != null) {
            for (int i = 0; i < locations.length; i++) {
                if (locations[i].startsWith(Location.FILESYSTEM_PREFIX)) {
                    String newLocation = locations[i].substring(Location.FILESYSTEM_PREFIX.length());
                    File file = new File(newLocation);
                    if (!file.isAbsolute()) {
                        file = new File(mavenProject.getBasedir(), newLocation);
                    }
                    locations[i] = Location.FILESYSTEM_PREFIX + file.getAbsolutePath();
                }
            }
            flyway.setLocations(locations);
        }
        if (encoding != null) {
            flyway.setEncoding(encoding);
        }
        if (sqlMigrationPrefix != null) {
            flyway.setSqlMigrationPrefix(sqlMigrationPrefix);
        }
        if (sqlMigrationSuffix != null) {
            flyway.setSqlMigrationSuffix(sqlMigrationSuffix);
        }
        if (validationErrorMode != null) {
            flyway.setValidationErrorMode(ValidationErrorMode.valueOf(validationErrorMode.toUpperCase()));
        }
        flyway.setCleanOnValidationError(cleanOnValidationError);
        flyway.setOutOfOrder(outOfOrder);
        if (target != null) {
            flyway.setTarget(new MigrationVersion(target));
        }

        doExecuteWithMigrationConfig(flyway);
    }

    /**
     * Executes Flyway fully configured for loading migrations.
     *
     * @param flyway The instance of Flyway to launch.
     * @throws Exception when the execution failed.
     */
    protected abstract void doExecuteWithMigrationConfig(Flyway flyway) throws Exception;
}
