/**
 * Copyright 2010-2016 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core.internal.dbsupport;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import org.flywaydb.core.api.FlywayException;

public class DryRun {
    private static final String SAVEPOINT_MARKER = "dryrun";
    private final Map<Connection, Savepoint> dryRunSavepoints = new WeakHashMap<Connection, Savepoint>();
    private final int connectionCount;
    
    public DryRun(int transactionIsolationLevel, DbSupport dbSupport, Connection... connections) {
        if (dbSupport == null) {
            throw new NullPointerException("A Dry-run requires dbSupport to determine whether connections can support a rollback successfully.");
        }

        if (!dbSupport.supportsDdlTransactions()) {
            throw new FlywayException("Dry-run is not supported with this database: " + dbSupport.getDbName());
        }
        for (Connection c : connections) {
            try {
                c.setTransactionIsolation(transactionIsolationLevel);
                c.setAutoCommit(false);
                dryRunSavepoints.put(c, c.setSavepoint(SAVEPOINT_MARKER));
            } catch (Exception e) {
                throw new FlywayException("Unable to start a dry-run: " + e.getMessage(), e);
            }
        }
        connectionCount = connections.length;
    }
    
    public DryRun(DbSupport dbSupport, Connection... connections) {
        this(Connection.TRANSACTION_READ_COMMITTED, dbSupport, connections);
    }

    public void rollback() throws FlywayException {
        FlywayException flywayException = null;
        Iterator<Map.Entry<Connection, Savepoint>> it = dryRunSavepoints.entrySet().iterator();
        int rollbacks = 0;
        while (it.hasNext()) {
            Map.Entry<Connection, Savepoint> entry = it.next();
            try {
                rollbacks++;
                entry.getKey().rollback(entry.getValue());
            } catch (SQLException e) {
                flywayException = new FlywayException("Unable to rollback the 'dryrun' transaction", e);
                // Keep going, attempt to rollback all provided connections
            }
            it.remove();
        }
        if (flywayException != null) {
            throw flywayException;
        }
        if (rollbacks != connectionCount) {
            throw new FlywayException("Only attempted to roll back " + rollbacks + " connections.  Dry-run was enabled for " + connectionCount + " connections.  Verify database integrity.");
        }
    }
}
