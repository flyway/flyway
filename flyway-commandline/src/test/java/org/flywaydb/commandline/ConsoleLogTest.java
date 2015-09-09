package org.flywaydb.commandline;

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

    @Test
    public void errorAndStackTraceGoesToSystemErr() throws Exception {
        final Exception mockedException = mock(Exception.class);
        new ConsoleLog(ConsoleLog.Level.INFO).error("F00b4r", mockedException);
        verify(mockedErr).println("ERROR: F00b4r");
        verify(mockedException).printStackTrace(mockedErr);
    }
}