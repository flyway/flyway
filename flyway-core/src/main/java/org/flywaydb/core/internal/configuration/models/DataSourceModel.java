package org.flywaydb.core.internal.configuration.models;

import javax.sql.DataSource;
import lombok.Getter;
import lombok.Setter;
import org.flywaydb.core.internal.database.DatabaseType;

@Getter
@Setter
public class DataSourceModel {
    private DataSource dataSource;
    private boolean dataSourceGenerated;
    private DatabaseType databaseType;

    public DataSourceModel(DataSource dataSource, boolean dataSourceGenerated) {
        this.dataSource = dataSource;
        this.dataSourceGenerated = dataSourceGenerated;
        this.databaseType = null;
    }
}