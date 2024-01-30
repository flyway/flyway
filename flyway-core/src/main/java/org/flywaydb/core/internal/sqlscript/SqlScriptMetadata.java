package org.flywaydb.core.internal.sqlscript;

import lombok.CustomLog;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.extensibility.LicenseGuard;
import org.flywaydb.core.extensibility.Tier;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.license.FlywayEditionUpgradeRequiredException;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.PlaceholderReplacingReader;

import java.util.HashMap;
import java.util.Map;

import static org.flywaydb.core.internal.configuration.ConfigUtils.removeBoolean;
import static org.flywaydb.core.internal.util.BooleanEvaluator.evaluateExpression;

@CustomLog
public class SqlScriptMetadata {
    private static final String EXECUTE_IN_TRANSACTION = "executeInTransaction";
    private static final String ENCODING = "encoding";
    private static final String PLACEHOLDER_REPLACEMENT = "placeholderReplacement";
    private static final String SHOULD_EXECUTE = "shouldExecute";

    private final Boolean executeInTransaction;
    private final String encoding;
    private final boolean placeholderReplacement;
    private boolean shouldExecute;

    private SqlScriptMetadata(Map<String, String> metadata, Configuration config) {
        // Make copy to prevent removing elements from the original
        metadata = new HashMap<>(metadata);

        this.executeInTransaction = removeBoolean(metadata, EXECUTE_IN_TRANSACTION);
        this.encoding = metadata.remove(ENCODING);

        this.placeholderReplacement = Boolean.parseBoolean(metadata.getOrDefault(PLACEHOLDER_REPLACEMENT, "true"));
        metadata.remove(PLACEHOLDER_REPLACEMENT);

        this.shouldExecute = true;







        {
            if (metadata.containsKey(SHOULD_EXECUTE)) {
                throw new FlywayEditionUpgradeRequiredException(Tier.TEAMS, LicenseGuard.getTier(config), "shouldExecute");
            }
        }
        ConfigUtils.checkConfigurationForUnrecognisedProperties(metadata, null);
    }

    public Boolean executeInTransaction() {
        return executeInTransaction;
    }

    public String encoding() {
        return encoding;
    }

    public boolean placeholderReplacement() {
        return placeholderReplacement;
    }

    public boolean shouldExecute() {
        return shouldExecute;
    }

    public static boolean isMultilineBooleanExpression(String line) {
        return !line.startsWith(SHOULD_EXECUTE) && (line.contains("==") || line.contains("!="));
    }

    public static SqlScriptMetadata fromResource(LoadableResource resource, Parser parser, Configuration config) {
        if (resource != null) {
            LOG.debug("Found script configuration: " + resource.getFilename());
            if (parser == null) {
                return new SqlScriptMetadata(ConfigUtils.loadConfigurationFromReader(resource.read()), config);
            }
            return new SqlScriptMetadata(ConfigUtils.loadConfigurationFromReader(
                    PlaceholderReplacingReader.create(parser.configuration, parser.parsingContext, resource.read())), parser.configuration);
        }
        return new SqlScriptMetadata(new HashMap<>(), config);
    }

    public static LoadableResource getMetadataResource(ResourceProvider resourceProvider, LoadableResource resource) {
        if (resourceProvider == null) {
            return null;
        }
        return resourceProvider.getResource(resource.getRelativePath() + ".conf");
    }
}