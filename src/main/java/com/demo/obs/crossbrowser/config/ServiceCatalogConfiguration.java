package com.demo.obs.crossbrowser.config;

import com.demo.obs.crossbrowser.model.CrossPlan;
import org.springframework.cloud.servicebroker.model.catalog.Catalog;
import org.springframework.cloud.servicebroker.model.catalog.Plan;
import org.springframework.cloud.servicebroker.model.catalog.ServiceDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Configuration
public class ServiceCatalogConfiguration {

    private static final String SERVICE_ID = "52445594-73a9-46d0-8de6-f2ed4a4f7f59";

    private static final String SERVICE_NAME = "crossbrowser";

    @Bean
    public Catalog catalog() {
        List<Plan> plans = Stream.of(CrossPlan.values()).map(plan -> {
            return Plan.builder()
                    .id(plan.getPlanId())
                    .name(plan.getPlanName())
                    .description(plan.getPlanDescription())
                    .free(true)
                    .metadata("displayName", plan.getPlanName())
                    .metadata("browsers", plan.getBrowsersName()) // оставляем для обратной совместимости
                    .metadata("versionsCount", plan.getVersionsCount())
                    .metadata("activeBrowsersCount", plan.getActiveBrowsersCount())
                    .build();
        }).collect(Collectors.toList());

        ServiceDefinition serviceDefinition = ServiceDefinition.builder()
                .id(SERVICE_ID)
                .name(SERVICE_NAME)
                .description("Модуль кроссбраузерного тестирования позволяет проводить параллельный запуск на любых браузерах из набора поддерживаемых.")
                .bindable(true)
                .instancesRetrievable(false)
                .bindingsRetrievable(false)
                .planUpdateable(false)
                .tags(SERVICE_NAME, "testing")
                .metadata("providerDisplayName", "Demo")
                .plans(plans)
                .build();

        return Catalog.builder()
                .serviceDefinitions(serviceDefinition)
                .build();
    }
}
