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
package org.flywaydb.core.internal.resolver.script;

import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.executor.Context;
import org.flywaydb.core.api.executor.MigrationExecutor;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.database.DatabaseExecutionStrategy;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.database.DatabaseTypeRegister;
import org.flywaydb.core.internal.jdbc.Results;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.resource.ResourceName;
import org.flywaydb.core.internal.util.FileUtils;
import org.flywaydb.core.internal.util.StringUtils;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CustomLog
@RequiredArgsConstructor
public class ScriptMigrationExecutor implements MigrationExecutor {
    private final LoadableResource resource;
    private final ParsingContext parsingContext;
    private final ResourceName resourceName;

    private final StatementInterceptor statementInterceptor;

    @Override
    public List<Results> execute(final Context context) throws SQLException {
        if (statementInterceptor != null) {
            statementInterceptor.scriptMigration(resource);
        } else if (context.getConnection() == null) {
            executeOnce(context);
        } else {
            DatabaseType databaseType = DatabaseTypeRegister.getDatabaseTypeForConnection(context.getConnection(), context.getConfiguration());

            DatabaseExecutionStrategy strategy = databaseType.createExecutionStrategy(context.getConnection());
            strategy.execute(() -> {
                executeOnce(context);
                return true;
            });
        }

        return List.of();
    }

    private void executeOnce(final Context context) {
        try {
            runScript(context);
        } catch (Exception e) {
            throw new FlywayException("Migration failed !", e);
        }
    }

    private String join(String joiner, List<String> strings) {
        if (strings.size() == 1) {
            return strings.get(0);
        }

        StringBuilder output = new StringBuilder();

        for (String s : strings) {
            output.append(s).append(joiner);
        }

        return output.toString();
    }

    List<String> getProcessArgs() {
        String resourcePath = resource.getAbsolutePathOnDisk();
        String resourceExt = StringUtils.getFileNameAndExtension(resourcePath).getRight();

        List<String> args = new ArrayList<>();

        if ("bat".equalsIgnoreCase(resourceExt) || "cmd".equalsIgnoreCase(resourceExt)) {
            args.add("cmd");
            args.add("/c");
            args.add(resourcePath);
        } else if ("ps1".equalsIgnoreCase(resourceExt)) {
            args.add("powershell");
            args.add(resourcePath);
        } else if ("py".equalsIgnoreCase(resourceExt)) {
            args.add("python");
            args.add(resourcePath);
        } else if ("sh".equalsIgnoreCase(resourceExt)) {
            args.add("sh");
            args.add(resourcePath);
        } else if ("bash".equalsIgnoreCase(resourceExt)) {
            args.add("bash");
            args.add(resourcePath);
        } else {
            File file = new File(resourcePath);
            if (!file.canExecute()) {
                file.setExecutable(true, true);
            }

            args.add(resourcePath);
        }

        return args;
    }

    private void setIfNotNull(ProcessBuilder builder, String property, String value) {
        if (value != null && !value.isEmpty()) {
            builder.environment().put(property, value);
        }
    }

    private void runScript(final Context context) throws Exception {
        List<String> args = getProcessArgs();
        LOG.info("Executing " + join(" ", args));

        String url = context.getConfiguration().getUrl();
        String username = context.getConfiguration().getUser();
        String password = context.getConfiguration().getPassword();
        String prefix = context.getConfiguration().getScriptPlaceholderPrefix();
        String suffix = context.getConfiguration().getScriptPlaceholderSuffix();

        parsingContext.updateFilenamePlaceholder(resourceName, context.getConfiguration());
        Map<String, String> placeHolders = parsingContext.getPlaceholders();
        placeHolders.putAll(context.getConfiguration().getPlaceholders());

        // If the url or username aren't set, try to read them from the connection metadata
        if (url == null && context.getConnection() != null) {
            try {
                url = context.getConnection().getMetaData().getURL();
            } catch (Exception ignored) {
            }
        }

        if (username == null && context.getConnection() != null) {
            try {
                username = context.getConnection().getMetaData().getUserName();
            } catch (Exception ignored) {
            }
        }

        ProcessBuilder builder = new ProcessBuilder(args);
        setIfNotNull(builder, "FLYWAY_URL", url);
        setIfNotNull(builder, "FLYWAY_USER", username);
        setIfNotNull(builder, "FLYWAY_PASSWORD", password);

        for (String key : placeHolders.keySet()) {
            String value = placeHolders.get(key);
            builder.environment().put(prefix + key.replace(':', '_') + suffix, value);
        }

        builder.redirectErrorStream(true);

        Process process = builder.start();
        String stdOut = FileUtils.copyToString(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
        int returnCode = process.waitFor();

        LOG.info(stdOut);

        if (returnCode != 0) {
            throw new FlywayException(stdOut);
        }
    }

    @Override
    public boolean canExecuteInTransaction() {
        return true;
    }

    @Override
    public boolean shouldExecute() {
        return true;
    }
}
