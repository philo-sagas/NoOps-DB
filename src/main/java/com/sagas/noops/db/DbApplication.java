package com.sagas.noops.db;

import com.sagas.noops.db.services.DbSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class DbApplication implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(DbApplication.class, args);
    }

    @Autowired
    private DbSourceService dbSourceService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        dbSourceService.loadCachedDataSourceList();
    }
}
