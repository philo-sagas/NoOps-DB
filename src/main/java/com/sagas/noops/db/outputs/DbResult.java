package com.sagas.noops.db.outputs;

import lombok.Data;

import java.util.List;

@Data
public class DbResult {
    private List<String> columnList;

    private List<List<Object>> dataList;
}
