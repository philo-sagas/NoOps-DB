package com.sagas.noops.db.services;

import com.sagas.noops.db.entities.DbSource;

import javax.sql.DataSource;
import java.util.List;

public interface DbSourceService {
    List<DbSource> getAll();

    DbSource getBy(String id);

    boolean save(DbSource dbSource, int dataVersion);

    boolean delete(String id, int dataVersion);

    DataSource findDataSource(String dbSourceId);

    int getDataVersion();
}
