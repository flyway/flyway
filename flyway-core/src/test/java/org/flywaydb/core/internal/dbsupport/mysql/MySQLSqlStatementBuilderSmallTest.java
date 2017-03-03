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
package org.flywaydb.core.internal.dbsupport.mysql;

import org.flywaydb.core.internal.dbsupport.Delimiter;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test for MySQLSqlStatementBuilder.
 */
public class MySQLSqlStatementBuilderSmallTest {
    private MySQLSqlStatementBuilder builder = new MySQLSqlStatementBuilder();

    @Test
    public void isCommentDirectiveRegularStatement() {
        assertFalse(builder.isCommentDirective("SELECT * FROM TABLE;"));
        assertFalse(builder.isInMultiLineCommentDirective);
    }

    @Test
    public void isCommentDirectiveNoVersion() {
        assertFalse(builder.isCommentDirective("/*SELECT * FROM TABLE*/;"));
        assertFalse(builder.isInMultiLineCommentDirective);
    }

    @Test
    public void isCommentDirectiveNoSemicolon() {
        assertTrue(builder.isCommentDirective("/*!12345 SELECT * FROM TABLE*/"));
        assertFalse(builder.isInMultiLineCommentDirective);
    }

    @Test
    public void isCommentDirectiveSemicolonNoSpace() {
        assertTrue(builder.isCommentDirective("/*!12345 SELECT * FROM TABLE*/;"));
        assertFalse(builder.isInMultiLineCommentDirective);
    }

    @Test
    public void isCommentDirectiveSemicolonSpace() {
        assertTrue(builder.isCommentDirective("/*!50003 SET @saved_cs_client = @@character_set_client */ ;"));
        assertFalse(builder.isInMultiLineCommentDirective);
    }

    @Test
    public void isUnquotedMultiLineCommentDirective() {
        assertFalse(builder.isCommentDirective("SELECT * FROM TABLE;"));
        assertFalse(builder.isInMultiLineCommentDirective);

        assertTrue(builder.isCommentDirective("/*!12345 CREATE TABLE tbl ("));
        assertTrue(builder.isInMultiLineCommentDirective);

        assertTrue(builder.isCommentDirective("foo varchar(5)"));
        assertTrue(builder.isInMultiLineCommentDirective);

        assertTrue(builder.isCommentDirective(") ENGINE=MyISAM*/;"));
        assertFalse(builder.isInMultiLineCommentDirective);

        assertFalse(builder.isCommentDirective("SELECT * FROM TABLE;"));
        assertFalse(builder.isInMultiLineCommentDirective);
    }

    @Test
    public void escapedSingleQuotes() {
        builder.setDelimiter(new Delimiter("$$", false));

        builder.addLine("CREATE PROCEDURE test_proc(testDate CHAR(10))");
        builder.addLine("BEGIN");
        builder.addLine("    SET @testSQL = CONCAT(");
        builder.addLine("        'INSERT INTO test_table (test_id, test_date) ',");
        builder.addLine("        ' VALUE (1, DATE(\\'', testDate, '\\'));');");
        builder.addLine("    PREPARE testStmt FROM @testSQL;");
        builder.addLine("    EXECUTE testStmt;");
        builder.addLine("    DEALLOCATE PREPARE testStmt;");
        builder.addLine("END $$");

        assertTrue(builder.isTerminated());
    }

    @Test
    public void createDefiner() {
        builder.setDelimiter(new Delimiter("$$", false));

        builder.addLine("CREATE DEFINER=`root`@`localhost` FUNCTION `ampx`.`IS_ADGROUP_CAMPAIGN_ACCOUNT_ACTIVE`(in_adgroup_id INTEGER) RETURNS tinyint(4)");
        builder.addLine("    READS SQL DATA");
        builder.addLine("begin");
        builder.addLine("    select");
        builder.addLine("    (1 = ag.ADGROUP_STATUS_ID and 1 = c.CAMPAIGN_STATUS_ID and 1 = a.ACCOUNT_STATUS_ID) into result");
        builder.addLine("    from ADGROUP ag inner join CAMPAIGN c on c.ID = ag.CAMPAIGN_ID");
        builder.addLine("    inner join ACCOUNT a on a.ID = c.ACCOUNT_ID");
        builder.addLine("    where ag.ID = in_adgroup_id;");
        builder.addLine("    return result;");
        builder.addLine("END $$");

        assertTrue(builder.isTerminated());
    }

