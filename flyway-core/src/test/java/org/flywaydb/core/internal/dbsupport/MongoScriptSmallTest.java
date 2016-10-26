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

import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test for Mongo JavaScript.
 */
public class MongoScriptSmallTest {
    /**
     * Class under test.
     */
    private MongoScript mongoScript = new MongoScript("", "mongoScriptTest");

    /**
     * Input lines.
     */
    private List<String> lines = new ArrayList<String>();

    @Test
    public void stripMongoCommentsNoComment() {
        lines.add("db.runCommand({count: 'sample'});");
        List<MongoStatement> mongoStatements = mongoScript.linesToStatements(lines);
        assertEquals("{count: 'sample'}", mongoStatements.get(0).getJson());
    }

    @Test
    public void stripMongoCommentsSingleLineComment() {
        lines.add("//single line comment");
        List<MongoStatement> mongoStatements = mongoScript.linesToStatements(lines);
        assertEquals(0, mongoStatements.size());
    }

    @Test
    public void stripMongoCommentsMultiLineCommentSingleLine() {
        lines.add("/*comment line*/");
        lines.add("db.runCommand({find: 'sample'});");
        List<MongoStatement> mongoStatements = mongoScript.linesToStatements(lines);
        assertEquals("{find: 'sample'}", mongoStatements.get(0).getJson());
    }

    @Test
    public void stripMongoCommentsMultiLineCommentMultipleLines() {
        lines.add("/*comment line");
        lines.add("more comment text*/");
        List<MongoStatement> mongoStatements = mongoScript.linesToStatements(lines);
        assertEquals(0, mongoStatements.size());
    }

    @Test
    public void linesToStatements() {
        lines.add("use(sample);");
        lines.add("db.runCommand({");
        lines.add("insert: 'sample',");
        lines.add("documents: [{item: 'pencil'}]");
        lines.add("});");

        List<MongoStatement> mongoStatements = mongoScript.linesToStatements(lines);
        assertNotNull(mongoStatements);
        assertEquals(1, mongoStatements.size());

        MongoStatement mongoStatement = mongoStatements.get(0);
        assertEquals(2, mongoStatement.getLineNumber());
        assertEquals("{insert: 'sample',documents: [{item: 'pencil'}]}", mongoStatement.getJson());
        assertEquals("sample", mongoStatement.getDbName());
    }

    @Test
    public void linesToStatementsJointMultilineSingleLineComment() {
        lines.add("/**");
        lines.add("//count something");
        lines.add("db.runCommand({count: 'sample'});");
        lines.add("**/// Comment on the same line attached to the multiline closing");
        lines.add("//these statements are not imported because end of multiline is not detected");
        lines.add("db.runCommand({find: 'sample'});");
        lines.add("/**");
        lines.add("//insert something");
        lines.add("db.runCommand({insert: 'sample',documents: [{item: 'pencil'}]});");
        lines.add("**/");
        lines.add("//these statements are imported the above multiline is detected");
        lines.add("db.runCommand({insert: 'sample',documents: [{item: 'pen'}]});");

        List<MongoStatement> mongoStatements = mongoScript.linesToStatements(lines);
        assertEquals(2, mongoStatements.size());

        assertEquals(6, mongoStatements.get(0).getLineNumber());
        assertEquals(12, mongoStatements.get(1).getLineNumber());
    }

    @Test(timeout = 3000)
    public void linesToStatementsSuperLongStatement() {
        lines.add("db.runCommand({insert: sample, documents: [");
        for (int i = 0; i < 10000; i++) {
            lines.add("{item: 'many'},");
        }
        lines.add("{item: 'many'}]});");

        List<MongoStatement> mongoStatements = mongoScript.linesToStatements(lines);
        assertNotNull(mongoStatements);
        assertEquals(1, mongoStatements.size());

        MongoStatement mongoStatement = mongoStatements.get(0);
        assertEquals(1, mongoStatement.getLineNumber());
    }

    @Test
    public void linesToStatementsMultilineCommentsWithSlashes() {
        lines.add("/*//////////////////////");
        lines.add("Some comments");
        lines.add("*////////////////////////");
        lines.add("db.runCommand({find: 'sample'});");

        List<MongoStatement> mongoStatements = mongoScript.linesToStatements(lines);
        assertNotNull(mongoStatements);
        assertEquals(1, mongoStatements.size());

        MongoStatement mongoStatement = mongoStatements.get(0);
        assertEquals(4, mongoStatement.getLineNumber());
    }

    @Test
    public void linesToStatementsTrimEmptyLinesInsideStatement() {
        lines.add("db.runCommand({find: 'this_");
        lines.add("");
        lines.add("is_");
        lines.add("");
        lines.add("a_collectionName'");
        lines.add("});");

        List<MongoStatement> mongoStatements = mongoScript.linesToStatements(lines);
        assertNotNull(mongoStatements);
        assertEquals(1, mongoStatements.size());

        MongoStatement mongoStatement = mongoStatements.get(0);
        assertEquals(1, mongoStatement.getLineNumber());
        assertEquals("{find: 'this_is_a_collectionName'}", mongoStatement.getJson());
    }

    @Test
    public void linesToStatementsSkipEmptyLinesBetweenStatements() {
        lines.add("db.runCommand({count: 'sampleA'});");
        lines.add("");
        lines.add("db.runCommand({count: 'sampleB'});");
        lines.add("");
        lines.add("");
        lines.add("db.runCommand({count: 'sampleC'});");

        List<MongoStatement> mongoStatements = mongoScript.linesToStatements(lines);
        assertNotNull(mongoStatements);
        assertEquals(3, mongoStatements.size());

        assertEquals(1, mongoStatements.get(0).getLineNumber());
        assertEquals("{count: 'sampleA'}", mongoStatements.get(0).getJson());

        assertEquals(3, mongoStatements.get(1).getLineNumber());
        assertEquals("{count: 'sampleB'}", mongoStatements.get(1).getJson());

        assertEquals(6, mongoStatements.get(2).getLineNumber());
        assertEquals("{count: 'sampleC'}", mongoStatements.get(2).getJson());
    }

    @Test
    public void parsePreserveTrailingCommentsInsideStatement() {
        String source = "db.runCommand({find: 'sample'/* comment */});";

        List<MongoStatement> mongoStatements = mongoScript.parse(source);
        assertNotNull(mongoStatements);
        assertEquals(1, mongoStatements.size());

        MongoStatement mongoStatement = mongoStatements.get(0);
        assertEquals(1, mongoStatement.getLineNumber());
        assertEquals("{find: 'sample'/* comment */}", mongoStatement.getJson());
    }

    @Ignore("Currently broken")
    @Test
    public void parseWithTrailingComment() {
        String source = "db.runCommand({find: 'sample'}); // trailing comment\r\n" +
                "db.runCommand({find: 'sample'});";
        List<MongoStatement> mongoStatements = mongoScript.parse(source);
        assertEquals(2, mongoStatements.size());
    }
}
