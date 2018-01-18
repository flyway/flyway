package org.flywaydb.core.internal.dbsupport.neo4j;

import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;

public class Neo4jSqlStatementBuilder extends SqlStatementBuilder{

    @Override
    protected void applyStateChanges(String line) {
        super.applyStateChanges(line);

        if(!executeInTransaction) {
            return;
        }


        String cleanSingleLine = line.replaceAll("\\s+", " ");
        if(cleanSingleLine.trim().matches("^(CREATE|DROP) CONSTRAINT.*")) {
            executeInTransaction = false;
        }
    }

}
