package org.flywaydb.core.internal.dbsupport.redshift;

import java.sql.Connection;
import java.sql.Types;

import org.flywaydb.core.internal.dbsupport.JdbcTemplate;

public class RedshfitDbSupportViaPostgreSQLDriver extends RedshiftDbSupport {

    /**
     * Creates a new instance.
     *
     * @param connection The connection to use.
     */
    public RedshfitDbSupportViaPostgreSQLDriver(Connection connection) {
        super(new JdbcTemplate(connection, Types.NULL));
    }

}
