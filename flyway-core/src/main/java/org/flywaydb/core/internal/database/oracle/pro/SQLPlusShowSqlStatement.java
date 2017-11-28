package org.flywaydb.core.internal.database.oracle.pro;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.AbstractSqlStatement;
import org.flywaydb.core.internal.database.JdbcTemplate;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.SQLException;
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
    public void execute(JdbcTemplate jdbcTemplate) throws SQLException {
        String option = extractOption();
        if ("CON_ID".equals(option)) {
            LOG.info("CON_ID\n------------------------------\n"
                    + jdbcTemplate.queryForString("SELECT CON_ID FROM V$VERSION WHERE BANNER LIKE 'Oracle Database%'"));
            return;
        }
        if (option.startsWith("ERR")) {
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
                return;
            }
            StringBuilder output = new StringBuilder(
                    "Errors for " + result.get(0).get("TYPE") + " " + result.get(0).get("NAME") + ":\n\n" +
                            "LINE/COL ERROR\n" +
                            "-------- -----------------------------------------------------------------\n");
            for (Map<String, String> row : result) {
                output.append(StringUtils.trimOrPad(row.get("LINE") + "/" + row.get("POSITION"), 8))
                        .append(" ").append(row.get("TEXT").replace("\n", "\n         ")).append("\n");
            }
            LOG.info(output.toString().substring(0, output.length() - 1));
            return;
        }
        if (option.startsWith("REL")) {
            LOG.info("release "
                    + jdbcTemplate.queryForString(
                    "SELECT VERSION FROM PRODUCT_COMPONENT_VERSION WHERE PRODUCT LIKE 'Oracle Database%'")
                    .replace(".", "0"));
            return;
        }
        LOG.warn("Unknown option for SHOW: " + option);
    }

    private String extractOption() {
        return sql.substring(sql.indexOf(" ") + 1).toUpperCase(Locale.ENGLISH);
    }
}
