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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingDoesNotExistException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.springframework.cloud.servicebroker.model.binding.*;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.security.SecureRandom;

@Service
public class CrossServiceBindingService implements ServiceInstanceBindingService {

    private static final String LOG_MSG_RESPONSE = "Response is going to be sent: {}";
    private static final Logger logger = LoggerFactory.getLogger(ServiceInstanceBindingService.class);

    @Value("${ggr_url}")
    private String GGR_URL;

    private final OsbObjectValidationService osbObjectValidationService;
    private final ServiceInstanceRepository<ServiceInstance> serviceInstanceRepository;
    private final ServiceBindingRepository<GGRServiceBinding> serviceBindingRepository;
    private final ServiceBrokerConfiguration brokerConfig;
    private final ConfiguratorClient configuratorClient;

    public CrossServiceBindingService(OsbObjectValidationService osbObjectValidationService, ServiceInstanceRepository<ServiceInstance> serviceInstanceRepository, ServiceBindingRepository<GGRServiceBinding> serviceBindingRepository, ServiceBrokerConfiguration brokerConfiguration, ConfiguratorClient configuratorClient) {
        this.osbObjectValidationService = osbObjectValidationService;
        this.serviceInstanceRepository = serviceInstanceRepository;
        this.serviceBindingRepository = serviceBindingRepository;
        this.brokerConfig = brokerConfiguration;
        this.configuratorClient = configuratorClient;
    }

    @Override
    public Mono<CreateServiceInstanceBindingResponse> createServiceInstanceBinding(CreateServiceInstanceBindingRequest request) {
        logger.debug("Request to create service instance binding: {}", request);

        osbObjectValidationService.validateNotNullServiceDefinition(request.getServiceDefinition(), request.getServiceDefinitionId());
        osbObjectValidationService.validateServicePlan(request.getPlan(), request.getPlanId());

        if (!serviceBindingRepository.getServiceBindingsByServiceInstanceId(request.getServiceInstanceId()).isEmpty()) {
            throw new ServiceInstanceBindingExistsException("Service binding for service instance already exist", request.getServiceInstanceId(), request.getBindingId());
        }

        final String serviceInstanceId = request.getServiceInstanceId();
        CreateServiceInstanceBindingResponse response = createUserAndPrepareResponse(
                request.getServiceDefinitionId(), request.getPlanId(), serviceInstanceId, request.getBindingId()
        );

        logger.debug(LOG_MSG_RESPONSE, response);

        return Mono.just(response);
    }

    private CreateServiceInstanceBindingResponse createUserAndPrepareResponse(String serviceId, String planId, String serviceInstanceId, String bindingId) {
        if (brokerConfig.isUuidValidationEnabled()) {
            UuidValidator.validateUuidOrThrowException(bindingId, "BindingId", logger);
        }

        CreateServiceInstanceAppBindingResponse.CreateServiceInstanceAppBindingResponseBuilder responseBuilder =
                CreateServiceInstanceAppBindingResponse.builder().async(false);

        final ServiceInstance serviceInstance = getServiceInstanceInfoOrThrowException(serviceInstanceId);

        final String userPassword = generatePassword();
        final GGRServiceBinding newBinding = new GGRServiceBinding(
                serviceInstanceId,
                bindingId,
                "testApp",
                bindingId,
                userPassword
        );
        GGRServiceBinding binding = serviceBindingRepository.putIfAbsent(newBinding);

        if (binding != null) {
            responseBuilder.bindingExisted(hasServiceInstanceInfoTheSameParams(serviceInstance, serviceId, planId, serviceInstanceId, bindingId));
        } else {
            try {
                configuratorClient.sendCreateServiceBindingRequest(serviceInstanceId, bindingId);
            } catch (Exception e) { // мб четко ловить нужные
                serviceBindingRepository.deleteById(bindingId);
                logger.error("Cannot create a user and grand privileges", e);
                throw new RuntimeException("Internal Error: A service binding cannot be created");
            }

            responseBuilder.bindingExisted(false);
            binding = newBinding;
        }

        responseBuilder
                .credentials("USER_NAME", binding.getUserName())
                .credentials("USER_PASSWORD", binding.getPassword())
                .credentials("GGR_URL", GGR_URL);

        return responseBuilder.build();
    }

    private ServiceInstance getServiceInstanceInfoOrThrowException(final String serviceInstanceId) {
        return serviceInstanceRepository.findById(serviceInstanceId)
                .orElseThrow(
                        () -> {
                            logger.warn("The specified serviceInstance {} does not exist", serviceInstanceId);
                            return new ServiceInstanceDoesNotExistException(serviceInstanceId);
                        }
                );
    }

