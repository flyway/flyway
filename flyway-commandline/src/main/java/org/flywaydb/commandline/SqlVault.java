/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.commandline;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.dbsupport.SqlScriptVault;
import org.flywaydb.core.internal.util.logging.console.ConsoleLog.Level;
import org.flywaydb.core.internal.util.scanner.LoadableResource;
import org.flywaydb.core.internal.util.scanner.filesystem.FileSystemResource;
import org.flywaydb.core.internal.util.logging.console.ConsoleLogCreator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Main class for the SQL vault tool.
 */
public class SqlVault extends Main {

    /**
     * Initializes the logging.
     *
     * @param level The minimum level to log at.
     */
    static void initLogging(Level level) {
        LogFactory.setFallbackLogCreator(new ConsoleLogCreator(level));
        LOG = LogFactory.getLog(SqlVault.class);
    }

    /**
     * Main method.
     *
     * @param args The command-line arguments.
     */
    public static void main(String[] args) {
        Level logLevel = getLogLevel(args);
        initLogging(logLevel);
        try {
            printVersion();
            if (isPrintVersionAndExit(args)) {
                System.exit(0);
            }

            String operation = determineOperation(args);
            if (operation.isEmpty() || operation.equals("help") || isFlagSet(args, "-?")) {
                printUsage();
                return;
            }

            List<LoadableResource> resources = determineResources(args);
            if (resources.isEmpty()) {
                printUsage();
                return;
            }

            Map<String, String> envVars = ConfigUtils.environmentVariablesToPropertyMap();

            Properties properties = new Properties();
            initializeDefaults(properties);
            loadConfigurationFromConfigFiles(properties, args, envVars);
            properties.putAll(envVars);
            overrideConfigurationWithArgs(properties, args);

            loadVaultPasswordFile(properties);
            if (!isSuppressPrompt(args)) {
                promptForVaultPassword(properties);
            }

            dumpConfiguration(properties);

            if (!properties.containsKey(ConfigUtils.VAULT_PASSWORD)) {
                printUsage();
                LOG.error("Vault password is required");
                return;
            }


            for (LoadableResource resource : resources) {
                executeOperation(properties, operation, resource);
            }
        } catch (Exception e) {
            if (logLevel == Level.DEBUG) {
                LOG.error("Unexpected error", e);
            } else {
                if (e instanceof FlywayException) {
                    LOG.error(e.getMessage());
                } else {
                    LOG.error(e.toString());
                }
            }
            System.exit(1);
        }
    }


    /**
     * Determine the operation Flyway should execute.
     *
     * @param args The command-line arguments passed in.
     * @return The operation. An empty string if none.
     */
    protected static String determineOperation(String[] args) {
        for (String arg : args) {
            if (!arg.startsWith("-")) {
                return arg;
            }
        }

        return "";
    }

    /**
     * Determine the resources to apply operation
     *
     * @param args The command-line arguments passed in.
     * @return The resources. An empty list if none.
     */
    protected static List<LoadableResource> determineResources(String[] args) {
        List<LoadableResource> resources = new ArrayList<>();

        boolean first = true;
        for (String arg : args) {
            if (!arg.startsWith("-")) {
                if (first) {
                    first = false;
                    continue;
                }
                resources.add(new FileSystemResource(arg));
            }
        }

        return resources;
    }

    /**
     * Executes this operation
     *
     *
     * @param properties The configured properties.
     * @param operation The operation to execute.
     * @param resource The resource to apply operation
     */
    protected static void executeOperation(Properties properties, String operation, LoadableResource resource) {
        if ("encrypt".equals(operation)) {
            encrypt(properties, resource);
        } else if ("decrypt".equals(operation)) {
            decrypt(properties, resource);
        } else {
            LOG.error("Invalid operation: " + operation);
            printUsage();
            System.exit(1);
        }
    }

    /**
     * Prints the usage instructions on the console.
     */
    protected static void printUsage() {
        LOG.info("Usage");
        LOG.info("=====");
        LOG.info("");
        LOG.info("sqlvault [options] command file1 file2 ...");
        LOG.info("");
        LOG.info("Commands");
        LOG.info("--------");
        LOG.info("encrypt  : Encrypt the files provided in arguments");
        LOG.info("decrypt  : Decrypt the files provided in arguments");
        LOG.info("");
        LOG.info("Options (Format: -key=value)");
        LOG.info("-------");
        LOG.info("vaultPassword                : Vault password to encrypt/decrypt SQL source file");
        LOG.info("vaultPasswordFile            : Load vault password from a file to encrypt/decrypt SQL source file");
        LOG.info("vaultCipher                  : Vault cipher to encrypt SQL source file: AES128 (default) or AES256 (need crypto.policy=unlimited)");
        LOG.info("askVaultPassword             : Ask vault password to encrypt/decrypt SQL source file");
        LOG.info("");
        LOG.info("Flags");
        LOG.info("-----");
        LOG.info("-X : Print debug output");
        LOG.info("-q : Suppress all output, except for errors and warnings");
        LOG.info("-v : Print the Flyway version and exit");
        LOG.info("-? : Print this usage info and exit");
        LOG.info("");
        LOG.info("Example");
        LOG.info("-------");
        LOG.info("sqlvault decrypt migration.sql");
        LOG.info("");
        LOG.info("More info at https://flywaydb.org/documentation/commandline");
    }

    /**
     * Encrypt a resource
     *
     * @param properties The properties object to load to configuration into.
     * @param resource The resource to encrypt.
     */
    protected static void encrypt(Properties properties, LoadableResource resource) {

        String vaultCipher = SqlScriptVault.DEFAULT_CIPHER;
        if (properties.containsKey(ConfigUtils.VAULT_CIPHER)) {
            vaultCipher = properties.getProperty(ConfigUtils.VAULT_CIPHER);
        }

        try {
            String sqlScriptSource = resource.loadAsString("UTF-8");
            if (SqlScriptVault.isEncrypted(sqlScriptSource)) {
                LOG.warn(resource.getLocation() + " is already encrypted");
                return;
            }

            String vaultPassword = properties.getProperty(ConfigUtils.VAULT_PASSWORD);

            BufferedWriter writer = new BufferedWriter(new FileWriter(resource.getLocation()));
            writer.write(SqlScriptVault.encrypt(sqlScriptSource, vaultPassword, vaultCipher));
            writer.close();
        }
        catch(IOException e) {
            new FlywayException("Error to write in " + resource.getLocation(), e);
        }
    }


    /**
     * Decrypt a resource
     *
     * @param properties The properties object to load to configuration into.
     * @param resource The resource to decrypt.
     */
    protected static void decrypt(Properties properties, LoadableResource resource) {

        try {
            String sqlScriptSource = resource.loadAsString("UTF-8");
            if (!SqlScriptVault.isEncrypted(sqlScriptSource)) {
                LOG.warn(resource.getLocation() + " is not encrypted");
                return;
            }

            String vaultPassword = properties.getProperty(ConfigUtils.VAULT_PASSWORD);

            BufferedWriter writer = new BufferedWriter(new FileWriter(resource.getLocation()));
            writer.write(SqlScriptVault.decrypt(sqlScriptSource, vaultPassword));
            writer.close();
        }
        catch(IOException e) {
            new FlywayException("Error to write in " + resource.getLocation(), e);
        }
    }

}