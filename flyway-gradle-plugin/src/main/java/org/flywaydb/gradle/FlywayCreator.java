package org.flywaydb.gradle;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.StringUtils;
import org.gradle.api.Project;

import java.util.*;

public class FlywayCreator {

    private static final String PLACEHOLDERS_PROPERTY_PREFIX = "flyway.placeholders.";

    public static Flyway create(Project project, FlywayExtensionBase taskExtension, FlywayExtensionBase localExtension, FlywayExtension masterExtension) {
        Map<String, String> conf = new HashMap<String, String>();
        putIfSet(conf, "driver", taskExtension.driver, localExtension.driver, masterExtension.driver);
        putIfSet(conf, "url", taskExtension.url, localExtension.url, masterExtension.url);
        putIfSet(conf, "user", taskExtension.user, localExtension.user, masterExtension.user);
        putIfSet(conf, "password", taskExtension.password, localExtension.password, masterExtension.password);
        putIfSet(conf, "table", taskExtension.table, localExtension.table, masterExtension.table);
        putIfSet(conf, "baselineVersion", taskExtension.baselineVersion, localExtension.baselineVersion, masterExtension.baselineVersion);
        putIfSet(conf, "baselineDescription", taskExtension.baselineDescription, localExtension.baselineDescription, masterExtension.baselineDescription);
        putIfSet(conf, "sqlMigrationPrefix", taskExtension.sqlMigrationPrefix, masterExtension.sqlMigrationPrefix, masterExtension.sqlMigrationPrefix);
        putIfSet(conf, "repeatableSqlMigrationPrefix", taskExtension.repeatableSqlMigrationPrefix, masterExtension.repeatableSqlMigrationPrefix, masterExtension.repeatableSqlMigrationPrefix);
        putIfSet(conf, "sqlMigrationSeparator", taskExtension.sqlMigrationSeparator, localExtension.sqlMigrationSeparator, masterExtension.sqlMigrationSeparator);
        putIfSet(conf, "sqlMigrationSuffix", taskExtension.sqlMigrationSuffix, localExtension.sqlMigrationSuffix, masterExtension.sqlMigrationSuffix);
        putIfSet(conf, "encoding", taskExtension.encoding, localExtension.encoding, masterExtension.encoding);
        putIfSet(conf, "placeholderReplacement", taskExtension.placeholderReplacement, localExtension.placeholderReplacement, masterExtension.placeholderReplacement);
        putIfSet(conf, "placeholderPrefix", taskExtension.placeholderPrefix, localExtension.placeholderPrefix, masterExtension.placeholderPrefix);
        putIfSet(conf, "placeholderSuffix", taskExtension.placeholderSuffix, localExtension.placeholderSuffix, masterExtension.placeholderSuffix);
        putIfSet(conf, "target", taskExtension.target, localExtension.target, masterExtension.target);
        putIfSet(conf, "outOfOrder", taskExtension.outOfOrder, localExtension.outOfOrder, masterExtension.outOfOrder);
        putIfSet(conf, "validateOnMigrate", taskExtension.validateOnMigrate, localExtension.validateOnMigrate, masterExtension.validateOnMigrate);
        putIfSet(conf, "cleanOnValidationError", taskExtension.cleanOnValidationError, localExtension.cleanOnValidationError, masterExtension.cleanOnValidationError);
        putIfSet(conf, "ignoreFutureMigrations", taskExtension.ignoreFutureMigrations, localExtension.ignoreFutureMigrations, masterExtension.ignoreFutureMigrations);
        putIfSet(conf, "cleanDisabled", taskExtension.cleanDisabled, localExtension.cleanDisabled, masterExtension.cleanDisabled);
        putIfSet(conf, "baselineOnMigrate", taskExtension.baselineOnMigrate, localExtension.baselineOnMigrate, masterExtension.baselineOnMigrate);
        putIfSet(conf, "skipDefaultResolvers", taskExtension.skipDefaultResolvers, localExtension.skipDefaultResolvers, masterExtension.skipDefaultResolvers);
        putIfSet(conf, "skipDefaultCallbacks", taskExtension.skipDefaultCallbacks, localExtension.skipDefaultCallbacks, masterExtension.skipDefaultCallbacks);
        putListIfSet(conf, "schemas", taskExtension.schemas, localExtension.schemas, masterExtension.schemas, masterExtension.schemasInstruction);

        conf.put("flyway.locations", Location.FILESYSTEM_PREFIX + project.getProjectDir().getAbsolutePath() + "/src/main/resources/db/migration");
        putListIfSet(conf, "locations", taskExtension.locations,localExtension.locations,masterExtension.locations, masterExtension.locationsInstruction);

        putListIfSet(conf, "resolvers", taskExtension.resolvers,localExtension.resolvers,masterExtension.resolvers, masterExtension.resolversInstruction);
        putListIfSet(conf, "callbacks", taskExtension.callbacks,localExtension.callbacks,masterExtension.callbacks, masterExtension.callbacksInstruction);

        putMapIfSet(conf, taskExtension.placeholders,localExtension.placeholders,masterExtension.placeholders, masterExtension.placeholdersInstruction);

        addConfigFromProperties(conf, project.getProperties());
        addConfigFromProperties(conf, System.getProperties());

        Flyway flyway = new Flyway();
        flyway.configure(conf);
        return flyway;
    }



