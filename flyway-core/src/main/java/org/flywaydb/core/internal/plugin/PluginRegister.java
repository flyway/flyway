package org.flywaydb.core.internal.plugin;

import lombok.CustomLog;
import lombok.NoArgsConstructor;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
@CustomLog
@NoArgsConstructor
public class PluginRegister {
    public final List<Plugin> REGISTERED_PLUGINS = new ArrayList<>();
    private final ClassLoader CLASS_LOADER = this.getClass().getClassLoader();
    private boolean hasRegisteredPlugins = false;

    public <T extends Plugin> T getPlugin(Class<T> clazz) {
        return (T) getPlugins()
                .stream()
                .filter(p -> p.getClass().getCanonicalName().equals(clazz.getCanonicalName()))
                .findFirst()
                .orElse(null);
    }

    public <T extends Plugin> List<T> getPlugins(Class<T> clazz) {
        return (List<T>) getPlugins()
                .stream()
                .filter(clazz::isInstance)
                .sorted()
                .collect(Collectors.toList());
    }

    public <T extends Plugin> List<T> getLicensedPlugins(Class<T> clazz, Configuration configuration) {
        return (List<T>) getPlugins()
                .stream()
                .filter(clazz::isInstance)
                .filter(p -> p.isLicensed(configuration))
                .sorted()
                .collect(Collectors.toList());
    }

    public <T extends Plugin> T getLicensedPlugin(String className, Configuration configuration) {
        return (T) getPlugins()
                .stream()
                .filter(p -> p.isLicensed(configuration))
                .filter(p -> p.getClass().getSimpleName().equals(className))
                .sorted()
                .findFirst()
                .orElse(null);
    }

    private List<Plugin> getPlugins() {
        registerPlugins();
        return REGISTERED_PLUGINS;
    }

    void registerPlugins() {
        synchronized (REGISTERED_PLUGINS) {
            if (hasRegisteredPlugins) {
                return;
            }

            for (Plugin plugin : ServiceLoader.load(Plugin.class, CLASS_LOADER)) {
                if (plugin.isEnabled()) {
                    REGISTERED_PLUGINS.add(plugin);
                }
            }

            hasRegisteredPlugins = true;
        }
    }

    public PluginRegister getCopy(){
        PluginRegister copy = new PluginRegister();
        copy.setRegisteredPlugins(getPlugins());
        return copy;
    }

    private void setRegisteredPlugins(List<Plugin> plugins) {
        REGISTERED_PLUGINS.clear();
        REGISTERED_PLUGINS.addAll(plugins.stream().map(Plugin::copy).collect(Collectors.toList()));
        hasRegisteredPlugins = true;
    }
}