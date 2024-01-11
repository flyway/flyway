package org.flywaydb.core.extensibility;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.flywaydb.core.api.configuration.Configuration;

public interface Plugin extends Comparable<Plugin> {
    @JsonIgnore
    default boolean isLicensed(Configuration configuration) {
        return true;
    }
    @JsonIgnore
    default boolean isEnabled() {
        return true;
    }

    @JsonIgnore
    default String getName(){
        return this.getClass().getSimpleName();
    }

    @JsonIgnore
    default String getPluginVersion(Configuration config) {
        return null;
    }

    /**
     * High numbers indicate that this type will be used in preference to lower priorities.
     */
    @JsonIgnore
    default int getPriority() {
        return 0;
    }

    default int compareTo(Plugin o) {
        return o.getPriority() - getPriority();
    }

    default Plugin copy() {
        return this;
    }
}