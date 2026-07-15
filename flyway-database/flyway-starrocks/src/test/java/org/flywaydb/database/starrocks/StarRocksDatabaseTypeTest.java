/*-
 * ========================LICENSE_START=================================
 * flyway-starrocks
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.database.starrocks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link StarRocksDatabaseType}.
 */
class StarRocksDatabaseTypeTest {

    private StarRocksDatabaseType databaseType;

    @BeforeEach
    void setUp() {
        databaseType = new StarRocksDatabaseType();
    }

    @Test
    @DisplayName("getName should return 'StarRocks'")
    void getName() {
        assertEquals("StarRocks", databaseType.getName());
    }

    @Test
    @DisplayName("getPriority should return 1 (higher than MySQL's 0)")
    void getPriority() {
        assertEquals(1, databaseType.getPriority());
    }

    @Test
    @DisplayName("getNullType should return Types.VARCHAR (12)")
    void getNullType() {
        assertEquals(java.sql.Types.VARCHAR, databaseType.getNullType());
    }

    /* ========== handlesJDBCUrl tests ========== */

    @Test
    @DisplayName("handlesJDBCUrl: standard jdbc:mysql: URL returns true")
    void jdbcUrlStandard() {
        assertTrue(databaseType.handlesJDBCUrl("jdbc:mysql://localhost:9030/starrocks_db"));
    }

    @Test
    @DisplayName("handlesJDBCUrl: jdbc:p6spy:mysql: URL returns true")
    void jdbcUrlP6spy() {
        assertTrue(databaseType.handlesJDBCUrl("jdbc:p6spy:mysql://localhost:9030/starrocks_db"));
    }

    @Test
    @DisplayName("handlesJDBCUrl: non-MySQL URL returns false")
    void jdbcUrlNonMySql() {
        assertFalse(databaseType.handlesJDBCUrl("jdbc:postgresql://localhost:5432/db"));
        assertFalse(databaseType.handlesJDBCUrl("jdbc:h2:mem:test"));
        assertFalse(databaseType.handlesJDBCUrl("jdbc:oracle:thin:@localhost:1521:db"));
    }

    /* ========== handlesDatabaseProductNameAndVersion tests ========== */

    @Test
    @DisplayName("handlesDatabaseProductNameAndVersion: non-MySQL product returns false")
    void productNameNonMySql() {
        assertFalse(databaseType.handlesDatabaseProductNameAndVersion(
            "PostgreSQL", "15.0", null));
    }

    @Test
    @DisplayName("handlesDatabaseProductNameAndVersion: SELECT version() contains 'starrocks'")
    void productNameVersionContainsStarRocks() throws Exception {
        Connection connection = mock(Connection.class);
        java.sql.PreparedStatement pstmt = mock(java.sql.PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        // getSelectVersionOutput uses prepareStatement("SELECT version()") internally
        when(connection.prepareStatement("SELECT version()")).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(1)).thenReturn("5.1.0-starrocks-3.0");

        boolean result = databaseType.handlesDatabaseProductNameAndVersion(
            "MySQL", "5.1.0", connection);

        assertTrue(result);
    }

    @Test
    @DisplayName("handlesDatabaseProductNameAndVersion: falls back to @@language check")
    void productNameFallbackToLanguage() throws Exception {
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        java.sql.PreparedStatement pstmt = mock(java.sql.PreparedStatement.class);
        ResultSet versionResult = mock(ResultSet.class);
        ResultSet languageResult = mock(ResultSet.class);

        // First: getSelectVersionOutput uses prepareStatement
        when(connection.prepareStatement("SELECT version()")).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(versionResult);
        when(versionResult.next()).thenReturn(true);
        when(versionResult.getString(1)).thenReturn("5.1.0");

        // Second: @@language check via createStatement
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery("SELECT @@language")).thenReturn(languageResult);
        when(languageResult.next()).thenReturn(true);
        when(languageResult.getString(1)).thenReturn("/starrocks/share/english/");

        boolean result = databaseType.handlesDatabaseProductNameAndVersion(
            "MySQL", "5.1.0", connection);

        assertTrue(result);
        // verify: prepareStatement used for version(), createStatement used once for @@language
        verify(connection).prepareStatement("SELECT version()");
        verify(connection, times(1)).createStatement();
        verify(statement, times(1)).executeQuery("SELECT @@language");
    }

    @Test
    @DisplayName("handlesDatabaseProductNameAndVersion: neither check matches returns false")
    void productNameNeitherMatches() throws Exception {
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        java.sql.PreparedStatement pstmt = mock(java.sql.PreparedStatement.class);
        ResultSet versionResult = mock(ResultSet.class);
        ResultSet languageResult = mock(ResultSet.class);

        when(connection.prepareStatement("SELECT version()")).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(versionResult);
        when(versionResult.next()).thenReturn(true);
        when(versionResult.getString(1)).thenReturn("5.1.0");

        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery("SELECT @@language")).thenReturn(languageResult);
        when(languageResult.next()).thenReturn(true);
        when(languageResult.getString(1)).thenReturn("/mysql/english/");

        boolean result = databaseType.handlesDatabaseProductNameAndVersion(
            "MySQL", "5.1.0", connection);

        assertFalse(result);
    }

    @Test
    @DisplayName("handlesDatabaseProductNameAndVersion: returns false on SQL exception")
    void productNameException() throws Exception {
        Connection connection = mock(Connection.class);
        when(connection.prepareStatement("SELECT version()")).thenThrow(new SQLException("Connection lost"));

        boolean result = databaseType.handlesDatabaseProductNameAndVersion(
            "MySQL", "5.1.0", connection);

        assertFalse(result);
    }

    /* ========== getDriverClass tests ========== */

    @Test
    @DisplayName("getDriverClass: standard URL returns MySQL driver")
    void driverClassStandard() {
        assertEquals("com.mysql.cj.jdbc.Driver",
            databaseType.getDriverClass("jdbc:mysql://localhost:9030/db", null));
    }

    @Test
    @DisplayName("getDriverClass: p6spy URL returns P6Spy driver")
    void driverClassP6spy() {
        assertEquals("com.p6spy.engine.spy.P6SpyDriver",
            databaseType.getDriverClass("jdbc:p6spy:mysql://localhost:9030/db", null));
    }
}
