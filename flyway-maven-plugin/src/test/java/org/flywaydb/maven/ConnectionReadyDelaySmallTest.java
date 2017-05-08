/**
 * Copyright 2010-2016 Boxfuse GmbH
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.maven;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.logging.Log;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Random;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionReadyDelaySmallTest {

    @Spy
    private DummyChecker checker;
    @Mock
    private Log log;

    @Test
    public void shouldExecuteImmediatelyWhenOptionNotSpecified() throws Exception {
        final ConnectionReadyDelay delay = new ConnectionReadyDelay(log, null);
        delay.waitTillConnectionReady(checker);
        verify(checker).check();
    }

    @Test(expected = FlywayException.class)
    public void shouldReThrowWhenMaxRetriesReached() throws Exception {
        final ConnectionReadyDelay delay = spy(new ConnectionReadyDelay(log, 15));
        doThrow(FlywayException.class).when(checker).check();
        doNothing().when(delay).sleep();

        delay.waitTillConnectionReady(checker);

        verify(checker, times(15)).check();
    }

    @Test
    public void shouldExecuteTillDbConnectionUp() throws Exception {
        Throwable[] errors = new Throwable[5];
        Arrays.fill(errors, new FlywayException());

        final ConnectionReadyDelay delay = spy(new ConnectionReadyDelay(log, 15));

        when(checker.doCheck()).thenThrow(errors).thenReturn("OK");
        doNothing().when(delay).sleep();

        delay.waitTillConnectionReady(checker);

        verify(checker, times(6)).check();
    }

    private static class DummyChecker implements ConnectionReadyDelay.ConnectionChecker {

        @Override
        public void check() throws Exception {
            doCheck();
        }

        Object doCheck() {
            return new Random();
        }
    }
}