package com.sagas.noops.db.entities;

import com.sagas.noops.db.constants.DbSourceType;
import lombok.Data;

@Data
public class DbSource {
    private String id;

    private DbSourceType type;

    private String url;

    private String username;

    private String password;
}
