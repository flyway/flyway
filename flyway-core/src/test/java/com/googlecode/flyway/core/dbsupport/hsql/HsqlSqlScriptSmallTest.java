package com.googlecode.flyway.core.dbsupport.hsql;

import com.googlecode.flyway.core.migration.sql.PlaceholderReplacer;
import com.googlecode.flyway.core.migration.sql.SqlStatement;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Small test for HsqlSqlScript.
 */
public class HsqlSqlScriptSmallTest {
    @Test
    public void parseBeginAtomic() {
        HsqlSqlScript script = new HsqlSqlScript("CREATE TRIGGER uniqueidx_trigger BEFORE INSERT ON usertable \n" +
                "\tREFERENCING NEW ROW AS newrow\n" +
                "    FOR EACH ROW WHEN (newrow.name is not null)\n" +
                "\tBEGIN ATOMIC\n" +
                "      IF EXISTS (SELECT * FROM usertable WHERE usertable.name = newrow.name) THEN\n" +
                "        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'duplicate name';\n" +
                "      END IF;\n" +
                "    END;   ", PlaceholderReplacer.NO_PLACEHOLDERS);
        List<SqlStatement> sqlStatements = script.getSqlStatements();

        assertEquals(1, sqlStatements.size());
    }
}
