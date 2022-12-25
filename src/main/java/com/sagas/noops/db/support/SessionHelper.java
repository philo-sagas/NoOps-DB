package com.sagas.noops.db.support;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Data
@Component
@SessionScope
public class SessionHelper {
    private String dbSourceId;
}
