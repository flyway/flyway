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
package org.flywaydb.core.internal.dbsupport.db2;

import org.flywaydb.core.internal.dbsupport.Delimiter;
import org.flywaydb.core.internal.util.StringUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class DB2SqlStatementBuilderSmallTest {
    @Test
    public void isBegin() throws Exception {
        assertTrue(DB2SqlStatementBuilder.isBegin("BEGIN"));
        assertTrue(DB2SqlStatementBuilder.isBegin("LABEL:BEGIN"));
        assertTrue(DB2SqlStatementBuilder.isBegin("LABEL: BEGIN"));
        assertTrue(DB2SqlStatementBuilder.isBegin("LABEL :BEGIN"));
        assertTrue(DB2SqlStatementBuilder.isBegin("LABEL : BEGIN"));
    }

    @Test
    public void isEnd() throws Exception {
        assertTrue(DB2SqlStatementBuilder.isEnd("END", null, new Delimiter(";", false)));
        assertTrue(DB2SqlStatementBuilder.isEnd("END;", null, new Delimiter(";", false)));
        assertTrue(DB2SqlStatementBuilder.isEnd("END", "LABEL", new Delimiter(";", false)));
        assertTrue(DB2SqlStatementBuilder.isEnd("END@", "LABEL", new Delimiter("@", false)));
        assertTrue(DB2SqlStatementBuilder.isEnd("END LABEL", "LABEL", new Delimiter(";", false)));
        assertFalse(DB2SqlStatementBuilder.isEnd("END FOR", null, new Delimiter(";", false)));
        assertFalse(DB2SqlStatementBuilder.isEnd("END FOR", "LABEL", new Delimiter(";", false)));
        assertFalse(DB2SqlStatementBuilder.isEnd("END IF", null, new Delimiter(";", false)));
        assertFalse(DB2SqlStatementBuilder.isEnd("END IF", "LABEL", new Delimiter(";", false)));
    }

    @Test
    public void extractLabel() throws Exception {
        assertNull(DB2SqlStatementBuilder.extractLabel("BEGIN"));
        assertEquals("LABEL", DB2SqlStatementBuilder.extractLabel("LABEL:BEGIN"));
        assertEquals("LABEL", DB2SqlStatementBuilder.extractLabel("LABEL: BEGIN"));
        assertEquals("LABEL", DB2SqlStatementBuilder.extractLabel("LABEL :BEGIN"));
        assertEquals("LABEL", DB2SqlStatementBuilder.extractLabel("LABEL : BEGIN"));
    }

    @Test
    public void isTerminated() {
        assertTerminated("CREATE OR REPLACE PROCEDURE IP_ROLLUP(\n" +
                "  IN iODLID INTEGER,\n" +
                "  IN iNDLID INTEGER,\n" +
                "  IN iOID INTEGER,\n" +
                "  IN iTYPE VARCHAR(5),\n" +
                "  IN iACTION VARCHAR(6)\n" +
                "  )\n" +
                "  LANGUAGE SQL\n" +
                "SPECIFIC SP_IP_ROLLUP\n" +
                "MAIN : BEGIN\n" +
                "\n" +
                "\n" +
                "END\n" +
                "@", "@");
    }

    @Test
    public void isTerminatedLabel() {
        assertTerminated("CREATE OR REPLACE PROCEDURE CUST_INSERT_IP_GL(\n" +
                "  IN iORDER_INTERLINER_ID INTEGER,\n" +
                "  IN iACCT_CODE VARCHAR(50),\n" +
                "  IN iACCT_TYPE CHAR(3),\n" +
                "  IN iAMOUNT DOUBLE,\n" +
                "  IN iSOURCE_TYPE VARCHAR(20),\n" +
                "  OUT oLEAVE_MAIN VARCHAR(5)\n" +
                "  )\n" +
                "  LANGUAGE SQL\n" +
                "SPECIFIC SP_CUST_INSERT_IP_GL\n" +
                "MAIN : BEGIN\n" +
                "  SET oLEAVE_MAIN = 'False';\n" +
                "END MAIN\n" +
                "@", "@");
    }

    @Test
    public void isTerminatedNested() {
        assertTerminated("CREATE PROCEDURE dummy_proc ()\n" +
                "LANGUAGE SQL\n" +
                "BEGIN\n" +
                "    declare var1 TIMESTAMP;\n" +
                "    \n" +
                "    SELECT CURRENT_TIMESTAMP INTO var1 FROM SYSIBM.DUAL;\n" +
                "    \n" +
                "    BEGIN\n" +
                "        declare var2 DATE;\n" +
                "        SELECT CURRENT_DATE INTO var2 FROM SYSIBM.DUAL;\n" +
                "    END;\n" +
                "END;", ";");
    }

    @Test
    public void isTerminatedForIf() {
        assertTerminated("CREATE OR REPLACE PROCEDURE TE_DRIVERPAY_ADJUST(\n" +
                "  IN iTRIP_NUMBER INTEGER\n" +
                "  )\n" +
                "  LANGUAGE SQL\n" +
                "SPECIFIC SP_TE_DRIVERPAY_ADJUST\n" +
                "MAIN:BEGIN\n" +
                "  DECLARE vREBILL_DLID INTEGER;\n" +
                "  DECLARE vDLID INTEGER;\n" +
                "  DECLARE NEW_PAY_ID INTEGER;\n" +
                "  DECLARE GEN_PAY_ID VARCHAR(128) DEFAULT 'GEN_PAY_ID';\n" +
                "  DECLARE vTOTAL_PAY_AMT DOUBLE;\n" +
                "  DECLARE vSESSION_ID INTEGER;\n" +
                "  DECLARE GEN_DP_ADJUST_ID VARCHAR(128) DEFAULT 'GEN_TE_DP_ADJUST_ID';\n" +
                "\n" +
                "  CALL GEN_ID(GEN_DP_ADJUST_ID, vSESSION_ID);\n" +
                "  FOR C1 AS\n" +
                "    SELECT DETAIL_LINE_ID\n" +
                "    FROM ITRIPTLO\n" +
                "    WHERE TRIP_NUMBER = iTRIP_NUMBER DO\n" +
                "\n" +
                "    SET vREBILL_DLID = NULL;\n" +
                "    SELECT MAX(DETAIL_LINE_ID)\n" +
                "      INTO vREBILL_DLID\n" +
                "    FROM TLORDER\n" +
                "    WHERE ORIGINAL_DLID = C1.DETAIL_LINE_ID\n" +
                "      AND DOCUMENT_TYPE = 'REBILL';\n" +
                "    IF vREBILL_DLID IS NOT NULL THEN\n" +
                "      INSERT INTO TE_DRIVERPAY_FB(SESSION_ID,DETAIL_LINE_ID)\n" +
                "        VALUES(vSESSION_ID,vREBILL_DLID);\n" +
                "    ELSE\n" +
                "      INSERT INTO TE_DRIVERPAY_FB(SESSION_ID,DETAIL_LINE_ID)\n" +
                "        VALUES(vSESSION_ID,C1.DETAIL_LINE_ID);\n" +
                "    END IF;\n" +
                "  END FOR;\n" +
                "\n" +
                "  UPDATE DRIVERPAY\n" +
                "    SET PMT_STATE = 'U',\n" +
                "        APPROVED_DATE = NULL,\n" +
                "        APPROVED_BY = NULL\n" +
                "  WHERE TRIP_NUMBER = iTRIP_NUMBER\n" +
                "    AND PAY_OVERRIDDEN = 'False'\n" +
                "    AND PMT_STATE IN ('A','H');\n" +
                "\n" +
                "  FOR C_DRIVERPAY AS\n" +
                "    SELECT PAY_ID,TRIP_NUMBER, LEG_SEQ, DRIVER_ID, TRIP_STATUS, TRAILER_1, TRAILER_2,\n" +
                "      TARPED, PAY_DESCRIPTION, LEG_DATE, CONTRACT_ID, RULE_ID, LEG_FROM_ZONE,\n" +
                "      LEG_TO_ZONE, BILL_NUMBER, FB_TOTAL_CHARGES, PERCENTAGE, CURRENCY_CODE,\n" +
                "      LOADED_MILES, LOADED_RATE, EMPTY_MILES, EMPTY_RATE, HOURS, HOURLY_RATE,\n" +
                "      PICK_QTY, PICK_RATE, DROP_QTY, DROP_RATE, LAYOVER_QTY, LAYOVER_RATE,\n" +
                "      OTHER_QTY, OTHER_RATE, TOTAL_PAY_AMT, FB_GR_TOT_CHARGES, RULE_GROUP, PUNIT,\n" +
                "      PAY_OVERRIDDEN,TAXABLE,PAY_CODE,DETAIL_LINE_ID,SC_QTY, SC_PERCENT\n" +
                "    FROM DRIVERPAY\n" +
                "    WHERE TRIP_NUMBER = iTRIP_NUMBER\n" +
                "      AND PMT_STATE NOT IN ('U','A','H')\n" +
                "      AND PAY_OVERRIDDEN = 'False'\n" +
                "      AND COALESCE(CONTRACT_ID,0) <> 0\n" +
                "      AND COALESCE(RULE_ID,0) <> 0\n" +
                "      AND DP_ADJUSTMENT = 'False'\n" +
                "      AND (COALESCE(DETAIL_LINE_ID,0) = 0 OR DETAIL_LINE_ID IN\n" +
                "        (SELECT DETAIL_LINE_ID FROM TE_DRIVERPAY_FB\n" +
                "         WHERE SESSION_ID = vSESSION_ID)) DO\n" +
                "\n" +
                "    CALL GEN_ID(GEN_PAY_ID, NEW_PAY_ID);\n" +
                "    INSERT INTO DRIVERPAY(PAY_ID, TRIP_NUMBER, LEG_ID, LEG_SEQ, DRIVER_ID,\n" +
                "      TRIP_STATUS, TRAILER_1, TRAILER_2, TARPED, PAY_DESCRIPTION, LEG_DATE,\n" +
                "      PMT_STATE, CONTRACT_ID, RULE_ID, LEG_FROM_ZONE, LEG_TO_ZONE,\n" +
                "      BILL_NUMBER, DETAIL_LINE_ID, FB_TOTAL_CHARGES, PERCENTAGE,\n" +
                "      CURRENCY_CODE, LOADED_MILES, LOADED_RATE, EMPTY_MILES,\n" +
                "      EMPTY_RATE, HOURS, HOURLY_RATE, PICK_QTY, PICK_RATE,\n" +
                "      DROP_QTY, DROP_RATE, LAYOVER_QTY, LAYOVER_RATE, OTHER_QTY,\n" +
                "      OTHER_RATE, TOTAL_PAY_AMT, ENTERED_BY_USER_ID, ENTERED_WHEN,\n" +
                "      FB_GR_TOT_CHARGES, RULE_GROUP, PUNIT,\n" +
                "      PAY_OVERRIDDEN,TAXABLE,PAY_CODE,DP_ADJUSTMENT,ORIG_PAY_ID,SC_QTY,SC_PERCENT\n" +
                "      )\n" +
                "      VALUES(NEW_PAY_ID, C_DRIVERPAY.TRIP_NUMBER, 0, C_DRIVERPAY.LEG_SEQ,\n" +
                "        C_DRIVERPAY.DRIVER_ID, C_DRIVERPAY.TRIP_STATUS, C_DRIVERPAY.TRAILER_1,\n" +
                "        C_DRIVERPAY.TRAILER_2, C_DRIVERPAY.TARPED, C_DRIVERPAY.PAY_DESCRIPTION,\n" +
                "        C_DRIVERPAY.LEG_DATE, 'U', C_DRIVERPAY.CONTRACT_ID, C_DRIVERPAY.RULE_ID,\n" +
                "        C_DRIVERPAY.LEG_FROM_ZONE, C_DRIVERPAY.LEG_TO_ZONE,\n" +
                "        C_DRIVERPAY.BILL_NUMBER, C_DRIVERPAY.DETAIL_LINE_ID,\n" +
                "        C_DRIVERPAY.FB_TOTAL_CHARGES * -1,\n" +
                "        C_DRIVERPAY.PERCENTAGE, C_DRIVERPAY.CURRENCY_CODE,\n" +
                "        C_DRIVERPAY.LOADED_MILES * -1,\n" +
                "        C_DRIVERPAY.LOADED_RATE,\n" +
                "        C_DRIVERPAY.EMPTY_MILES * -1,\n" +
                "        C_DRIVERPAY.EMPTY_RATE,\n" +
                "        C_DRIVERPAY.HOURS * -1,\n" +
                "        C_DRIVERPAY.HOURLY_RATE,\n" +
                "        C_DRIVERPAY.PICK_QTY * -1,\n" +
                "        C_DRIVERPAY.PICK_RATE,\n" +
                "        C_DRIVERPAY.DROP_QTY * -1,\n" +
                "        C_DRIVERPAY.DROP_RATE,\n" +
                "        C_DRIVERPAY.LAYOVER_QTY * -1,\n" +
                "        C_DRIVERPAY.LAYOVER_RATE,\n" +
                "        C_DRIVERPAY.OTHER_QTY * -1,\n" +
                "        C_DRIVERPAY.OTHER_RATE,\n" +
                "        C_DRIVERPAY.TOTAL_PAY_AMT * -1, USER,\n" +
                "        CURRENT TIMESTAMP,\n" +
                "        C_DRIVERPAY.FB_GR_TOT_CHARGES * -1,\n" +
                "        C_DRIVERPAY.RULE_GROUP,\n" +
                "        C_DRIVERPAY.PUNIT,\n" +
                "        'True',\n" +
                "        C_DRIVERPAY.TAXABLE,\n" +
                "        C_DRIVERPAY.PAY_CODE,\n" +
                "        'True',\n" +
                "        C_DRIVERPAY.PAY_ID,\n" +
                "        C_DRIVERPAY.SC_QTY * -1,\n" +
                "        C_DRIVERPAY.SC_PERCENT\n" +
                "        );\n" +
                "\n" +
                "    CALL UPDATE_DRIVERPAY_GL(NEW_PAY_ID);\n" +
                "    UPDATE DRIVERPAY\n" +
                "      SET DP_ADJUSTMENT = 'True'\n" +
                "    WHERE PAY_ID = C_DRIVERPAY.PAY_ID;\n" +
                "    INSERT INTO TE_DRIVERPAY_ID(SESSION_ID,DRIVER_ID,ORIG_PAY_ID,REVERSE_PAY_ID,TOTAL_PAY_AMT)\n" +
                "      VALUES(vSESSION_ID,C_DRIVERPAY.DRIVER_ID,C_DRIVERPAY.PAY_ID,NEW_PAY_ID,C_DRIVERPAY.TOTAL_PAY_AMT);\n" +
                "  END FOR;\n" +
                "\n" +
                "  FOR C1 AS\n" +
                "     SELECT DISTINCT ILD_FB_ID DLID\n" +
                "     FROM ILEGDTL\n" +
                "     WHERE ILD_TRIP_NUMBER = iTRIP_NUMBER AND ILD_RES_TYPE = 'F'\n" +
                "  DO\n" +
                "    SET vDLID = C1.DLID;\n" +
                "    SET vREBILL_DLID = NULL;\n" +
                "    SELECT MAX(DETAIL_LINE_ID)\n" +
                "      INTO vREBILL_DLID\n" +
                "    FROM TLORDER\n" +
                "    WHERE ORIGINAL_DLID = vDLID\n" +
                "      AND DOCUMENT_TYPE = 'REBILL';\n" +
                "\n" +
                "    IF vREBILL_DLID IS NOT NULL THEN\n" +
                "      FOR C_REBILL AS\n" +
                "        SELECT DETAIL_LINE_ID\n" +
                "          FROM TLORDER\n" +
                "        WHERE ORIGINAL_DLID = vDLID\n" +
                "         AND DOCUMENT_TYPE = 'REBILL'DO\n" +
                "        CALL CALC_PAY_FOR_DRIVER_FB (C_REBILL.DETAIL_LINE_ID, USER );\n" +
                "      END FOR;\n" +
                "    ELSE\n" +
                "      CALL CALC_PAY_FOR_DRIVER_FB (vDLID, USER );\n" +
                "    END IF;\n" +
                "  END FOR;\n" +
                "\n" +
                "  FOR C2 AS\n" +
                "     SELECT LS_LEG_ID LEG_ID\n" +
                "     FROM LEGSUM WHERE LS_TRIP_NUMBER = iTRIP_NUMBER\n" +
                "  DO\n" +
                "    CALL CALC_PAY_FOR_DRIVER_LEG(C2.LEG_ID, USER );\n" +
                "  END FOR;\n" +
                "\n" +
                "  CALL CALC_PAY_FOR_DRIVER_LTLMILES(iTRIP_NUMBER, USER);\n" +
                "\n" +
                "  -- Reset adjustment if the total pay amount is identical with previous calculation\n" +
                "  FOR C_DRIVERPAY_SUM AS\n" +
                "    SELECT DRIVER_ID, SUM(TOTAL_PAY_AMT) AMT\n" +
                "    FROM DRIVERPAY\n" +
                "    WHERE TRIP_NUMBER = iTRIP_NUMBER\n" +
                "      AND PMT_STATE = 'U'\n" +
                "      AND PAY_OVERRIDDEN = 'False'\n" +
                "      AND COALESCE(CONTRACT_ID,0) <> 0\n" +
                "      AND COALESCE(RULE_ID,0) <> 0\n" +
                "      AND DP_ADJUSTMENT = 'False'\n" +
                "      AND (COALESCE(DETAIL_LINE_ID,0) = 0 OR DETAIL_LINE_ID IN\n" +
                "        (SELECT DETAIL_LINE_ID FROM TE_DRIVERPAY_FB\n" +
                "         WHERE SESSION_ID = vSESSION_ID))\n" +
                "      GROUP BY DRIVER_ID DO\n" +
                "    SET vTOTAL_PAY_AMT =\n" +
                "      COALESCE((SELECT SUM(TOTAL_PAY_AMT)\n" +
                "         FROM TE_DRIVERPAY_ID\n" +
                "       WHERE DRIVER_ID = C_DRIVERPAY_SUM.DRIVER_ID\n" +
                "         AND SESSION_ID = vSESSION_ID),0);\n" +
                "\n" +
                "    IF ABS(vTOTAL_PAY_AMT - C_DRIVERPAY_SUM.AMT) < 0.01 THEN\n" +
                "      DELETE FROM DRIVERPAY\n" +
                "        WHERE TRIP_NUMBER = iTRIP_NUMBER\n" +
                "        AND DRIVER_ID = C_DRIVERPAY_SUM.DRIVER_ID\n" +
                "        AND PMT_STATE = 'U'\n" +
                "        AND PAY_OVERRIDDEN = 'False'\n" +
                "        AND COALESCE(CONTRACT_ID,0) <> 0\n" +
                "        AND COALESCE(RULE_ID,0) <> 0\n" +
                "        AND DP_ADJUSTMENT = 'False'\n" +
                "        AND (COALESCE(DETAIL_LINE_ID,0) = 0 OR DETAIL_LINE_ID IN\n" +
                "        (SELECT DETAIL_LINE_ID FROM TE_DRIVERPAY_FB\n" +
                "         WHERE SESSION_ID = vSESSION_ID));\n" +
                "      UPDATE DRIVERPAY\n" +
                "        SET DP_ADJUSTMENT = 'False'\n" +
                "        WHERE PAY_ID\n" +
                "          IN (SELECT REVERSE_PAY_ID\n" +
                "            FROM TE_DRIVERPAY_ID\n" +
                "            WHERE DRIVER_ID = C_DRIVERPAY_SUM.DRIVER_ID\n" +
                "              AND SESSION_ID = vSESSION_ID);\n" +
                "      DELETE FROM DRIVERPAY\n" +
                "        WHERE PAY_ID\n" +
                "          IN (SELECT REVERSE_PAY_ID\n" +
                "            FROM TE_DRIVERPAY_ID\n" +
                "            WHERE DRIVER_ID = C_DRIVERPAY_SUM.DRIVER_ID\n" +
                "              AND SESSION_ID = vSESSION_ID);\n" +
                "      UPDATE DRIVERPAY\n" +
                "        SET  DP_ADJUSTMENT = 'False'\n" +
                "        WHERE PAY_ID\n" +
                "          IN (SELECT ORIG_PAY_ID\n" +
                "            FROM TE_DRIVERPAY_ID\n" +
                "            WHERE DRIVER_ID = C_DRIVERPAY_SUM.DRIVER_ID\n" +
                "              AND SESSION_ID = vSESSION_ID);\n" +
                "    END IF;\n" +
                "  END FOR;\n" +
                "\n" +
                "  DELETE FROM TE_DRIVERPAY_FB\n" +
                "    WHERE SESSION_ID = vSESSION_ID;\n" +
                "\n" +
                "  DELETE FROM TE_DRIVERPAY_ID\n" +
                "    WHERE SESSION_ID = vSESSION_ID;\n" +
                "END@", "@");
    }

    private void assertTerminated(String sqlScriptSource, String delimiter) {
        DB2SqlStatementBuilder builder = new DB2SqlStatementBuilder();
        builder.setDelimiter(new Delimiter(delimiter, false));
        String[] lines = StringUtils.tokenizeToStringArray(sqlScriptSource, "\n");
        for (String line : lines) {
            assertFalse("Line should not terminate: " + line, builder.isTerminated());
            builder.addLine(line);
        }

        assertTrue(builder.isTerminated());
    }
}