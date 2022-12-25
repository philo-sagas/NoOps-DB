package com.sagas.noops.db.services;

import com.sagas.noops.db.entities.DbSource;
import com.sagas.noops.db.outputs.DbResult;

import javax.sql.DataSource;
import java.util.List;

public interface DbQueryService {
    List<DbResult> execute(String sql, DataSource dataSource);

    void testConnection(DbSource dbSource) throws Exception;
}
