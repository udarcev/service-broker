package com.demo.obs.crossbrowser.service;

import com.demo.obs.crossbrowser.config.ServiceBrokerConfiguration;
import com.demo.obs.crossbrowser.configurator.ConfiguratorClient;
import com.demo.obs.crossbrowser.model.GGRServiceBinding;
import com.demo.obs.crossbrowser.repository.binding.ServiceBindingRepository;
import com.demo.obs.crossbrowser.repository.instance.ServiceInstanceRepository;
import com.demo.obs.crossbrowser.validation.OsbObjectValidationService;
import com.demo.obs.crossbrowser.validation.UuidValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceExistsException;
import org.springframework.cloud.servicebroker.model.instance.*;
import org.springframework.cloud.servicebroker.service.ServiceInstanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
public class CrossServiceInstanceService implements ServiceInstanceService {

    private static final String LOG_MSG_RESPONSE = "Response is going to be sent: {}";
    private static final Logger logger = LoggerFactory.getLogger(CrossServiceInstanceService.class);

    private final OsbObjectValidationService osbObjectValidationService;
    private final ServiceInstanceRepository<ServiceInstance> serviceInstanceRepository;
    private final ServiceBindingRepository<GGRServiceBinding> serviceBindingRepository;
    private final ServiceBrokerConfiguration brokerConfig;
    private final ServiceInstanceInfo serviceInstanceInfo;
    private final ConfiguratorClient configuratorClient;

    @Autowired
    public CrossServiceInstanceService(OsbObjectValidationService osbObjectValidationService, ServiceInstanceRepository<ServiceInstance> serviceInstanceRepository, ServiceBindingRepository<GGRServiceBinding> serviceBindingRepository, ServiceBrokerConfiguration brokerConfig, ServiceInstanceInfo serviceInstanceInfo, ConfiguratorClient configuratorClient) {
        this.osbObjectValidationService = osbObjectValidationService;
        this.serviceInstanceRepository = serviceInstanceRepository;
        this.serviceBindingRepository = serviceBindingRepository;
        this.brokerConfig = brokerConfig;
        this.serviceInstanceInfo = serviceInstanceInfo;
        this.configuratorClient = configuratorClient;
    }

    @Override
    public Mono<CreateServiceInstanceResponse> createServiceInstance(CreateServiceInstanceRequest request) {
        logger.debug("Request to create service instance: {}", request);

        osbObjectValidationService.validateNotNullServiceDefinition(request.getServiceDefinition(), request.getServiceDefinitionId());
        osbObjectValidationService.validateServicePlan(request.getPlan(), request.getPlanId());

        final String serviceInstanceId = request.getServiceInstanceId();
        if (brokerConfig.isUuidValidationEnabled()) {
            UuidValidator.validateUuidOrThrowException(serviceInstanceId, "ServiceInstanceId", logger);
        }
        CreateServiceInstanceResponse.CreateServiceInstanceResponseBuilder responseBuilder =
                CreateServiceInstanceResponse.builder().async(false);

        //смотрим, существует ли такой инстанс
        final ServiceInstance serviceInstanceInfo = serviceInstanceRepository.putIfAbsent(
                new ServiceInstance(serviceInstanceId, request.getServiceDefinitionId(), request.getPlanId())
        );
        //если существует, то проверяем отличаются ли параметры инстанса
        if (serviceInstanceInfo != null) {
            responseBuilder.instanceExisted(hasServiceInstanceInfoTheSameParams(serviceInstanceInfo, request.getServiceDefinitionId(), request.getPlanId(), request.getServiceInstanceId()));
        } else {
            serviceInstanceRepository.save(new ServiceInstance(serviceInstanceId, request.getServiceDefinitionId(), request.getPlanId()));
            try {
                configuratorClient.sendCreateServiceInstanceRequest(serviceInstanceId, request.getPlanId());
            } catch (Exception ex) {
                serviceInstanceRepository.deleteById(serviceInstanceId);
                logger.error("Cannot create a user and grand privileges", ex);
                throw new RuntimeException("Internal Error: A service instance cannot be created");
            }
            responseBuilder.instanceExisted(false);
            responseBuilder.dashboardUrl(this.serviceInstanceInfo.getDashboardUrl(serviceInstanceId));
        }
        CreateServiceInstanceResponse response = responseBuilder.build();

        logger.debug(LOG_MSG_RESPONSE, response);

        return Mono.just(response);
    }

