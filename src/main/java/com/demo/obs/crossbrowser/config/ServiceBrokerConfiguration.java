package com.demo.obs.crossbrowser.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <pre>
 *     service:
 *       broker:
 *         uuidValidationEnabled: true | false # Значение по умолчанию true
 * </pre>
 */

@ConfigurationProperties(prefix = "service.broker")
public class ServiceBrokerConfiguration {
    /**
     * Надо ли проверять во время работы все UUID на валидность
     */
    private boolean uuidValidationEnabled = true;

    public boolean isUuidValidationEnabled() {
        return uuidValidationEnabled;
    }

    public void setUuidValidationEnabled(boolean uuidValidationEnabled) {
        this.uuidValidationEnabled = uuidValidationEnabled;
    }
}
