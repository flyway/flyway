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
package org.flywaydb.core.internal.dbsupport;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import org.flywaydb.core.api.FlywayException;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DryRunTest {

    @Mock
    private DbSupport dbSupport;
    @Mock(answer = Answers.RETURNS_MOCKS)
    private Connection connection1;
    @Mock(answer = Answers.RETURNS_MOCKS)
    private Connection connection2;

    @Before
    public void setup() throws Exception {
        when(dbSupport.supportsDdlTransactions()).thenReturn(true);
        doThrow(new SQLException()).when(connection1).setTransactionIsolation(eq(Connection.TRANSACTION_SERIALIZABLE));
    }

    @Test(expected = NullPointerException.class)
    public void invalidParameters() throws Exception {
        new DryRun(null);
    }

    @Test
    public void noConnectionsDoesNothingOk() throws Exception {
        DryRun dryRun = new DryRun(dbSupport);
        dryRun.rollback();
    }

    @Test
    public void multipleConnectionsRollback() throws Exception {
        DryRun dryRun = new DryRun(dbSupport, connection1, connection2);
        verify(connection1, times(1)).setSavepoint(anyString());
        verify(connection2, times(1)).setSavepoint(anyString());
        dryRun.rollback();
        verify(connection1, times(1)).rollback(any(Savepoint.class));
        verify(connection2, times(1)).rollback(any(Savepoint.class));
    }

    @Test(expected = FlywayException.class)
    public void doesNotSupportDdlTransactions() throws Exception {
        when(dbSupport.supportsDdlTransactions()).thenReturn(false);
        new DryRun(dbSupport, connection1, connection2);
    }

    @Test(expected = FlywayException.class)
    public void doesNotSupportTransactionIsolationLevel() throws Exception {
        new DryRun(Connection.TRANSACTION_SERIALIZABLE, dbSupport, connection1, connection2);
    }

    @Test
    public void errorOnRollbackContinuesForOtherConnections() throws Exception {
        doThrow(new SQLException()).when(connection1).rollback(any(Savepoint.class));
        DryRun dryRun = new DryRun(dbSupport, connection1, connection2);
        verify(connection1, times(1)).setSavepoint(anyString());
        FlywayException ex = null;
        try{
            dryRun.rollback();
        } catch(FlywayException e){
            ex = e;
        }
        verify(connection1, times(1)).rollback(any(Savepoint.class));
        verify(connection2, times(1)).rollback(any(Savepoint.class));
        assertNotNull("Expected a FlywayException to be thrown", ex);
    }
}
