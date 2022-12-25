package com.sagas.noops.db.services;

import com.sagas.noops.db.entities.DbSource;
import com.sagas.noops.db.outputs.DbResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Service
public class DbQueryServiceImpl implements DbQueryService {
    private JdbcTemplate jdbcTemplate = new JdbcTemplate();

    @Override
    public List<DbResult> execute(String sql, DataSource dataSource) {
        List<DbResult> dbResultList = new LinkedList<>();
        jdbcTemplate.setDataSource(dataSource);
        jdbcTemplate.execute((StatementCallback<?>) stmt -> {
            stmt.execute(sql);
            do {
                ResultSet resultSet = stmt.getResultSet();
                if (resultSet != null) {
                    ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                    int columnCount = resultSetMetaData.getColumnCount();
                    List<String> columnList = new ArrayList<>(columnCount);
                    for (int i = 1; i <= columnCount; i++) {
                        columnList.add(resultSetMetaData.getColumnName(i));
                    }
                    List<List<Object>> dataList = new LinkedList<>();
                    while (resultSet.next()) {
                        List<Object> rowInfo = new ArrayList<>(columnCount);
                        for (int i = 1; i <= columnCount; i++) {
                            rowInfo.add(resultSet.getObject(i));
                        }
                        dataList.add(rowInfo);
                    }
                    DbResult dbResult = new DbResult();
                    dbResult.setColumnList(columnList);
                    dbResult.setDataList(dataList);
                    dbResultList.add(dbResult);
                }
                int updateCount = stmt.getUpdateCount();
                if (updateCount != -1) {
                    DbResult dbResult = new DbResult();
                    dbResult.setColumnList(Arrays.asList("Affected Rows"));
                    dbResult.setDataList(Arrays.asList(Arrays.asList(updateCount)));
                    dbResultList.add(dbResult);
                }
            } while (stmt.getMoreResults() || stmt.getUpdateCount() != -1);
            return dbResultList;
        });
        return dbResultList;
    }

    @Override
    public void testConnection(DbSource dbSource) throws Exception {
        Connection connection = null;
        try {
            Class.forName(dbSource.getType().getDriverClassName());
            connection = DriverManager.getConnection(dbSource.getUrl(), dbSource.getUsername(), dbSource.getPassword());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    // ignore
                }
            }
        }
    }
}
