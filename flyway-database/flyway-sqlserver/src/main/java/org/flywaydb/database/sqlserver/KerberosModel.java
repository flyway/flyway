package org.flywaydb.database.sqlserver;

import lombok.Data;

@Data
public class KerberosModel {
    private LoginModel login = new LoginModel();
}