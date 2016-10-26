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
package org.flywaydb.core.api.callback;

import org.flywaydb.core.api.configuration.ConfigurationAware;
import org.flywaydb.core.api.configuration.FlywayConfiguration;

import java.sql.Connection;

/**
 * Abstract implementation of the generic Callback interface for use with JDBC/SQL databases.
 * Classes that extend this gain access to {@link FlywayConfiguration} as a field. Unlike the
 * BaseFlywayCallback class, classes that extend this must implement all methods inherited from
 * the FlywayCallback interface.
 * 
 * @author Brennan Collins
 */
public abstract class SQLFlywayCallback implements Callback<Connection>, ConfigurationAware {

	protected FlywayConfiguration flywayConfiguration;
	
	public SQLFlywayCallback(FlywayConfiguration flywayConfiguration) {
		this.flywayConfiguration = flywayConfiguration;
	}

	@Override
	public void setFlywayConfiguration(FlywayConfiguration flywayConfiguration) {
		this.flywayConfiguration = flywayConfiguration;
	}
}
