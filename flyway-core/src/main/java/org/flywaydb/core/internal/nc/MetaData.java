/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
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
package org.flywaydb.core.internal.nc;

/**
 * This contains connection relevant metadata gathered from the database.
 *
 * @param productName    The database type name given by the database. For example, MySQL.
 * @param productVersion The database type version given by the database. This may include variation information. For
 *                       example, MariaDB 10.11.
 */
public record MetaData(String databaseType,
                       String productName,
                       DatabaseVersion version,
                       String productVersion,
                       String databaseName,
                       ConnectionType connectionType) {}
