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
