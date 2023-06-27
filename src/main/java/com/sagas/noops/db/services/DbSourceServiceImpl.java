package com.sagas.noops.db.services;

import com.sagas.noops.db.constants.ApplicationConstants;
import com.sagas.noops.db.constants.DbSourceType;
import com.sagas.noops.db.entities.DbSource;
import io.micrometer.common.util.StringUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Log4j2
@Service
public class DbSourceServiceImpl implements DbSourceService, InitializingBean {
    private static final String PLACEHOLDER = "♡";

    private static final CopyOnWriteArrayList<DbSource> cachedDbSourceList = new CopyOnWriteArrayList<>();

    private static final ConcurrentHashMap<String, DataSource> dataSourceMap = new ConcurrentHashMap<>();

    private static final AtomicInteger cachedVersion = new AtomicInteger();

    @Autowired
    private ApplicationConstants applicationConstants;

    @Override
    public void afterPropertiesSet() throws Exception {
        loadCachedDataSourceList();
    }

    @Override
    public List<DbSource> getAll() {
        return cachedDbSourceList.stream().toList();
    }

    @Override
    public DbSource getBy(String id) {
        return cachedDbSourceList.stream().filter(dbSource -> dbSource.getId().equals(id)).findAny().orElse(null);
    }

    @Override
    public boolean save(DbSource dbSource, int dataVersion) {
        Assert.isTrue(cachedVersion.compareAndSet(dataVersion, dataVersion + 1), "数据源列表已经发生改变，请重新操作。");

        cachedDbSourceList.stream().filter(ds -> ds.getId().equals(dbSource.getId()))
                .findAny().ifPresentOrElse(ds -> {
                    ds.setType(dbSource.getType());
                    ds.setUrl(dbSource.getUrl());
                    ds.setUsername(dbSource.getUsername());
                    if (StringUtils.isNotBlank(dbSource.getPassword())) {
                        ds.setPassword(dbSource.getPassword());
                    }
                    DataSource dataSource = dataSourceMap.remove(ds.getId());
                    if (dataSource instanceof AutoCloseable) {
                        try {
                            ((AutoCloseable) dataSource).close();
                        } catch (Exception ex) {
                            log.warn(ex);
                        }
                    }
                    dataSourceMap.put(ds.getId(), buildDataSource(ds));
                }, () -> {
                    cachedDbSourceList.add(dbSource);
                    dataSourceMap.put(dbSource.getId(), buildDataSource(dbSource));
                });
        CompletableFuture.runAsync(() -> persistCachedDataSourceList());

        return true;
    }

    @Override
    public boolean delete(String id, int dataVersion) {
        Assert.isTrue(cachedVersion.compareAndSet(dataVersion, dataVersion + 1), "数据源列表已经发生改变，请重新操作。");

        boolean success = cachedDbSourceList.removeIf(dataSource -> dataSource.getId().equals(id));
        if (success) {
            DataSource dataSource = dataSourceMap.remove(id);
            if (dataSource instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) dataSource).close();
                } catch (Exception ex) {
                    log.warn(ex);
                }
            }
            CompletableFuture.runAsync(() -> persistCachedDataSourceList());
        }

        return success;
    }

    @Override
    public DataSource findDataSource(String dbSourceId) {
        return dataSourceMap.get(dbSourceId);
    }

    @Override
    public int getDataVersion() {
        return cachedVersion.get();
    }

    private void loadCachedDataSourceList() {
        try {
            Path path = getCachedPath();
            List<String> lines = Files.readAllLines(path);
            List<DbSource> dbSourceList = lines.stream().map(line -> {
                String[] items = line.split(PLACEHOLDER);
                DbSource dbSource = new DbSource();
                dbSource.setId(items[0]);
                dbSource.setType(DbSourceType.valueOf(items[1]));
                dbSource.setUrl(items[2]);
                dbSource.setUsername(items[3]);
                dbSource.setPassword(items.length >= 5 ? items[4] : "");
                return dbSource;
            }).toList();
            cachedDbSourceList.clear();
            cachedDbSourceList.addAll(dbSourceList);
            dataSourceMap.putAll(dbSourceList.stream()
                    .collect(Collectors.toMap(DbSource::getId, this::buildDataSource)));
            cachedVersion.set(1);
        } catch (Exception ex) {
            log.warn(ex, ex);
        }
    }

    private void persistCachedDataSourceList() {
        try {
            List<String> lines = cachedDbSourceList.stream().map(dbSource -> {
                StringBuilder builder = new StringBuilder();
                builder.append(dbSource.getId()).append(PLACEHOLDER)
                        .append(dbSource.getType()).append(PLACEHOLDER)
                        .append(dbSource.getUrl()).append(PLACEHOLDER)
                        .append(dbSource.getUsername()).append(PLACEHOLDER)
                        .append(dbSource.getPassword()).append(PLACEHOLDER);
                return builder.toString();
            }).toList();
            Path path = getCachedPath();
            Files.write(path, lines);
        } catch (Exception ex) {
            log.warn(ex);
        }
    }

    private Path getCachedPath() throws IOException {
        Path path = Path.of(applicationConstants.getCachedPath());
        if (!Files.exists(path)) {
            Files.createFile(path);
            System.out.printf("Cached Path: %s%n", path.toAbsolutePath());
        }
        return path;
    }

    private DataSource buildDataSource(DbSource dbSource) {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create()
                .driverClassName(dbSource.getType().getDriverClassName())
                .url(dbSource.getUrl())
                .username(dbSource.getUsername())
                .password(dbSource.getPassword());
        DataSource dataSource = dataSourceBuilder.build();
        return dataSource;
    }
}
