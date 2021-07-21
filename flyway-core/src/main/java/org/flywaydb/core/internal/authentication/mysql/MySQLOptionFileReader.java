/*
 * Copyright (C) Red Gate Software Ltd 2010-2021
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
package org.flywaydb.core.internal.authentication.mysql;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.authentication.ExternalAuthFileReader;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MySQLOptionFileReader implements ExternalAuthFileReader {
    private static final Log LOG = LogFactory.getLog(MySQLOptionFileReader.class);

    public final List<String> optionFiles;
    private final List<String> encryptedOptionFiles;

    public MySQLOptionFileReader() {
        optionFiles = new ArrayList<>();
        encryptedOptionFiles = new ArrayList<>();
    }
    
    @Override
    public List<String> getAllContents() {
        List<String> fileContents = new ArrayList<>();




















        return fileContents;
    }









































    public void populateOptionFiles() {
        boolean isWindows = System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");
        if (isWindows) {
            String winDir = System.getenv("WINDIR");
            addIfOptionFileExists(winDir + "\\my.ini", false);
            addIfOptionFileExists(winDir + "\\my.cnf", false);
            addIfOptionFileExists("C:\\my.ini", false);
            addIfOptionFileExists("C:\\my.cnf", false);

            String mysqlHome = System.getenv("MYSQL_HOME");
            if (mysqlHome != null) {
                addIfOptionFileExists(mysqlHome + "\\my.ini", false);
                addIfOptionFileExists(mysqlHome + "\\my.cnf", false);
            }

            String appdata = System.getenv("APPDATA");
            addIfOptionFileExists(appdata + "\\MySQL\\.mylogin.cnf", true);
        } else {
            addIfOptionFileExists("/etc/my.cnf", false);
            addIfOptionFileExists("/etc/mysql/my.cnf", false);

            String mysqlHome = System.getenv("MYSQL_HOME");
            if (mysqlHome != null) {
                addIfOptionFileExists(mysqlHome + "/my.cnf", false);
            }

            String userHome = System.getProperty("user.home");
            addIfOptionFileExists(userHome + "/.my.cnf", true);
            addIfOptionFileExists(userHome + "/.mylogin.cnf", true);
        }
    }

    private void addIfOptionFileExists(String optionFilePath, boolean encrypted) {
        File optionFile = new File(optionFilePath);
        if (!optionFile.exists()) {
            return;
        }
        optionFiles.add(optionFile.getAbsolutePath());
        if (encrypted) {
            encryptedOptionFiles.add(optionFile.getAbsolutePath());
        }
    }
}