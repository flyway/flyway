package org.flywaydb.core.internal.configuration.resolvers;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.flywaydb.core.ProgressLogger;
import org.flywaydb.core.api.ErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.ConfigurationExtension;
import org.flywaydb.core.extensibility.Plugin;
import org.flywaydb.core.internal.configuration.models.EnvironmentModel;
import org.flywaydb.core.internal.configuration.models.ResolvedEnvironment;
import org.flywaydb.core.internal.plugin.PluginRegister;

public class EnvironmentResolver {

    private final Map<String, PropertyResolver> propertyResolvers;
    private final Map<String, ? extends EnvironmentProvisioner> environmentProvisioners;

    public EnvironmentResolver(final Map<String, PropertyResolver> propertyResolvers,
        final Map<String, ? extends EnvironmentProvisioner> environmentProvisioners) {
        this.propertyResolvers = new HashMap<>(propertyResolvers);
        this.environmentProvisioners = new HashMap<>(environmentProvisioners);
    }

    public ResolvedEnvironment resolve(final String environmentName, final EnvironmentModel environment,
        final Configuration configuration, final ProgressLogger progress) {
        return resolve(environmentName, environment, ProvisionerMode.Provision, configuration, progress);
    }

    public ResolvedEnvironment resolve(final String environmentName, final EnvironmentModel environment, final ProvisionerMode mode,
        final Configuration configuration, final ProgressLogger progress) {
        final Map<String, ConfigurationExtension> resolverConfigs = getEnvironmentPluginConfigMap(environment,
            configuration.getPluginRegister());
        final PropertyResolverContext context = new PropertyResolverContextImpl(environmentName, configuration,
            propertyResolvers,  resolverConfigs);

        final ResolvedEnvironment result = new ResolvedEnvironment();
        result.setDriver(environment.getDriver());
        result.setConnectRetries(environment.getConnectRetries());
        result.setConnectRetriesInterval(environment.getConnectRetriesInterval());
        result.setInitSql(environment.getInitSql());
        result.setSchemas(environment.getSchemas());
        result.setJarDirs(environment.getJarDirs());

        progress.pushSteps(2);
        final ProgressLogger provisionProgress = progress.subTask("provision");
        final ProgressLogger resolveProgress = progress.subTask("resolve");

        final EnvironmentProvisioner provisioner = getProvisioner(environment.getProvisioner(), context, provisionProgress);
        if (mode == ProvisionerMode.Provision) {
            progress.log("Provisioning environment " + environmentName + " with " + provisioner.getName());
            provisioner.preProvision(context, provisionProgress);
        } else if (mode == ProvisionerMode.Reprovision) {
            progress.log("Reprovisioning environment " + environmentName + " with " + provisioner.getName());
            provisioner.preReprovision(context, provisionProgress);
        }

        progress.log("Resolving environment properties " + environmentName);
        if (environment.getJdbcProperties() != null) {
            final Map<String, String> jdbcResolvedProps = new HashMap<>();
            for (final Map.Entry<String, String> entry : environment.getJdbcProperties().entrySet()) {
                jdbcResolvedProps.put(entry.getKey(), context.resolveValue(entry.getValue(), resolveProgress));
            }
            result.setJdbcProperties(jdbcResolvedProps);
        }

        result.setPassword(context.resolveValue(environment.getPassword(), resolveProgress));
        result.setUser(context.resolveValue(environment.getUser(), resolveProgress));
        result.setUrl(context.resolveValue(environment.getUrl(), resolveProgress));
        result.setToken(context.resolveValue(environment.getToken(), resolveProgress));

        if (mode == ProvisionerMode.Provision) {
            progress.log("Provisioning environment " + environmentName + " with " + provisioner.getName());
            provisioner.postProvision(context, result, provisionProgress);
        } else if (mode == ProvisionerMode.Reprovision) {
            progress.log("Reprovisioning environment " + environmentName + " with " + provisioner.getName());
            provisioner.postReprovision(context, result, provisionProgress);
        }

        return result;
    }

    private EnvironmentProvisioner getProvisioner(final String provisionerName, final PropertyResolverContext context,
        final ProgressLogger progress) {
        final String name = context.resolveValue(provisionerName, progress);
        if (name != null) {
            if (!environmentProvisioners.containsKey(provisionerName)) {
                throw new FlywayException(
                    "Unknown provisioner '" + provisionerName + "' for environment " + context.getEnvironmentName(),
                    ErrorCode.CONFIGURATION);
            }
            return environmentProvisioners.get(provisionerName);
        }
        return new EnvironmentProvisionerNone();
    }
    
    private Map<String, ConfigurationExtension> getEnvironmentPluginConfigMap(final EnvironmentModel environmentModel,
        final PluginRegister pluginRegister) {

        if (environmentModel.getResolvers() != null) {

            return environmentModel.getResolvers()
                .keySet()
                .stream()
                .collect(Collectors.toMap(key->key, v->getResolverConfig(environmentModel, pluginRegister, v)));
        }
        return null;
    }


    private ConfigurationExtension getResolverConfig(final EnvironmentModel environmentModel, final PluginRegister pluginRegister,
        final String key) {
        final Class<?> clazz = getResolverConfigClassFromKey(pluginRegister, key);

        if (clazz != null) {
            try {
                final var data = environmentModel.getResolvers().get(key);
                return (ConfigurationExtension) new ObjectMapper().convertValue(data, clazz);
            } catch (final IllegalArgumentException e) {
                throw new FlywayException("Error reading resolver configuration for resolver " + key + ": " + e.getMessage(), e, ErrorCode.CONFIGURATION);
            }
        }

        throw new FlywayException("Unable to find resolver: " + key);
    }

    private Class<? extends Plugin> getResolverClassFromKey(final PluginRegister pluginRegister, final String key) {
        Plugin plugin = pluginRegister.getPlugins(EnvironmentProvisioner.class).stream()
            .filter(p -> p.getName().equalsIgnoreCase(key))
            .findFirst()
            .orElse(null);


        if (plugin == null) {
            plugin = pluginRegister.getPlugins(PropertyResolver.class).stream()
                .filter(p -> p.getName().equalsIgnoreCase(key))
                .findFirst()
                .orElse(null);
        }

        if (plugin!=null){
            return plugin.getClass();
        }

        throw new FlywayException("Unable to find resolver: " + key);
    }

    private Class<?> getResolverConfigClassFromKey(final PluginRegister pluginRegister, final String key) {
        final Class<? extends Plugin> resolverClass = getResolverClassFromKey(pluginRegister, key);
        if (resolverClass == null) {
            return null;
        }

        final Plugin plugin = pluginRegister.getPlugin(resolverClass);
        if (plugin instanceof final EnvironmentProvisioner environmentProvisioner){
            return environmentProvisioner.getConfigClass();
        }
        if (plugin instanceof final PropertyResolver propertyResolver){
            return propertyResolver.getConfigClass();
        }

        throw new FlywayException("Unable to find resolver: " + key);
    }
}