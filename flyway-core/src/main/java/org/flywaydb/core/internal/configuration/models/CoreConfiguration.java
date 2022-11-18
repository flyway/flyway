/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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
package org.flywaydb.core.internal.configuration.models;

import java.util.List;
import java.util.Map;

public class CoreConfiguration {

    private boolean cleanDisabled;
    private boolean cleanOnValidationError;
    private List<String> locations;
    private String defaultSchema;
    private List<String> schemas;
    private String table;
    private String tablespace;
    private String target;
    private boolean failOnMissingTarget;
    private List<String> cherryPick;
    private boolean placeholderReplacement;
    private List<String> ignoreMigrationPatterns;
    private boolean validateMigrationNaming;
    private boolean validateOnMigrate;
    private String baselineVersion;
    private String baselineDescription;
    private boolean baselineOnMigrate;
    private boolean outOfOrder;
    private boolean skipExecutingMigrations;
    private List<String> callbacks;
    private boolean skipDefaultCallbacks;
    private List<String> migrationResolvers;
    private boolean skipDefaultResolvers;
    private boolean mixed;
    private boolean group;
    private String installedBy;
    private boolean createSchemas;
    private List<String> errorOverrides;
    private String dryRunOutput;
    private boolean stream;
    private boolean batch;
    private boolean outputQueryResults;
    private int lockRetryCount;
    private String kerberosConfigFile;
    private String oracleKerberosCacheFile;
    private String oracleWalletLocation;
    private boolean failOnMissingLocations;
    private List<String> loggers;
    private Map<String, String> jdbcProperties;
    private Map<String, String> placeholders;
    private String driver;
    private List<EnvironmentResolver> environmentResolvers;
}