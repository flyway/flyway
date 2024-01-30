package org.flywaydb.database.postgresql;

import lombok.Data;

@Data
public class TransactionalModel {
    private Boolean lock = null;
}