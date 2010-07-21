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

package com.googlecode.flyway.maven;

import com.googlecode.flyway.core.Flyway;

import java.util.Map;

/**
 * Maven goal that triggers the migration of the configured database to the latest version.
 *
 * @goal migrate
 * @requiresDependencyResolution compile
 * @configurator include-project-dependencies
 */
@SuppressWarnings({"UnusedDeclaration"})
public class MigrateMojo extends AbstractFlywayMojo {
    /**
     * The base package where the Java migrations are located. (default: db.migration) <br>
     * default property: ${flyway.basePackage}
     *
     * @parameter default-value="${flyway.basePackage}"
     */
    private String basePackage;

    /**
     * The base directory on the classpath where the Sql migrations are located. (default: sql/location)<br>
     * default property: ${flyway.baseDir}
     *
     * @parameter default-value="${flyway.baseDir}"
     */
    private String baseDir;

    /**
     * The encoding of Sql migrations. (default: UTF-8)<br>
     * default property: ${flyway.encoding}
     *
     * @parameter default-value="${flyway.encoding}"
     */
    private String encoding;

    /**
     * The name of the schema metadata table that will be used by flyway. (default: schema_version)<br>
     * default property: ${flyway.schemaMetaDataTable}
     *
     * @parameter default-value="${flyway.schemaMetaDataTable}"
     */
    private String schemaMetaDataTable;

    /**
     * A map of <placeholder, replacementValue> to apply to sql migration scripts.
     *
     * @parameter
     */
    private Map<String, String> placeholders;

    /**
     * The prefix of every placeholder. (default: ${ )<br>
     * default property: ${flyway.placeholderPrefix}
     *
     * @parameter default-value="${flyway.placeholderPrefix}"
     */
    private String placeholderPrefix = "${";

    /**
     * The suffix of every placeholder. (default: } )<br>
     * default property: ${flyway.placeholderSuffix}
     *
     * @parameter default-value="${flyway.placeholderSuffix}"
     */
    private String placeholderSuffix = "}";

    @Override
    protected void doExecute() throws Exception {
        Flyway flyway = new Flyway();
        flyway.setDataSource(getDataSource());
        if (basePackage != null) {
            flyway.setBasePackage(basePackage);
        }
        if (baseDir != null) {
            flyway.setBaseDir(baseDir);
        }
        if (encoding != null) {
            flyway.setEncoding(encoding);
        }
        if (schemaMetaDataTable != null) {
            flyway.setSchemaMetaDataTable(schemaMetaDataTable);
        }
        if (placeholders != null) {
            flyway.setPlaceholders(placeholders);
        }
        if (placeholderPrefix != null) {
            flyway.setPlaceholderPrefix(placeholderPrefix);
        }
        if (placeholderSuffix != null) {
            flyway.setPlaceholderSuffix(placeholderSuffix);
        }

        flyway.migrate();
    }
}
