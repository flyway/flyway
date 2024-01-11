package org.flywaydb.core.internal.configuration.models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ResolvedEnvironment {
    private String url;
    private String user;
    private String password;
    private String driver;
    private List<String> schemas;
    private List<String> jarDirs;
    private String token;
    private Integer connectRetries;
    private Integer connectRetriesInterval;
    private String initSql;
    private Map<String, String> jdbcProperties;

    public EnvironmentModel toEnvironmentModel() {
        EnvironmentModel result = new EnvironmentModel();
        result.setUrl(url);
        result.setPassword(password);
        result.setUser(user);
        result.setDriver(driver);
        result.setSchemas(schemas);
        result.setJarDirs(jarDirs);
        result.setToken(token);
        result.setConnectRetries(connectRetries);
        result.setConnectRetriesInterval(connectRetriesInterval);
        result.setInitSql(initSql);
        result.setJdbcProperties(jdbcProperties);
        result.setResolvers(Map.of());
        result.setProvisioner("none");
        return result;
    }
}