    /**
     * Puts this property in the config if it has been set either in the task or the extension.
     *
     * @param config         The config.
     * @param key            The peoperty name.
     * @param propValue      The value in the plugin.
     * @param extensionValue The value in the extension.
     */
    private static void putIfSet(Map<String, String> config, String key, Object propValue, Object extensionValue, Object masterExtensionValue) {
        if (propValue != null) {
            config.put("flyway." + key, propValue.toString());
        } else if (extensionValue != null) {
            config.put("flyway." + key, extensionValue.toString());
        } else if (masterExtensionValue != null) {
            config.put("flyway." + key, masterExtensionValue.toString());
        }
    }

    private static void putListIfSet(
            Map<String, String> config,
            String key,
            String[] propValue,
            String[] extensionValue,
            String[] masterExtensionValue,
            ListPropertyInstruction instruction) {
        switch (instruction){
            case PRIORITIZE:
                putIfSet(config,
                        key,
                        StringUtils.arrayToCommaDelimitedString(propValue),
                        StringUtils.arrayToCommaDelimitedString(extensionValue),
                        StringUtils.arrayToCommaDelimitedString(masterExtensionValue));
                break;
            case MERGE:
                config.put("flyway." + key,
                        mergeListToString(
                                Arrays.asList(propValue, extensionValue, masterExtensionValue)));
                break;
        }
    }

    private static void putMapIfSet(
            Map<String, String> config,
            Map<Object, Object> propMap,
            Map<Object, Object> extensionMap,
            Map<Object, Object> masterExtensionMap,
            ListPropertyInstruction instruction) {
        switch (instruction){
            case PRIORITIZE:
                if (propMap != null) {
                    putMap(config, PLACEHOLDERS_PROPERTY_PREFIX, propMap);
                } else if (extensionMap != null) {
                    putMap(config, PLACEHOLDERS_PROPERTY_PREFIX, extensionMap);
                } else if (masterExtensionMap != null) {
                    putMap(config, PLACEHOLDERS_PROPERTY_PREFIX, masterExtensionMap);
                }
                break;
            case MERGE:
                putMap(config,
                        PLACEHOLDERS_PROPERTY_PREFIX,
                        mergeMap(Arrays.asList(masterExtensionMap, extensionMap, propMap)));
                break;
        }
    }

    private static void putMap(Map<String, String> config, String prefix, Map<Object, Object> placeholders) {
        for (Map.Entry<Object, Object> entry : placeholders.entrySet()) {
            config.put(prefix + entry.getKey().toString(), entry.getValue().toString());
        }
    }

    private static String mergeListToString(List<String[]> arraysToMerge) {

        ArrayList<String> returnList = new ArrayList<String>();

        for(String[] arrayToMerge : arraysToMerge) {
            for (String mergeValue : arrayToMerge) {
                if (!(returnList.contains(mergeValue))) {
                    returnList.add(mergeValue);
                }
            }
        }
        return StringUtils.arrayToCommaDelimitedString(returnList.toArray(new String[0]));
    }

    private static Map<Object, Object> mergeMap(
            List<Map<Object, Object>> mapsToMerge) {

        Map<Object, Object> returnMap = new HashMap<Object, Object>();

        for(Map<Object, Object> mapToMerge : mapsToMerge) {
            returnMap.putAll(mapToMerge);
        }

        return returnMap;
    }



    private static void addConfigFromProperties(Map<String, String> config, Properties properties) {
        for (String prop : properties.stringPropertyNames()) {
            if (prop.startsWith("flyway.")) {
                config.put(prop, properties.getProperty(prop));
            }
        }
    }

    private static void addConfigFromProperties(Map<String, String> config, Map<String, ?> properties) {
        for (String prop : properties.keySet()) {
            if (prop.startsWith("flyway.")) {
                config.put(prop, properties.get(prop).toString());
            }
        }
    }
}