    @Test
    public void stringEndingInX() throws Exception {
        builder.addLine("INSERT INTO Tablename (id) VALUES (' x');");
        assertTrue(builder.isTerminated());
    }

    @Test
    public void stringEndingInB() throws Exception {
        builder.addLine("INSERT INTO Tablename (id) VALUES (' b');");
        assertTrue(builder.isTerminated());
    }

    @Test
    public void stringEndingInCapitalB() throws Exception {
        builder.addLine("INSERT INTO `injured_situation` VALUES(null, now(), now(), 'Test ending with B');");
        assertTrue(builder.isTerminated());
    }

    @Test
    public void charsetCastedString() throws Exception {
        builder.addLine("INSERT INTO Tablename (id) VALUES (_utf8'hello');");
        assertTrue(builder.isTerminated());
    }

    @Test
    public void charsetCastedComplexString() throws Exception {
        builder.addLine("INSERT INTO Tablename (id) VALUES (_utf8'text with spaces and \" and pretend casts _utf8\\'hello\\' here _utf8');");
        assertTrue(builder.isTerminated());
    }

    @Test
    public void stringEndingInCastPrefix() throws Exception {
        builder.addLine("INSERT INTO Tablename (id) VALUES ('text goes here _utf8');");
        assertTrue(builder.isTerminated());
    }

    @Test
    public void stringEndingInDoubleQuote() throws Exception {
        builder.addLine("INSERT INTO Tablename (id) VALUES (' \"');");
        assertTrue(builder.isTerminated());
    }

    @Test
    public void stringEndingInSingleQuote() throws Exception {
        builder.addLine("INSERT INTO Tablename (id) VALUES (\"' '\");");
        assertTrue(builder.isTerminated());
    }

    @Test
    public void stringMixedQuotes() throws Exception {
        builder.addLine("SET sql_cmd = CONCAT(");
        assertFalse(builder.isTerminated());
        builder.addLine("    'SELECT");
        assertFalse(builder.isTerminated());
        builder.addLine("    \"',dt0,'\" AS from_date");
        assertFalse(builder.isTerminated());
        builder.addLine("FROM stats_bonus s1");
        assertFalse(builder.isTerminated());
        builder.addLine("WHERE s1.agg_date=\"',dt1,'\"');");
        assertTrue(builder.isTerminated());
    }

    @Test
    public void stringBeginningWithInSingleQuote() throws Exception {
        builder.addLine("INSERT INTO Tablename (id) VALUES (\"' \");");
        assertTrue(builder.isTerminated());
    }

    @Test
    public void stringEndingInDoubleQuoteMultiple() throws Exception {
        builder.addLine("insert into sample_table_a(id, string)\n" +
                "values (1, '[\"GIF\", \"JPG\", \"PNG\"]');");
        assertTrue(builder.isTerminated());
    }