    private static String generatePassword() {
        final int numBits = 128;
        SecureRandom random = new SecureRandom();
        // generate random number over the range 0 to 2^numBits - 1, inclusive
        BigInteger randomBigNumber = new BigInteger(numBits, random);

        // Transform the number to a string with max radix (all numbers + all english letters)
        return randomBigNumber.toString(Character.MAX_RADIX);
    }

    private boolean hasServiceInstanceInfoTheSameParams(
            final ServiceInstance serviceInstance, final String serviceId, final String planId,
            final String serviceInstanceId, final String validatedBindingId) {

        if (serviceId.equals(serviceInstance.getServiceId()) && planId.equals(serviceInstance.getPlanId())) {
            return true;
        } else {
            logger.warn("ServiceInstanceBinding {} already exists but with different parameters", validatedBindingId);
            throw new ServiceInstanceBindingExistsException(serviceInstanceId, validatedBindingId);
        }
    }

    @Override
    public Mono<GetServiceInstanceBindingResponse> getServiceInstanceBinding(GetServiceInstanceBindingRequest request) {
        logger.debug("Request to get service instance binding: {}", request);
        String serviceInstanceId = request.getServiceInstanceId();

        ServiceInstance serviceInstance = getServiceInstanceInfoOrThrowException(serviceInstanceId);
        logger.info("Requested instance id {} exist", serviceInstance.getServiceInstanceId());

        GGRServiceBinding serviceBinding = serviceBindingRepository
                .findById(request.getBindingId())
                .orElseThrow(
                        () -> {
                            logger.warn("ServiceBinding with id {} does not exist", request.getBindingId());
                            return new ServiceInstanceBindingDoesNotExistException(request.getBindingId());
                        }
                );

        GetServiceInstanceBindingResponse response = GetServiceInstanceAppBindingResponse.builder()
                .credentials("USER_NAME", serviceBinding.getUserName())
                .credentials("USER_PASSWORD", serviceBinding.getPassword())
                .credentials("GGR_URL", GGR_URL)
                .build();

        logger.debug(LOG_MSG_RESPONSE, response);
        return Mono.just(response);
    }

    @Override
    public Mono<DeleteServiceInstanceBindingResponse> deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request) {
        logger.debug("Request to delete service instance binding: {}", request);

        osbObjectValidationService.validateNotNullServiceDefinition(request.getServiceDefinition(), request.getServiceDefinitionId());
        osbObjectValidationService.validateServicePlan(request.getPlan(), request.getPlanId());

        final String serviceInstanceId = request.getServiceInstanceId();
        final String bindingId = request.getBindingId();

        final ServiceInstance serviceInstance =
                serviceInstanceRepository.findById(serviceInstanceId).orElseThrow(
                        () -> {
                            logger.warn("Service instance {} does not exist", serviceInstanceId);
                            return new ServiceInstanceDoesNotExistException(serviceInstanceId);
                        }
                );

        GGRServiceBinding binding = serviceBindingRepository.findById(bindingId).orElse(null);

        if (binding == null) {
            logger.warn("Service instance binding {} does not exist", bindingId);
            throw new ServiceInstanceBindingDoesNotExistException(bindingId);
        }

        if (serviceInstance.getServiceInstanceId().equals(serviceInstanceId) &&
                serviceInstance.getPlanId().equals(request.getPlanId()) &&
                serviceInstance.getServiceId().equals(request.getServiceDefinitionId())) {
            try {
                serviceBindingRepository.deleteById(bindingId);
                configuratorClient.sendDeleteServiceBindingRequest(serviceInstanceId, bindingId);
            } catch (Exception e) {
                logger.error("Cannot drop user {}", bindingId, e);
                // We don't throw the exception so that Platform could continue work properly thinking that
                // the service instance has been deleted. Surely there should be an alert configured on the errors
                // like that
            }
        } else {
            logger.warn("ServiceInstanceBinding {} already exists but with different parameters", bindingId);
            throw new ServiceInstanceBindingExistsException(serviceInstanceId, bindingId);
        }

        DeleteServiceInstanceBindingResponse response = DeleteServiceInstanceBindingResponse.builder()
                .async(false)
                .build();

        logger.debug(LOG_MSG_RESPONSE, response);

        return Mono.just(response);
    }
}
