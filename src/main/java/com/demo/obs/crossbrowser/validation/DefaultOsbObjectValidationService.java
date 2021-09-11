package com.demo.obs.crossbrowser.validation;

import org.springframework.cloud.servicebroker.exception.ServiceBrokerInvalidParametersException;
import org.springframework.cloud.servicebroker.model.catalog.Plan;
import org.springframework.cloud.servicebroker.model.catalog.ServiceDefinition;
import org.springframework.stereotype.Service;

/**
 * Проверяет объекты OSB API на условие соответствия спецификации.
 */
@Service
public class DefaultOsbObjectValidationService implements OsbObjectValidationService {

    @Override
    public void validateNotNullServiceDefinition(ServiceDefinition serviceDefinition, String serviceDefinitionId)
            throws ServiceBrokerInvalidParametersException {
        if (serviceDefinition == null) {
            throw new ServiceBrokerInvalidParametersException(
                    "Service Definition doesn't exist. " + "Service Definition id: " + serviceDefinitionId);
        }
    }

    @Override
    public void validateServicePlan(Plan plan, String requestPlanId) throws ServiceBrokerInvalidParametersException {
        if (plan == null) {
            throw new ServiceBrokerInvalidParametersException("Service plan doesn't exist. Plan id: " + requestPlanId);
        }
    }

}