    @Test
    public void stringEndingInDoubleQuoteComplex() throws Exception {
        builder.addLine("UPDATE TABLE_A SET entity_version = 2, last_updated_timestamp = '2013-04-20 13:55:17', " +
                "paragraph_text = 'Inauspicious. Isn\\'t that a great word? Let it roll off the tongue: in -awespish-us." +
                " I love words, which -as you\\'ll soon see -is a very good thing. There are all sorts of definitions" +
                " for this particular word. If you check the dictionary, you\\'ll learn it means \"suggesting that the" +
                " future is unpromising.\" So, an inauspicious event is a disaster that points toward a whole lot more" +
                " disasters down the road. Think of it as a bad start. Better yet, let\\'s define it by example. My" +
                " first encounter with organized sports was definitely \"inauspicious.\"', paragraph_text_unformatted" +
                " = 'Inauspicious.\\nIsn\\'t that a great word?\\nLet it roll off the tongue: in -awespish-us.\\nI love" +
                " words, which -as you\\'ll soon see -is a very good thing.\\nThere are all sorts of definitions for this" +
                " particular word.\\nIf you check the dictionary, you\\'ll learn it means \"suggesting that the future" +
                " is unpromising.\"\\nSo, an inauspicious event is a disaster that points toward a whole lot more" +
                " disasters down the road.\\nThink of it as a bad start.\\nBetter yet, let\\'s define it by example." +
                "\\nMy first encounter with organized sports was definitely \"inauspicious.\"' " +
                "WHERE id = '40288103-3df2e55a-013d-f2e5df6f-0181';");
        assertTrue(builder.isTerminated());
    }

    @Test
    public void stringEndingInDoubleQuoteComplex2() throws Exception {
        builder.addLine("UPDATE TABLE_A SET entity_version = 2, last_updated_timestamp = '2013-04-20 13:55:17'," +
                " paragraph_text = 'I\\'m not even sure what grade I was in when I decided to join the after -school" +
                " football program. Second grade sounds about right. I don\\'t remember the gym teacher\\'s name," +
                " either. So let\\'s just call him Mr. Growler. The first fact about sports that caught my attention" +
                " as I wandered toward the field behind the school was that everyone else seemed to have been born " +
                "knowing not only the rules to the game, but also exactly what to do. \\nI followed my teammates to " +
                "one end of the field. \"Lubar!\" Mr. Growler shouted at me.', paragraph_text_unformatted = 'I\\'m not" +
                " even sure what grade I was in when I decided to join the after -school football program.\\nSecond " +
                "grade sounds about right.\\nI don\\'t remember the gym teacher\\'s name, either.\\nSo let\\'s just " +
                "call him Mr. Growler.\\nThe first fact about sports that caught my attention as I wandered toward the" +
                " field behind the school was that everyone else seemed to have been born knowing not only the rules " +
                "to the game, but also exactly what to do.\\n\\nI followed my teammates to one end of the field.\\n\"" +
                "Lubar!\" Mr. Growler shouted at me.' WHERE id = '40288103-3df2e55a-013d-f2e5dfdc-0183';");
        assertTrue(builder.isTerminated());
    }

    @Test
    public void hexLiteral() throws Exception {
        builder.addLine("INSERT INTO Tablename (id) VALUES (x'5B1A5933964C4AA59F3013D3B3F3414D');");
        assertTrue(builder.isTerminated());
    }

    @Test
    public void binaryLiteral() throws Exception {
        builder.addLine("INSERT INTO Tablename (id) VALUES (b'10110011');");
        assertTrue(builder.isTerminated());
    }

    @Test
    public void trailingEscapedBackSlash() throws Exception {
        builder.addLine("INSERT INTO Tablename (id) VALUES ('\\\\');");
        assertTrue(builder.isTerminated());
    }

    @Test
    public void singleQuotesInDoubleQuotedStringLiteral() throws Exception {
        builder.addLine("INSERT INTO Tablename (id) VALUES (\",'a'a\"),(\" 'a'a\"),(\" ''\"),(\"' ''\"),(\" ' \"),(\" '\"),(\"' \");");
        assertTrue(builder.isTerminated());
    }

    @Test
    public void doubleQuotesInSingleQuotedStringLiteral() throws Exception {
        builder.addLine("INSERT INTO Tablename (id) VALUES (',\"a\"a'),(' \"a\"a'),(' \"\"'),('\" \"\"'),(' \" '),(' \"'),('\" ');");
        assertTrue(builder.isTerminated());
    }
}
