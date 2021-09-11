package com.demo.obs.crossbrowser.service;


import com.demo.obs.crossbrowser.config.ServiceBrokerConfiguration;
import com.demo.obs.crossbrowser.model.CrossPlan;
import com.demo.obs.crossbrowser.model.GGRServiceBinding;
import com.demo.obs.crossbrowser.repository.binding.ServiceBindingRepository;
import com.demo.obs.crossbrowser.repository.instance.ServiceInstanceRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.validator.internal.util.Contracts;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerInvalidParametersException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingDoesNotExistException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.springframework.cloud.servicebroker.model.binding.*;
import org.springframework.cloud.servicebroker.model.catalog.Plan;
import org.springframework.cloud.servicebroker.model.catalog.ServiceDefinition;
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceResponse;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ServiceBindingServiceTests {

    @Autowired
    private ServiceInstanceRepository<ServiceInstance> serviceInstanceRepository;

    @Autowired
    private ServiceBindingRepository<GGRServiceBinding> serviceBindingRepository;

    @Autowired
    private CrossServiceInstanceService instanceService;

    @Autowired
    private CrossServiceBindingService service;

    @SpyBean
    @Autowired
    private ServiceBrokerConfiguration brokerConfiguration;

    @Test(expected = ServiceBrokerInvalidParametersException.class)
    public void createServiceInstanceBindingNotValidBindingId() {
        CreateServiceInstanceBindingRequest request = createCreateServiceInstanceBindingRequestMockWithServiceAndPlanId();

        when(request.getBindingId()).thenReturn("1");
        when(brokerConfiguration.isUuidValidationEnabled()).thenReturn(true);

        service.createServiceInstanceBinding(request);
    }

    @Test(expected = ServiceInstanceDoesNotExistException.class)
    public void createServiceInstanceBindingNonExistingServiceInstance() {
        CreateServiceInstanceBindingRequest request = createCreateServiceInstanceBindingRequestMockWithServiceAndPlanId();
        String serviceInstanceId = UUID.randomUUID().toString();

        when(request.getServiceInstanceId()).thenReturn(serviceInstanceId);
        when(request.getBindingId()).thenReturn(UUID.randomUUID().toString());

        service.createServiceInstanceBinding(request);
    }

    @Test(expected = ServiceInstanceBindingExistsException.class)
    public void createServiceInstanceBindingExistedButWithDifferentServiceId() {
        GGRServiceBinding binding = createBinding();
        createServiceInstanceBindingExistedButWithDifferentParams(true, false, binding);
    }

    @Test(expected = ServiceInstanceBindingExistsException.class)
    public void createServiceInstanceBindingExistedButWithDifferentPlanId() {
        GGRServiceBinding binding = createBinding();
        createServiceInstanceBindingExistedButWithDifferentParams(false, true, binding);
    }

    @Test(expected = ServiceInstanceBindingExistsException.class)
    public void createServiceInstanceBindingExisted() {
        CreateServiceInstanceBindingRequest request = createCreateServiceInstanceBindingRequestMockWithServiceAndPlanId();

        ServiceInstance serviceInstanceInfo = createServiceInstance();
        String serviceInstanceId = serviceInstanceInfo.getServiceInstanceId();

        String name = RandomStringUtils.randomAlphabetic(5);
        String pass = RandomStringUtils.randomAlphanumeric(5);
        GGRServiceBinding binding = new GGRServiceBinding(serviceInstanceId, UUID.randomUUID().toString(), "testappId", name, pass);

        serviceBindingRepository.save(binding);

        String bindingId = binding.getServiceBindingId();

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("APPLICATION_ID", binding.getApplicationId());

        when(request.getParameters()).thenReturn(parameters);
        when(request.getServiceInstanceId()).thenReturn(serviceInstanceId);
        when(request.getBindingId()).thenReturn(bindingId);

        String planId = serviceInstanceInfo.getPlanId();
        String serviceId = serviceInstanceInfo.getServiceId();

        when(request.getPlanId()).thenReturn(planId);
        when(request.getServiceDefinitionId()).thenReturn(serviceId);

        Mono<CreateServiceInstanceBindingResponse> response = service.createServiceInstanceBinding(request);
    }

    @Test(expected = ServiceInstanceBindingExistsException.class)
    public void createExistedServiceInstanceBindingToAnotherServiceInstance() {
        ServiceInstance serviceInstance = createServiceInstance();
        String serviceInstanceId = serviceInstance.getServiceInstanceId();

        String name = RandomStringUtils.randomAlphabetic(5);
        String pass = RandomStringUtils.randomAlphanumeric(5);
        GGRServiceBinding binding = new GGRServiceBinding(serviceInstanceId, UUID.randomUUID().toString(), "asda", name, pass);

        serviceBindingRepository.save(binding);

        serviceInstance = createServiceInstance();
        serviceInstanceId = serviceInstance.getServiceInstanceId();

        ServiceInstance anotherServiceInstance = createServiceInstance();

        CreateServiceInstanceBindingRequest request = createCreateServiceInstanceBindingRequestMockWithServiceAndPlanId();

        when(request.getServiceInstanceId()).thenReturn(serviceInstanceId);
        when(request.getBindingId()).thenReturn(binding.getServiceBindingId());

        String planId = anotherServiceInstance.getPlanId();
        String serviceId = anotherServiceInstance.getServiceId();

        when(request.getPlanId()).thenReturn(planId);
        when(request.getServiceDefinitionId()).thenReturn(serviceId);

        service.createServiceInstanceBinding(request);
    }

    @Test
    public void createServiceInstanceBindingNewBindingCreated() {
        CreateServiceInstanceBindingRequest request = createCreateServiceInstanceBindingRequestMockWithServiceAndPlanId();

        ServiceInstance serviceInstanceInfo = createServiceInstance();
        String serviceInstanceId = serviceInstanceInfo.getServiceInstanceId();
        String name = RandomStringUtils.randomAlphabetic(5);
        String pass = RandomStringUtils.randomAlphanumeric(5);
        GGRServiceBinding binding = new GGRServiceBinding(serviceInstanceId, name, "asda", name, pass);

        String bindingId = binding.getServiceBindingId();
        String planId = serviceInstanceInfo.getPlanId();
        String serviceId = serviceInstanceInfo.getServiceId();

        when(request.getServiceInstanceId()).thenReturn(serviceInstanceId);
        when(request.getBindingId()).thenReturn(bindingId);
        when(request.getPlanId()).thenReturn(planId);
        when(request.getServiceDefinitionId()).thenReturn(serviceId);
        when(brokerConfiguration.isUuidValidationEnabled()).thenReturn(false);

        Mono<CreateServiceInstanceBindingResponse> response = service.createServiceInstanceBinding(request);

        response.subscribe(r -> {
            assertFalse(r.isBindingExisted());

            Map<String, Object> responseCredentials = ((CreateServiceInstanceAppBindingResponse) r).getCredentials();
            assertEquals(name, responseCredentials.get("USER_NAME"));
            assertNotNull(responseCredentials.get("USER_PASSWORD"));
        });
    }

    @Test(expected = ServiceBrokerInvalidParametersException.class)
    public void createServiceInstanceWithNonExistedServiceDefinition() {
        CreateServiceInstanceBindingRequest request = mock(CreateServiceInstanceBindingRequest.class);

        when(request.getServiceDefinitionId()).thenReturn(UUID.randomUUID().toString());

        service.createServiceInstanceBinding(request);
    }

    @Test(expected = ServiceBrokerInvalidParametersException.class)
    public void createServiceInstanceWithNonExistedServiceDefinitionPlan() {
        CreateServiceInstanceBindingRequest request = mock(CreateServiceInstanceBindingRequest.class);

        when(request.getServiceDefinition()).thenReturn(mock(ServiceDefinition.class));
        when(request.getPlanId()).thenReturn(UUID.randomUUID().toString());

        service.createServiceInstanceBinding(request);
    }

    @Test
    public void deleteServiceInstanceBinding() {
        CreateServiceInstanceBindingRequest createServiceInstanceBindingRequest = createCreateServiceInstanceBindingRequestMockWithServiceAndPlanId();

        ServiceInstance serviceInstanceInfo = createServiceInstance();
        String serviceInstanceId = serviceInstanceInfo.getServiceInstanceId();

        String name = RandomStringUtils.randomAlphabetic(5);
        String pass = RandomStringUtils.randomNumeric(5);
        GGRServiceBinding binding = new GGRServiceBinding(serviceInstanceInfo.getServiceInstanceId(), UUID.randomUUID().toString(), "testappId", name, pass);
        serviceBindingRepository.save(binding);

        String bindingId = binding.getServiceBindingId();
        String planId = serviceInstanceInfo.getPlanId();
        String serviceId = serviceInstanceInfo.getServiceId();

        when(createServiceInstanceBindingRequest.getServiceInstanceId()).thenReturn(serviceInstanceId);
        when(createServiceInstanceBindingRequest.getBindingId()).thenReturn(bindingId);
        when(createServiceInstanceBindingRequest.getPlanId()).thenReturn(planId);
        when(createServiceInstanceBindingRequest.getServiceDefinitionId()).thenReturn(serviceId);
        when(brokerConfiguration.isUuidValidationEnabled()).thenReturn(false);

        DeleteServiceInstanceBindingRequest request = mockDeleteRequest(serviceInstanceInfo.getServiceInstanceId(),
                bindingId, serviceInstanceInfo.getServiceId(), serviceInstanceInfo.getPlanId());

        Mono<DeleteServiceInstanceBindingResponse> response = service.deleteServiceInstanceBinding(request);

        response.subscribe(Contracts::assertNotNull);

        assertFalse(serviceBindingRepository.findById(bindingId).isPresent());
        assertNotNull(serviceInstanceRepository.findById(serviceInstanceInfo.getServiceInstanceId()));
    }

    @Test(expected = ServiceInstanceBindingExistsException.class)
    public void deleteServiceInstanceBindingButInstanceWithDifferentServiceId() {
        ServiceInstance serviceInstanceInfo = createServiceInstance();

        String name = RandomStringUtils.randomAlphabetic(5);
        String pass = RandomStringUtils.randomNumeric(5);
        GGRServiceBinding binding = new GGRServiceBinding(serviceInstanceInfo.getServiceInstanceId(), UUID.randomUUID().toString(), "testappId", name, pass);

        serviceBindingRepository.save(binding);

        String serviceBindingId = binding.getServiceBindingId();
        DeleteServiceInstanceBindingRequest request = mockDeleteRequest(serviceInstanceInfo.getServiceInstanceId(),
                serviceBindingId, UUID.randomUUID().toString(), serviceInstanceInfo.getPlanId());

        service.deleteServiceInstanceBinding(request);
    }

    @Test(expected = ServiceInstanceBindingExistsException.class)
    public void deleteServiceInstanceBindingButInstanceWithDifferentPlanId() {
        ServiceInstance serviceInstanceInfo = createServiceInstance();

        String name = RandomStringUtils.randomAlphabetic(5);
        String pass = RandomStringUtils.randomNumeric(5);
        GGRServiceBinding binding = new GGRServiceBinding(serviceInstanceInfo.getServiceInstanceId(), UUID.randomUUID().toString(), "testappId", name, pass);

        serviceBindingRepository.save(binding);

        String serviceBindingId = binding.getServiceBindingId();
        DeleteServiceInstanceBindingRequest request = mockDeleteRequest(serviceInstanceInfo.getServiceInstanceId(),
                serviceBindingId, serviceInstanceInfo.getServiceId(), CrossPlan.EXTENDED.getPlanId());

        service.deleteServiceInstanceBinding(request);
    }

    @Test(expected = ServiceInstanceDoesNotExistException.class)
    public void deleteServiceInstanceBindingButInstanceDoesNotExist() {
        ServiceInstance serviceInstanceInfo = generateServiceInstance();
        String serviceInstanceId = serviceInstanceInfo.getServiceInstanceId();
        String planId = serviceInstanceInfo.getPlanId();
        String serviceId = serviceInstanceInfo.getServiceId();

        DeleteServiceInstanceBindingRequest request = mockDeleteRequest(serviceInstanceId,
                UUID.randomUUID().toString(), serviceId, planId);

        service.deleteServiceInstanceBinding(request);
    }

    @Test(expected = ServiceInstanceBindingDoesNotExistException.class)
    public void deleteServiceInstanceBindingButItDoesNotExist() {
        ServiceInstance serviceInstanceInfo = createServiceInstance();
        String serviceInstanceId = serviceInstanceInfo.getServiceInstanceId();
        String planId = serviceInstanceInfo.getPlanId();
        String serviceId = serviceInstanceInfo.getServiceId();

        GGRServiceBinding bindingCredentials = generateBinding(serviceInstanceInfo);
        String bindingId = bindingCredentials.getServiceBindingId();

        DeleteServiceInstanceBindingRequest request = mockDeleteRequest(serviceInstanceId, bindingId, serviceId, planId);

        service.deleteServiceInstanceBinding(request);
    }

    @Test(expected = ServiceBrokerInvalidParametersException.class)
    public void deleteServiceInstanceWithNonExistedServiceDefinition() {
        DeleteServiceInstanceBindingRequest request = mock(DeleteServiceInstanceBindingRequest.class);

        when(request.getServiceDefinitionId()).thenReturn(UUID.randomUUID().toString());

        service.deleteServiceInstanceBinding(request);
    }

    @Test(expected = ServiceBrokerInvalidParametersException.class)
    public void deleteServiceInstanceWithNonExistedServiceDefinitionPlan() {
        DeleteServiceInstanceBindingRequest request = mock(DeleteServiceInstanceBindingRequest.class);

        when(request.getServiceDefinition()).thenReturn(mock(ServiceDefinition.class));
        when(request.getPlanId()).thenReturn(UUID.randomUUID().toString());

        service.deleteServiceInstanceBinding(request);
    }

    @Test(expected = ServiceInstanceDoesNotExistException.class)
    public void getServiceInstanceBindingNonExistingServiceInstance() {
        GetServiceInstanceBindingRequest request = mock(GetServiceInstanceBindingRequest.class);

        when(request.getServiceInstanceId()).thenReturn(UUID.randomUUID().toString());

        service.getServiceInstanceBinding(request);
    }

    @Test(expected = ServiceInstanceBindingDoesNotExistException.class)
    public void getNonExistingServiceInstanceBinding() {
        ServiceInstance serviceInstanceInfo = createServiceInstance();

        String name = RandomStringUtils.randomAlphabetic(5);
        String pass = RandomStringUtils.randomNumeric(5);
        GGRServiceBinding binding = new GGRServiceBinding(serviceInstanceInfo.getServiceInstanceId(), UUID.randomUUID().toString(), "testappId", name, pass);

        String bindingId = binding.getServiceBindingId();

        GetServiceInstanceBindingRequest request = mock(GetServiceInstanceBindingRequest.class);

        when(request.getServiceInstanceId()).thenReturn(binding.getServiceInstanceId());
        when(request.getBindingId()).thenReturn(bindingId);

        service.getServiceInstanceBinding(request);
    }

    @Test
    public void getExistingServiceInstanceBinding() {
        ServiceInstance serviceInstanceInfo = createServiceInstance();
        String serviceInstanceId = serviceInstanceInfo.getServiceInstanceId();

        String name = RandomStringUtils.randomAlphabetic(5);
        String pass = RandomStringUtils.randomNumeric(5);
        GGRServiceBinding binding = new GGRServiceBinding(serviceInstanceInfo.getServiceInstanceId(), UUID.randomUUID().toString(), "testappId", name, pass);

        serviceBindingRepository.save(binding);

        GetServiceInstanceBindingRequest request = mock(GetServiceInstanceBindingRequest.class);
        when(request.getServiceInstanceId()).thenReturn(serviceInstanceId);

        String bindingId = binding.getServiceBindingId();
        when(request.getBindingId()).thenReturn(bindingId);

        Mono<GetServiceInstanceBindingResponse> response = service.getServiceInstanceBinding(request);
        response.subscribe(r -> {
            Map<String, Object> responseCredentials = ((GetServiceInstanceAppBindingResponse) r).getCredentials();
            assertEquals(name, responseCredentials.get("USER_NAME"));
            assertEquals(pass, responseCredentials.get("USER_PASSWORD"));
        });
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getLastOperation() {
        Mono<GetLastServiceBindingOperationResponse> response = service.getLastOperation(
                mock(GetLastServiceBindingOperationRequest.class));
        response.block();
    }


    private CreateServiceInstanceBindingRequest createCreateServiceInstanceBindingRequestMockWithServiceAndPlanId() {
        CreateServiceInstanceBindingRequest request = mock(CreateServiceInstanceBindingRequest.class);

        when(request.getServiceDefinition()).thenReturn(mock(ServiceDefinition.class));
        when(request.getPlan()).thenReturn(Plan.builder().id(CrossPlan.STANDARD.getPlanId()).build());
        when(request.getPlanId()).thenReturn(CrossPlan.STANDARD.getPlanId());

        return request;
    }

    protected ServiceInstance generateServiceInstance() {
        String serviceInstanceId = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();
        String plainId = CrossPlan.EXTENDED.getPlanId();

        return new ServiceInstance(serviceInstanceId, serviceId, plainId);
    }

    protected GGRServiceBinding createBinding() {
        return createBinding(createServiceInstance());
    }

    protected GGRServiceBinding createBinding(ServiceInstance serviceInstanceInfo) {
        String serviceInstanceId = serviceInstanceInfo.getServiceInstanceId();
        String validatedBindingId = UUID.randomUUID().toString();

        CreateServiceInstanceBindingRequest request = createCreateServiceInstanceBindingRequestMockWithServiceAndPlanId();

        when(request.getServiceInstanceId()).thenReturn(serviceInstanceId);
        when(request.getBindingId()).thenReturn(validatedBindingId);
        when(request.getPlanId()).thenReturn(serviceInstanceInfo.getPlanId());
        when(request.getServiceDefinitionId()).thenReturn(serviceInstanceInfo.getServiceId());

        service.createServiceInstanceBinding(request);
        return serviceBindingRepository.findById(validatedBindingId).get();
    }

    protected GGRServiceBinding generateBinding(ServiceInstance serviceInstanceInfo) {
        String name = RandomStringUtils.randomAlphabetic(5);
        String pass = RandomStringUtils.randomNumeric(5);
        GGRServiceBinding binding = new GGRServiceBinding(serviceInstanceInfo.getServiceInstanceId(), UUID.randomUUID().toString(), "testappId", name, pass);
        return binding;
    }

    protected ServiceInstance createServiceInstance() {
        String serviceInstanceId = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();
        String planId = CrossPlan.STANDARD.getPlanId();

        CreateServiceInstanceRequest request = mock(CreateServiceInstanceRequest.class);
        when(request.getServiceDefinition()).thenReturn(mock(ServiceDefinition.class));
        when(request.getPlan()).thenReturn(Plan.builder().id(planId).build());

        when(request.getServiceInstanceId()).thenReturn(serviceInstanceId);
        when(request.getPlanId()).thenReturn(planId);
        when(request.getServiceDefinitionId()).thenReturn(serviceId);

        Mono<CreateServiceInstanceResponse> response = instanceService.createServiceInstance(request);

        return serviceInstanceRepository.findById(serviceInstanceId).get();
    }

    protected ServiceInstance createServiceInstance(CreateServiceInstanceRequest request) {
        Mono<CreateServiceInstanceResponse> response = instanceService.createServiceInstance(request);
        return serviceInstanceRepository.findById(request.getServiceInstanceId()).get();
    }

    private DeleteServiceInstanceBindingRequest mockDeleteRequest(final String serviceInstanceId, final String bindingId,
                                                                  final String serviceId, final String planId) {

        DeleteServiceInstanceBindingRequest request = mock(DeleteServiceInstanceBindingRequest.class);
        when(request.getServiceDefinition()).thenReturn(mock(ServiceDefinition.class));
        when(request.getPlan()).thenReturn(Plan.builder().id(planId).build());

        when(request.getServiceInstanceId()).thenReturn(serviceInstanceId);
        when(request.getBindingId()).thenReturn(bindingId);
        when(request.getServiceDefinitionId()).thenReturn(serviceId);
        when(request.getPlanId()).thenReturn(planId);

        return request;
    }

    private Mono<CreateServiceInstanceBindingResponse> createServiceInstanceBindingExistedButWithDifferentParams(
            boolean differentServiceId, boolean differentPlanId, GGRServiceBinding binding) {

        final String serviceInstanceId = binding.getServiceInstanceId();
        CreateServiceInstanceBindingRequest request = createCreateServiceInstanceBindingRequestMockWithServiceAndPlanId();

        when(request.getServiceInstanceId()).thenReturn(serviceInstanceId);
        when(request.getBindingId()).thenReturn(binding.getServiceBindingId());

        final String existedServiceId = serviceInstanceId;
        final String existedPlanId = CrossPlan.STANDARD.getPlanId();

        final String newServiceId;
        final String newPlanId;

        String newServiceIdValue = UUID.randomUUID().toString();
        if (differentServiceId) {
            newServiceId = newServiceIdValue;
        } else {
            newServiceId = existedServiceId;
        }

        if (differentPlanId) {
            newPlanId = CrossPlan.EXTENDED.getPlanId();
        } else {
            newPlanId = existedPlanId;
        }

        when(request.getServiceDefinitionId()).thenReturn(newServiceId);
        when(request.getPlan()).thenReturn(Plan.builder().id(newPlanId).build());
        when(request.getPlanId()).thenReturn(newPlanId);

        return service.createServiceInstanceBinding(request);
    }
}
