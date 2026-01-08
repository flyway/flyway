/*-
 * ========================LICENSE_START=================================
 * flyway-nc-core
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
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
package org.flywaydb.nc.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import org.flywaydb.core.api.FlywayException;

public class TemporaryFileUtils {

    public static String createTempFile(final String sqlContent) {
        return createTempFile(sqlContent, ".sql");
    }
    
    public static String createTempFile(final String sqlContent, final String suffix) {
        try {
            final File tempFile = File.createTempFile("temp_sql_", suffix);
            tempFile.deleteOnExit();

            try (final BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                writer.write(sqlContent);
            }

            return tempFile.getAbsolutePath();
        } catch (final Exception e) {
            throw new FlywayException("Failed to write SQL file due to: " + e.getMessage(), e);
        }
    }
}
