/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.core.internal.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExceptionUtilsSmallTest {
    @Test
    public void rootCause() {
        Exception cause = new Exception();
        Exception e = new Exception(cause);
        assertEquals(cause, ExceptionUtils.getRootCause(e));
    }

    @Test
    public void rootCauseIsExceptionItself() {
        Exception e = new Exception();
        assertEquals(e, ExceptionUtils.getRootCause(e));
    }
}
