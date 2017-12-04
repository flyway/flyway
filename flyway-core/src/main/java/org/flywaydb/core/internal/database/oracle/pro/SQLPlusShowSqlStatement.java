/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.oracle.pro;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.AbstractSqlStatement;
import org.flywaydb.core.internal.util.jdbc.ContextImpl;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.jdbc.Result;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A SQL*Plus SHOW statement.
 */
public class SQLPlusShowSqlStatement extends AbstractSqlStatement {
    private static final Log LOG = LogFactory.getLog(SQLPlusShowSqlStatement.class);

    public SQLPlusShowSqlStatement(int lineNumber, String sql) {
        super(lineNumber, sql);
    }

    @Override
    public List<Result> execute(ContextImpl errorContext, JdbcTemplate jdbcTemplate) throws SQLException {
        String option = sql.substring(sql.indexOf(" ") + 1).toUpperCase(Locale.ENGLISH);
        if ("CON_ID".equals(option)) {
            conId(jdbcTemplate);
        } else if (option.startsWith("ERR")) {
            err(jdbcTemplate, option);
        } else if (option.startsWith("REL")) {
            rel(jdbcTemplate);
        } else if (option.startsWith("USER")) {
            user(jdbcTemplate);
        } else {
            LOG.warn("Unknown option for SHOW: " + option);
        }
        return new ArrayList<Result>();
    }

    private void conId(JdbcTemplate jdbcTemplate) throws SQLException {
        LOG.info("CON_ID\n------------------------------\n"
                + jdbcTemplate.queryForString("SELECT CON_ID FROM V$VERSION WHERE BANNER LIKE 'Oracle Database%'"));
    }

    private void err(JdbcTemplate jdbcTemplate, String option) throws SQLException {
        String query = "SELECT TYPE,NAME,LINE,POSITION,TEXT FROM USER_ERRORS";
        String orderBy = " ORDER BY SEQUENCE";

        List<Map<String, String>> result;
        if (option.matches("(ERR|ERRORS)")) {
            result = jdbcTemplate.queryForList(query + orderBy);
        } else {
            String[] typeName = StringUtils.tokenizeToStringArray(option.substring(option.indexOf(" ") + 1), " ");
            result = jdbcTemplate.queryForList(query + " WHERE TYPE=? AND NAME=?" + orderBy, typeName);
        }
        if (result.isEmpty()) {
            LOG.info("No errors.");
        } else {
            StringBuilder output = new StringBuilder(
                    "Errors for " + result.get(0).get("TYPE") + " " + result.get(0).get("NAME") + ":\n\n" +
                            "LINE/COL ERROR\n" +
                            "-------- -----------------------------------------------------------------\n");
            for (Map<String, String> row : result) {
                output.append(StringUtils.trimOrPad(row.get("LINE") + "/" + row.get("POSITION"), 8))
                        .append(" ").append(row.get("TEXT").replace("\n", "\n         ")).append("\n");
            }
            LOG.info(output.toString().substring(0, output.length() - 1));
        }
    }

    private void rel(JdbcTemplate jdbcTemplate) throws SQLException {
        LOG.info("release "
                + jdbcTemplate.queryForString(
                "SELECT VERSION FROM PRODUCT_COMPONENT_VERSION WHERE PRODUCT LIKE 'Oracle Database%'")
                .replace(".", "0"));
    }

    private void user(JdbcTemplate jdbcTemplate) throws SQLException {
        LOG.info("USER is \"" + jdbcTemplate.getConnection().getMetaData().getUserName() + "\"");
    }
}
