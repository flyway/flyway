package org.flywaydb.database.postgresql;

import lombok.Data;
import org.flywaydb.core.extensibility.ConfigurationExtension;

@Data
public class PostgreSQLConfigurationExtension implements ConfigurationExtension {
    private static final String TRANSACTIONAL_LOCK = "flyway.postgresql.transactional.lock";

    private TransactionalModel transactional = null;

    public boolean isTransactionalLock() {
        // null is default, default is true, done this way for merge reasons.
        return transactional == null || transactional.getLock() == null || transactional.getLock();
    }

    public void setTransactionalLock(boolean transactionalLock) {
        transactional = new TransactionalModel();
        transactional.setLock(transactionalLock);
    }
    @Override
    public String getConfigurationParameterFromEnvironmentVariable(String environmentVariable) {
        if ("FLYWAY_POSTGRESQL_TRANSACTIONAL_LOCK".equals(environmentVariable)) {
            return TRANSACTIONAL_LOCK;
        }
        return null;
    }

    @Override
    public String getNamespace() {
        return "postgresql";
    }
}