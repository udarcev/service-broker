package com.demo.obs.crossbrowser.validation;

import org.springframework.cloud.servicebroker.exception.ServiceBrokerInvalidParametersException;
import org.springframework.cloud.servicebroker.model.catalog.Plan;
import org.springframework.cloud.servicebroker.model.catalog.ServiceDefinition;

public interface OsbObjectValidationService {

    void validateNotNullServiceDefinition(ServiceDefinition serviceDefinition, String serviceDefinitionId)
            throws ServiceBrokerInvalidParametersException;

    void validateServicePlan(Plan plan, String requestPlanId) throws ServiceBrokerInvalidParametersException;
}
