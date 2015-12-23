package org.flywaydb.core.internal.dbsupport.redshift;

import java.sql.Connection;
import java.sql.Types;

import org.flywaydb.core.internal.dbsupport.JdbcTemplate;

public class RedshfitDbSupportViaRedshiftDriver extends RedshiftDbSupport {

    /**
     * Creates a new instance.
     *
     * @param connection The connection to use.
     */
    public RedshfitDbSupportViaRedshiftDriver(Connection connection) {
        super(new JdbcTemplate(connection, Types.VARCHAR));
    }

}
