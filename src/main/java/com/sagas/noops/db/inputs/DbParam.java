package com.sagas.noops.db.inputs;

import lombok.Data;

@Data
public class DbParam {
    private String sql;

    private String dbSourceId;
}
