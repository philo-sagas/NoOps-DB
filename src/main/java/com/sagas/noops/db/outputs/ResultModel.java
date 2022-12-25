package com.sagas.noops.db.outputs;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
public class ResultModel<T> {
    private int status;

    private String error;

    private T result;

    public static <M> ResultModel<M> success(M result) {
        return new ResultModel<>(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(), result);
    }

    public static <M> ResultModel<M> success(M result, String error) {
        return new ResultModel<>(HttpStatus.OK.value(), error, result);
    }

    public static <M> ResultModel<M> failure(M result, String error) {
        return new ResultModel<>(HttpStatus.SERVICE_UNAVAILABLE.value(), error, result);
    }

    public static ResultModel failure(String error) {
        return new ResultModel<>(HttpStatus.SERVICE_UNAVAILABLE.value(), error, null);
    }
}
