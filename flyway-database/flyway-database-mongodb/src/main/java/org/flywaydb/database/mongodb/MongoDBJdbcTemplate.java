/*-
 * ========================LICENSE_START=================================
 * flyway-database-mongodb
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
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
package org.flywaydb.database.mongodb;

import com.dbschema.mongo.MongoConnection;
import com.dbschema.mongo.MongoPreparedStatement;
import com.dbschema.mongo.resultSet.ListResultSet;
import lombok.NonNull;
import org.bson.Document;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.database.DatabaseTypeRegister;
import org.flywaydb.core.internal.jdbc.JdbcNullTypes;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.JdbcUtils;
import org.flywaydb.core.internal.jdbc.RowMapper;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MongoDBJdbcTemplate extends JdbcTemplate {

    public MongoDBJdbcTemplate(Connection connection, DatabaseType databaseType) {
        super(connection, databaseType);
    }



    @Override
    protected PreparedStatement prepareStatement(String sql, Object[] params) throws SQLException {
        Object[] params2 = new Object[params.length];

        String replaceMe = sql.replaceAll("\\?", "%s");
        for (int i = 0; i < params.length; i++) {
            if (params[i] instanceof Integer integerValue) {
                params2[i] = integerValue;
            } else if (params[i] instanceof Boolean booleanValue) {
                params2[i] =  booleanValue;
            } else if (params[i] instanceof String stringValue) {
                params2[i] = "'"+stringValue+"'";
            } else if (params[i] == null || params[i] == JdbcNullTypes.StringNull || params[i] == JdbcNullTypes.IntegerNull || params[i] == JdbcNullTypes.BooleanNull) {
                params2[i] = null;
            } else {
                throw new FlywayException("Unhandled object of type '" + params[i].getClass().getName() + "'. " +
                                                  "Please contact support or leave an issue on GitHub.");
            }
        }
        String statementString = String.format(replaceMe, params2);

        return connection.prepareStatement(statementString);
    }

    @Override
    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        List<T> results;
        try {
            statement = prepareStatement(sql, params);
            resultSet = statement.executeQuery();

            results = new ArrayList<>();
            while (resultSet.next()) {
                ResultSet rs = convertMongoResultset(resultSet);
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
            }
        } catch (Exception eeeee) {
            throw new FlywayException("Error executing statement " + sql, eeeee);
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(statement);
        }

        return results;
    }

    public String queryForString(String query, String... params) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String result;
        try {
            statement = prepareStatement(query, params);
            resultSet = statement.executeQuery();
            result = null;
            if (resultSet.next()) {
                try {
                    ResultSet rs = convertMongoResultset(resultSet);
                    while (rs.next()) {
                        result = rs.getString(1);
                    }
                } catch (Exception e) {
                    result = resultSet.getString(1);
                }
            }
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(statement);
        }

        return result;
    }

    public void update(String sql, Object... params) throws SQLException {
        if(sql.contains("updateOne")) {
            List<Object> tmp = new ArrayList<>(List.of(params));
            tmp.add(0, tmp.get(tmp.size() -1 ));
            tmp.remove(tmp.size() - 1);
            params = tmp.toArray();
        }

        PreparedStatement statement = null;
        try {
            statement = prepareStatement(sql, params);
            statement.executeUpdate();
        } finally {
            JdbcUtils.closeStatement(statement);
        }
    }

    @NonNull
    private static ResultSet convertMongoResultset(ResultSet resultSet) throws SQLException {
        Document doc = (Document)resultSet.getObject(1);
        doc.remove("_id");
        String[] columnNames = doc.keySet().stream().toList().toArray(String[]::new);
        List<Object[]> data = new ArrayList<>();
        Collection<Object> vals = doc.values();
        Object[] valsArray = vals.toArray();
        data.add(valsArray);
        return new ListResultSet(data, columnNames);
    }
}
