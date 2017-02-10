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
package org.flywaydb.core.internal.util.logging.console;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.PrintStream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ConsoleLogTest {
    private PrintStream oldErr;

    @Mock
    private PrintStream mockedErr;

    @Before
    public void setUp() throws Exception {
        oldErr = System.err;
        System.setErr(mockedErr);
    }

    @After
    public void tearDown() throws Exception {
        System.setErr(oldErr);
    }

    @Test
    public void errorGoesToSystemErr() throws Exception {
        new ConsoleLog(ConsoleLog.Level.INFO).error("F00b4r");
        verify(mockedErr).println("ERROR: F00b4r");
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    @Test
    public void errorAndStackTraceGoesToSystemErr() throws Exception {
        final Exception mockedException = mock(Exception.class);
        new ConsoleLog(ConsoleLog.Level.INFO).error("F00b4r", mockedException);
        verify(mockedErr).println("ERROR: F00b4r");
        verify(mockedException).printStackTrace(mockedErr);
    }
}