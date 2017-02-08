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
package org.flywaydb.core.internal.dbsupport.sqlserver.large;

import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.jdbc.core.JdbcTemplate;

@SuppressWarnings("UnusedDeclaration")
public class V3_1_3__Insert_tipos_de_eventos_padroes implements SpringJdbcMigration {

    public void migrate(JdbcTemplate jdbc) throws Exception {
        jdbc.update("INSERT INTO EVENTO_TIPO VALUES ('Casamento')");
    }

}
