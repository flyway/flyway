package org.flywaydb.core.internal.resolver.sql;

import junit.framework.Assert;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.mysql.MySQLDbSupport;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.scanner.filesystem.FileSystemResource;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test Stream feature
 */
public class SqlMigrationExecutorMediumTest {
    int cpt = 0;
    boolean checkEndFile = false;
    boolean checkPlaceHolderFile = false;
    @Test
    public void testExecuteStream() throws UnsupportedEncodingException {

        String path = URLDecoder.decode(getClass().getClassLoader().getResource("migration/sql/dbdump.sql").getPath(), "UTF-8");
        FileSystemResource resource = new FileSystemResource(path);

        Map<String,String> map =  new HashMap<String, String>();
        map.put("PLACEHOLDER_KEY","PLACEHOLDER_VALUE");
        PlaceholderReplacer placeHolder = new PlaceholderReplacer(map, "${", "}");

        SqlMigrationExecutor exec  = new SqlMigrationExecutor(new MySQLDbSupport(null),resource, placeHolder,"UTF-8"){
            @Override
            protected JdbcTemplate newJdbcTemplate(Connection connection) {
                return new JdbcTemplate(null,0){
                    public void executeStatement(List<String> sqls) throws BatchUpdateException,SQLException {
                        cpt++;
                        checkEndFile = sqls.get(sqls.size()-1).contains("END");
                        checkPlaceHolderFile = sqls.get(sqls.size()-1).contains("PLACEHOLDER_VALUE");
                    }
                    public void executeStatement(String sql) throws BatchUpdateException,SQLException {
                        cpt++;
                        checkEndFile = sql.contains("END");
                        checkPlaceHolderFile = sql.contains("PLACEHOLDER_VALUE");
                    }

                };
            }
        };
        exec.execute(null);

        assertEquals(68, cpt);
        assertTrue(checkEndFile);
        assertTrue(checkPlaceHolderFile);




    }




}
