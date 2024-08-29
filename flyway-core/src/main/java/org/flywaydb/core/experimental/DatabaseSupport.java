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
package org.flywaydb.core.experimental;

/**
 * This contains both a boolean for if the database is supported and the priority of which a database should be assessed against.
 * Multiple database types may support the same URL and the priority will be used to check if it is correct for the connected database.
 * @param isSupported Is the database type supported?
 * @param priority What is the Flyway specific priority of this database type over others which support this URL.
 */
public record DatabaseSupport(boolean isSupported, int priority) {}
