package com.sagas.noops.db.constants;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "no-ops-db")
public class ApplicationConstants {
    private String cachedPath;

    private String adminPassword;

    private String guestPassword;

    public String getCachedPath() {
        return cachedPath;
    }

    public void setCachedPath(String cachedPath) {
        this.cachedPath = cachedPath;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getGuestPassword() {
        return guestPassword;
    }

    public void setGuestPassword(String guestPassword) {
        this.guestPassword = guestPassword;
    }
}
