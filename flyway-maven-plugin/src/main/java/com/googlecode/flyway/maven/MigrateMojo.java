/**
 * Copyright (C) 2010-2012 the original author or authors.
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
import com.googlecode.flyway.core.migration.Migration;
import com.googlecode.flyway.core.migration.MigrationProvider;
import com.googlecode.flyway.core.migration.SchemaVersion;
import com.googlecode.flyway.core.validation.ValidationErrorMode;
import com.googlecode.flyway.core.validation.ValidationMode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.project.MavenProject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Maven goal that triggers the migration of the configured database to the latest version.
 *
 * @goal migrate
 */
@SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
public class MigrateMojo extends AbstractMigrationLoadingMojo {
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(MigrateMojo.class);

    /**
     * Property name prefix for placeholders that are configured through properties.
     */
    private static final String PLACEHOLDERS_PROPERTY_PREFIX = "flyway.placeholders.";

    /**
     * The target version up to which Flyway should run migrations. Migrations with a higher version number will not be
     * applied. (default: the latest version) Also configurable with Maven or System Property: ${flyway.target}
     *
     * @parameter expression="${flyway.target}"
     */
    private String target;

    /**
     * Ignores failed future migrations when reading the metadata table. These are migrations that we performed by a
     * newer deployment of the application that are not yet available in this version. For example: we have migrations
     * available on the classpath up to version 3.0. The metadata table indicates that a migration to version 4.0
     * (unknown to us) has already been attempted and failed. Instead of bombing out (fail fast) with an exception, a
     * warning is logged and Flyway terminates normally. This is useful for situations where a database rollback is not
     * an option. An older version of the application can then be redeployed, even though a newer one failed due to a
     * bad migration. (default: false) Also configurable with Maven or System Property:
     * ${flyway.ignoreFailedFutureMigration}
     *
     * @parameter expression="${flyway.ignoreFailedFutureMigration}"
     */
    private boolean ignoreFailedFutureMigration;

    /**
     * A map of <placeholder, replacementValue> to apply to sql migration scripts.
     *
     * @parameter
     */
    private Map<String, String> placeholders;

    /**
     * The prefix of every placeholder. (default: ${ )<br> Also configurable with Maven or System Property:
     * ${flyway.placeholderPrefix}
     *
     * @parameter expression="${flyway.placeholderPrefix}"
     */
    private String placeholderPrefix;

    /**
     * The suffix of every placeholder. (default: } )<br> Also configurable with Maven or System Property:
     * ${flyway.placeholderSuffix}
     *
     * @parameter expression="${flyway.placeholderSuffix}"
     */
    private String placeholderSuffix;

    /**
     * Flag to disable the check that a non-empty schema has been properly initialized with init. This check ensures
     * Flyway does not migrate or clean the wrong database in case of a configuration mistake. Be careful when disabling
     * this! (default: false)<br/>Also configurable with Maven or System Property:
     * ${flyway.disableInitCheck}
     *
     * @parameter expression="${flyway.disableInitCheck}"
     */
    private boolean disableInitCheck;

    /**
     * The type of validation to be performed before migrating.<br/> <br/> Possible values are:<br/> <br/> <b>NONE</b>
     * (default)<br/> No validation is performed.<br/> <br/> <b>ALL</b><br/> For each sql migration a CRC32 checksum is
     * calculated when the sql script is executed. The validate mechanism checks if the sql migrations in the classpath
     * still has the same checksum as the sql migration already executed in the database.<br/> <br/> Also configurable
     * with Maven or System Property: ${flyway.validationMode}
     *
     * @parameter expression="${flyway.validationMode}"
     */
    private String validationMode;

    /**
     * Reference to the current project that includes the Flyway Maven plugin.
     *
     * @parameter expression="${project}" required="true"
     */
    private MavenProject mavenProject;

    @Override
    protected void doExecute(Flyway flyway) throws Exception {
        super.doExecute(flyway);

        if (target != null) {
            flyway.setTarget(new SchemaVersion(target));
        }
        flyway.setIgnoreFailedFutureMigration(ignoreFailedFutureMigration);

        Map<String, String> mergedPlaceholders = new HashMap<String, String>();
        addPlaceholdersFromProperties(mergedPlaceholders, mavenProject.getProperties());
        addPlaceholdersFromProperties(mergedPlaceholders, System.getProperties());
        if (placeholders != null) {
            mergedPlaceholders.putAll(placeholders);
        }
        flyway.setPlaceholders(mergedPlaceholders);

        if (placeholderPrefix != null) {
            flyway.setPlaceholderPrefix(placeholderPrefix);
        }
        if (placeholderSuffix != null) {
            flyway.setPlaceholderSuffix(placeholderSuffix);
        }
        flyway.setDisableInitCheck(disableInitCheck);
        if (validationMode != null) {
            flyway.setValidationMode(ValidationMode.valueOf(validationMode.toUpperCase()));
        }

        MigrationProvider migrationProvider =
                new MigrationProvider(flyway.getBasePackage(), flyway.getBaseDir(), flyway.getEncoding(),
                        flyway.getSqlMigrationPrefix(), flyway.getSqlMigrationSuffix(),
                        flyway.getPlaceholders(), flyway.getPlaceholderPrefix(), flyway.getPlaceholderSuffix());
        List<Migration> availableMigrations = migrationProvider.findAvailableMigrations();

        if (availableMigrations.isEmpty()) {
            LOG.warn("Possible solution: run mvn compile first so Flyway can find the migrations");
            return;
        }

        flyway.migrate();
    }

    /**
     * Adds the additional placeholders contained in these properties to the existing list.
     *
     * @param placeholders The existing list of placeholders.
     * @param properties   The properties containing additional placeholders.
     */
    private static void addPlaceholdersFromProperties(Map<String, String> placeholders, Properties properties) {
        for (Object property : properties.keySet()) {
            String propertyName = (String) property;
            if (propertyName.startsWith(PLACEHOLDERS_PROPERTY_PREFIX)
                    && propertyName.length() > PLACEHOLDERS_PROPERTY_PREFIX.length()) {
                String placeholderName = propertyName.substring(PLACEHOLDERS_PROPERTY_PREFIX.length());
                String placeholderValue = properties.getProperty(propertyName);
                placeholders.put(placeholderName, placeholderValue);
            }
        }
    }
}