    private boolean hasServiceInstanceInfoTheSameParams(final ServiceInstance serviceInstanceInfo, final String serviceDefinitionId,
                                                        final String planId, final String serviceInstanceId) {
        if (serviceDefinitionId.equals(serviceInstanceInfo.getServiceId()) && planId.equals(serviceInstanceInfo.getPlanId())) {
            return true;
        } else {
            logger.warn("ServiceInstance {} already exists but with different parameters", serviceInstanceId);
            throw new ServiceInstanceExistsException(serviceInstanceId, serviceDefinitionId);
        }
    }

    @Override
    public Mono<GetServiceInstanceResponse> getServiceInstance(GetServiceInstanceRequest request) {
        logger.debug("Request to get service instance: {}", request);

        final String serviceInstanceId = request.getServiceInstanceId();
        final ServiceInstance serviceInstance = serviceInstanceRepository.findById(serviceInstanceId)
                .orElseThrow(
                        () -> {
                            logger.warn("Couldn't get non existed service instance: {}", request.getServiceInstanceId());
                            return new ServiceInstanceDoesNotExistException(serviceInstanceId);
                        }
                );
        GetServiceInstanceResponse response = GetServiceInstanceResponse.builder()
                .serviceDefinitionId(serviceInstance.getServiceId())
                .planId(serviceInstance.getPlanId())
                .dashboardUrl(serviceInstanceInfo.getDashboardUrl(serviceInstanceId))
                .build();

        logger.debug(LOG_MSG_RESPONSE, response);

        return Mono.just(response);
    }

    @Override
    @Transactional
    public Mono<DeleteServiceInstanceResponse> deleteServiceInstance(DeleteServiceInstanceRequest request) {
        logger.debug("Request to delete service instance: {}", request);

        osbObjectValidationService.validateNotNullServiceDefinition(request.getServiceDefinition(), request.getServiceDefinitionId());
        osbObjectValidationService.validateServicePlan(request.getPlan(), request.getPlanId());

        final String serviceInstanceId = request.getServiceInstanceId();
        final ServiceInstance serviceInstance = serviceInstanceRepository.findById(serviceInstanceId)
                .filter(ins -> ins.getPlanId().equals(request.getPlanId()) && ins.getServiceId().equals(request.getServiceDefinitionId()))
                .orElseThrow(
                        () -> {
                            logger.warn(
                                    "Couldn't delete non existed service instance: {}",
                                    request.getServiceInstanceId()
                            );
                            return new ServiceInstanceDoesNotExistException(serviceInstanceId);
                        }
                );

        // Delete it from the repository to prohibit addition of new bindings
        serviceBindingRepository.deleteServiceBindingsByServiceInstanceId(serviceInstanceId);
        serviceInstanceRepository.deleteById(serviceInstanceId);

        try {
            logger.debug("Deleting service instance {}", serviceInstance.getServiceInstanceId());
            configuratorClient.sendDeleteServiceInstanceRequest(serviceInstanceId);
        } catch (Exception e) {
            logger.error("Cannot drop the database {}", serviceInstanceId, e);
            // We don't throw the exception so that Platform could continue work properly thinking that
            // the service instance has been deleted. Surely there should be an alert configured on the errors like that
        }

        DeleteServiceInstanceResponse response = DeleteServiceInstanceResponse.builder()
                .async(false)
                .build();

        logger.debug(LOG_MSG_RESPONSE, response);

        return Mono.just(response);
    }
}
