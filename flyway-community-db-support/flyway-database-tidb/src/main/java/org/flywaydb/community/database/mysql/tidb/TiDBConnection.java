package org.flywaydb.community.database.mysql.tidb;

import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.database.mysql.MySQLConnection;
import org.flywaydb.database.mysql.MySQLDatabase;
import org.flywaydb.database.mysql.MySQLNamedLockTemplate;
import org.flywaydb.core.internal.exception.FlywaySqlException;

import java.sql.SQLException;
import java.util.concurrent.Callable;

public class TiDBConnection extends MySQLConnection {

    private static final String TIDB_TXN_MODE = "tidb_txn_mode";

    public TiDBConnection(MySQLDatabase database, java.sql.Connection connection) {
        super(database, connection);
    }

    private void setTidbTxnMode() {
        try {
            // Since we use a separate connection for the lock, it is not required to restore the transaction mode.
            jdbcTemplate.execute("SET " + TIDB_TXN_MODE + "=pessimistic");
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to set value for 'tidb_txn_mode' variable", e);
        }
    }

    @Override
    public <T> T lock(Table table, Callable<T> callable) {
        setTidbTxnMode();
        return super.lock(table, callable);
    }

    @Override
    protected boolean canUseNamedLockTemplate() {return false;}
}