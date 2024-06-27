/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2024 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.core.internal.database.base;

import java.util.regex.Pattern;

public class DatabaseConstants {

    public static final Pattern DATABASE_HOSTING_AZURE_URL_IDENTIFIER = Pattern.compile(".+\\.azure\\.com");
    public static final Pattern DATABASE_HOSTING_RDS_URL_IDENTIFIER = Pattern.compile(".+\\.rds\\.(\\+\\.+)*amazon\\,com(\\.\\w+)?");
    public static final String DATABASE_HOSTING_GCP_URL_IDENTIFIER = "socketFactory=com.google.cloud";
    public static final String DATABASE_HOSTING_AZURE_SQL_DATABASE = "azure-sql-database";
    public static final String DATABASE_HOSTING_AZURE_SQL_MANAGED_INSTANCE = "azure-sql-managed-instance";
    public static final String DATABASE_HOSTING_AWS_RDS = "aws-rds";
    public static final String DATABASE_HOSTING_AZURE_VM = "azure-vm";
    public static final String DATABASE_HOSTING_AWS_VM = "aws-vm";
    public static final String DATABASE_HOSTING_GCP_VM = "gcp-vm";
    public static final String DATABASE_HOSTING_GOOGLE_BIGQUERY = "gcp-big-query";
    public static final String DATABASE_HOSTING_GOOGLE_SPANNER = "gcp-cloud-spanner";
    public static final String DATABASE_HOSTING_AZURE_SNOWFLAKE = "azure-snowflake";
    public static final String DATABASE_HOSTING_GCP_SNOWFLAKE = "gcp-snowflake";
    public static final String DATABASE_HOSTING_AWS_SNOWFLAKE = "aws-snowflake";
    public static final String DATABASE_HOSTING_MONGODB_ATLAS = "mongodb-atlas";
    public static final String DATABASE_HOSTING_LOCAL = "local";

}
