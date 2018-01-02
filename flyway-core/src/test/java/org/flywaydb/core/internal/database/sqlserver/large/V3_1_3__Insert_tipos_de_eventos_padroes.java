/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.core.internal.database.sqlserver.large;

import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.jdbc.core.JdbcTemplate;

@SuppressWarnings("UnusedDeclaration")
public class V3_1_3__Insert_tipos_de_eventos_padroes implements SpringJdbcMigration {

    public void migrate(JdbcTemplate jdbc) throws Exception {
        jdbc.update("INSERT INTO EVENTO_TIPO VALUES ('Casamento')");
    }

}
