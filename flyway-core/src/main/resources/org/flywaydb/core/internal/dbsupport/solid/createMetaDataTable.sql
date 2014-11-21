--
-- Copyright 2010-2014 Axel Fontaine
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--         http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> #885: Clean up source headers
-- SolidDB support developed 2014 by Sabine Gallus & Michael Forstner
-- Media-Saturn IT Services GmbH
-- Wankelstr. 5
-- 85046 Ingolstadt, Germany
--
<<<<<<< HEAD
=======
>>>>>>> Initial commit for SolidDB support (#885)
=======
>>>>>>> #885: Clean up source headers

CREATE TABLE ${schema}.${table} (
    version_rank INT NOT NULL,
    installed_rank INT NOT NULL,
    version VARCHAR(50) NOT NULL,
    description VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INT,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP,
    execution_time INT NOT NULL,
    success SMALLINT NOT NULL,
    PRIMARY KEY(version)
) STORE DISK;

<<<<<<< HEAD
<<<<<<< HEAD
"CREATE TRIGGER ${schema}.${table}_create ON ${schema}.${table}
=======
"CREATE TRIGGER ${table}_create ON ${schema}.${table}
>>>>>>> Initial commit for SolidDB support (#885)
=======
"CREATE TRIGGER ${schema}.${table}_create ON ${schema}.${table}
>>>>>>> #885: Minor fixes regarding SolidDB support including green ConcurrentMigrationTest
    BEFORE INSERT REFERENCING NEW installed_on AS new_installed_on
    BEGIN
    SET new_installed_on = NOW();
    END";

CREATE INDEX ${table}_vr_idx ON ${schema}.${table} (version_rank);
CREATE INDEX ${table}_ir_idx ON ${schema}.${table} (installed_rank);
CREATE INDEX ${table}_s_idx ON ${schema}.${table} (success);

<<<<<<< HEAD
<<<<<<< HEAD
COMMIT WORK;

=======
>>>>>>> Initial commit for SolidDB support (#885)
=======
COMMIT WORK;

>>>>>>> #885: Minor fixes regarding SolidDB support including green ConcurrentMigrationTest
