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
package org.flywaydb.core;

/**
 * Wraps all interfaces used to express that specific test need DB to run
 * It can be used also to exclude some test from running if for example some DB is missing
 */
public class DbCategory {
    public interface DB {}
    public interface EmbeddedDB extends DB {}
    public interface DockerDB extends DB {}
    public interface SpecialDB extends DB {}

    public interface Derby extends EmbeddedDB {}
    public interface H2 extends EmbeddedDB {}
    public interface HSQL extends EmbeddedDB {}
    public interface SQLite extends EmbeddedDB {}

    public interface MySQL extends DockerDB {}
    public interface MariaDB extends DockerDB {}
    public interface PostgreSQL extends DockerDB {}
    public interface CockroachDB extends DockerDB {}
    public interface Oracle extends DockerDB {}
    public interface DB2 extends DockerDB {}
    public interface SQLServer extends DockerDB {}
    public interface SAPHANA extends DockerDB {}
    public interface SybaseASE extends DockerDB {}

    public interface Redshift extends SpecialDB {}

    // Other databases support maintained by the third party contributors
//    public interface EnterpriseDB extends ContributorSupportedDB {}
//    public interface Phoenix extends ContributorSupportedDB {}
//    public interface SolidDB extends ContributorSupportedDB {}
}
