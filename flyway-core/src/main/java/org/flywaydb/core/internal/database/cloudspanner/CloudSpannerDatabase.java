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
package org.flywaydb.core.internal.database.cloudspanner;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.exception.FlywayDbUpgradeRequiredException;
import org.flywaydb.core.internal.sqlscript.SqlStatementBuilder;
import org.flywaydb.core.internal.sqlscript.SqlStatementBuilderFactory;

import java.sql.Connection;
import java.sql.Types;

/**
 * Google Cloud Spanner database.
 */
public class CloudSpannerDatabase extends Database<CloudSpannerConnection> {
    /**
     * Creates a new instance.
     *
     * @param configuration The Flyway configuration.
     * @param connection    The connection to use.
     * @param originalAutoCommit
     */
    public CloudSpannerDatabase(Configuration configuration, Connection connection, boolean originalAutoCommit



    ) {
        super(configuration, connection, originalAutoCommit



        );
    }

    @Override
    protected CloudSpannerConnection getConnection(Connection connection



    ) {
        return new CloudSpannerConnection(configuration, this, connection, originalAutoCommit, Types.VARCHAR



        );
    }

    @Override
    public final void ensureSupported() {
        String version = majorVersion + "." + minorVersion;

        if (majorVersion < 1) {
            throw new FlywayDbUpgradeRequiredException("Google Cloud Spanner", version, "1.0");
        }
        if (majorVersion > 1) {
        	recommendFlywayUpgrade("Google Cloud Spanner", version);
        }
    }

    public String getDbName() {
        return "cloudspanner";
    }

    public boolean supportsDdlTransactions() {
        return false;
    }

    public String getBooleanTrue() {
        return "true";
    }

    public String getBooleanFalse() {
        return "false";
    }

    @Override
    public String doQuote(String identifier) {
        return "`" + identifier + "`";
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }
    
    public boolean useSingleConnection() {
        return true;
    }

	@Override
	public boolean supportsChangingCurrentSchema() {
		return false;
	}

    private enum CloudSpannerSqlStatementBuilderFactory implements SqlStatementBuilderFactory {
        INSTANCE;

        @Override
        public SqlStatementBuilder createSqlStatementBuilder() {
            return new CloudSpannerSqlStatementBuilder();
        }
    }

	@Override
	protected SqlStatementBuilderFactory getSqlStatementBuilderFactory() {
		return CloudSpannerSqlStatementBuilderFactory.INSTANCE;
	}
}