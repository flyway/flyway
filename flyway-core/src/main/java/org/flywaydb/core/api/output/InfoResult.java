package org.flywaydb.core.api.output;

import java.time.LocalDateTime;
import java.util.List;

public class InfoResult extends HtmlResult {
    private static final String COMMAND = "info";
    public String schemaVersion;
    public String schemaName;
    public List<InfoOutput> migrations;
    public String flywayVersion;
    public String database;
    public boolean allSchemasEmpty;

    public InfoResult(String flywayVersion,
                      String database,
                      String schemaVersion,
                      String schemaName,
                      List<InfoOutput> migrations,
                      boolean allSchemasEmpty) {
        super(LocalDateTime.now(), COMMAND);
        this.flywayVersion = flywayVersion;
        this.database = database;
        this.schemaVersion = schemaVersion;
        this.schemaName = schemaName;
        this.migrations = migrations;
        this.allSchemasEmpty = allSchemasEmpty;
    }
}