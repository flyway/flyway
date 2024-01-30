package org.flywaydb.core.api;

import org.flywaydb.core.api.output.InfoResult;

interface InfoOutputProvider {
    InfoResult getInfoResult();

    InfoResult getInfoResult(MigrationFilter filter);
}