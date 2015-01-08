/**
 * Copyright 2010-2015 Axel Fontaine
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
    public interface InstallableDB extends DB {}
    public interface EmbeddedDB extends DB {}
    public interface OpenSourceDB extends InstallableDB {}
    public interface CommercialDB extends InstallableDB {}
    public interface ContributorSupportedDB extends CommercialDB {}

    public interface Derby extends EmbeddedDB {}
    public interface H2 extends EmbeddedDB {}
    public interface HSQL extends EmbeddedDB {}
    public interface SQLite extends EmbeddedDB {}
    public interface Phoenix extends EmbeddedDB {}

    public interface MySQL extends OpenSourceDB {}
    public interface MariaDB extends OpenSourceDB {}
    public interface PostgreSQL extends OpenSourceDB {}

    public interface DB2 extends CommercialDB {}
    public interface Oracle extends CommercialDB {}
    public interface SQLServer extends CommercialDB {}
    public interface GoogleCloudSQL extends CommercialDB {}

    public interface DB2zOS extends ContributorSupportedDB {}
    public interface Vertica extends ContributorSupportedDB {}
    public interface Redshift extends ContributorSupportedDB {}
    public interface SolidDB extends ContributorSupportedDB {}

}
