/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.util;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Test for FeatureDetector.
 */
public class FeatureDetectorSmallTest {
    @Test
    public void isSpringJdbcAvailable() {
        assertTrue(new FeatureDetector(Thread.currentThread().getContextClassLoader()).isSpringJdbcAvailable());
    }
}
