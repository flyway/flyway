package org.flywaydb.core.internal.configuration.models;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.flywaydb.core.internal.util.MergeUtils;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class ConfigurationModel {
    private Map<String, EnvironmentModel> environments = new HashMap<>();
    private FlywayModel flyway = new FlywayModel();
    private String id;

    @JsonAnySetter
    private Map<String,Object> rootConfigurations = new HashMap<>();

    public static ConfigurationModel defaults() {
        ConfigurationModel model = new ConfigurationModel();
        model.flyway = FlywayModel.defaults();
        model.environments.put("default", new EnvironmentModel());
        return model;
    }

    public ConfigurationModel merge(ConfigurationModel otherPojo) {
        ConfigurationModel result = new ConfigurationModel();
        result.id = MergeUtils.merge(id, otherPojo.id);

        result.flyway = flyway != null ? flyway.merge(otherPojo.flyway) : otherPojo.flyway;
        result.environments = MergeUtils.merge(environments, otherPojo.environments, EnvironmentModel::merge);
        result.rootConfigurations = MergeUtils.merge(rootConfigurations, otherPojo.rootConfigurations, (a,b) -> b != null ? b : a);
        return result;
    }

    public static ConfigurationModel clone(ConfigurationModel pojo) {
        ConfigurationModel basePojo = new ConfigurationModel();
        return basePojo.merge(pojo);
    }
}