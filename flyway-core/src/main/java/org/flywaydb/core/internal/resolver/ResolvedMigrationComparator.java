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
package org.flywaydb.core.internal.resolver;

import org.flywaydb.core.api.resolver.ResolvedMigration;

import java.util.Comparator;

/**
* Comparator for ResolvedMigration.
*/
public class ResolvedMigrationComparator implements Comparator<ResolvedMigration> {
    @Override
    public int compare(ResolvedMigration o1, ResolvedMigration o2) {
        if ((o1.getVersion() != null) && o2.getVersion() != null) {
            int v = o1.getVersion().compareTo(o2.getVersion());
            // [pro]
            if (v == 0) {
                if (o1.getType().isUndo() && o2.getType().isUndo()) {
                    return 0;
                }
                if (o1.getType().isUndo()) {
                    return Integer.MAX_VALUE;
                }
                if (o2.getType().isUndo()) {
                    return Integer.MIN_VALUE;
                }
            }
            // [/pro]
            return v;
        }
        if (o1.getVersion() != null) {
            return Integer.MIN_VALUE;
        }
        if (o2.getVersion() != null) {
            return Integer.MAX_VALUE;
        }
        return o1.getDescription().compareTo(o2.getDescription());
    }
}
