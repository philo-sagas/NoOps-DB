package com.sagas.noops.db.exceptions;

public class DbQueryException extends RuntimeException {
    private String sql;

    public DbQueryException(Throwable cause, String sql) {
        super(cause);
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }
}
