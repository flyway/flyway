package org.flywaydb.core.internal.batch;

import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.internal.dbsupport.DbSupport;

/**
 * Created on 07/08/16.
 *
 * @author Reda.Housni-Alaoui
 */
public class SingleTransactionBatchService implements MigrationBatchService {
    @Override
    public boolean isLastOfBatch(DbSupport dbSupport, MigrationInfo migrationInfo) {
        return false;
    }
}
