package org.flywaydb.core.internal.resolver;

import org.flywaydb.core.api.resolver.ResolvedMigration;

import java.util.Comparator;

public class ResolvedMigrationComparator implements Comparator<ResolvedMigration> {
    @Override
    public int compare(ResolvedMigration o1, ResolvedMigration o2) {
        if ((o1.getVersion() != null) && o2.getVersion() != null) {
            int v = o1.getVersion().compareTo(o2.getVersion());
            if (v == 0) {
                if (o1.getType().isUndo() && o2.getType().isUndo()) {
                    return 0;
                }
                if (o1.getType().isUndo()) {
                    return 1;
                }
                if (o2.getType().isUndo()) {
                    return -1;
                }
            }
            return v;
        }
        if (o1.getVersion() != null) {
            return -1;
        }
        if (o2.getVersion() != null) {
            return 1;
        }
        return o1.getDescription().compareTo(o2.getDescription());
    }
}