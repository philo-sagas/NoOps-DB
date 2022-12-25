package com.sagas.noops.db.constants;

public enum DbSourceType {
    Oracle("oracle.jdbc.driver.OracleDriver"),
    H2("org.h2.Driver"),
    MariaDB("org.mariadb.jdbc.Driver"),
    MySQL("com.mysql.cj.jdbc.Driver"),
    PostgreSQL("org.postgresql.Driver"),
    SQLServer("com.microsoft.sqlserver.jdbc.SQLServerDriver"),
    DB2("com.ibm.db2.jcc.DB2Driver");

    private String driverClassName;

    DbSourceType(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getDriverClassName() {
        return driverClassName;
    }
}
