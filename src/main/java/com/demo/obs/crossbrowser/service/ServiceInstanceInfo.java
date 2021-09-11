package com.demo.obs.crossbrowser.service;

import com.demo.obs.crossbrowser.configurator.ConfiguratorClient;
import com.demo.obs.crossbrowser.configurator.CreateServiceInstanceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

@Service
public class ServiceInstanceInfo {
    private final ConfiguratorClient configuratorClient;
    private static final Logger logger = LoggerFactory.getLogger(CrossServiceInstanceService.class);

    public ServiceInstanceInfo(ConfiguratorClient configuratorClient) {
        this.configuratorClient = configuratorClient;
    }

    public String getDashboardUrl(String serviceInstanceId) {
        try {
            CreateServiceInstanceResponse createServiceInstanceResponse = configuratorClient.sendGetServiceInstanceRequest(serviceInstanceId);
            if (createServiceInstanceResponse.getDashboardUrl() != null)
                return createServiceInstanceResponse.getDashboardUrl();
            else return "";
        } catch (HttpStatusCodeException exception) {
            logger.error("Не удалось получить dashboardUrl", exception);
            return "";
        }
    }
}
