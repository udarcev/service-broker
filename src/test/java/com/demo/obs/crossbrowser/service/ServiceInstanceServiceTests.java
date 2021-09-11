package com.demo.obs.crossbrowser.service;

import com.demo.obs.crossbrowser.model.CrossPlan;
import com.demo.obs.crossbrowser.model.GGRServiceBinding;
import com.demo.obs.crossbrowser.repository.binding.ServiceBindingRepository;
import com.demo.obs.crossbrowser.repository.instance.ServiceInstanceRepository;
import junit.framework.TestCase;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerInvalidParametersException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceDoesNotExistException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceExistsException;
import org.springframework.cloud.servicebroker.model.catalog.Plan;
import org.springframework.cloud.servicebroker.model.catalog.ServiceDefinition;
import org.springframework.cloud.servicebroker.model.instance.*;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(SpringRunner.class)
@SpringBootTest
public class ServiceInstanceServiceTests {

    private static final String SERVICE_INSTANCE_ID = UUID.randomUUID().toString();

    @Autowired
    private ServiceInstanceRepository<ServiceInstance> serviceInstanceRepository;

    @Autowired
    private ServiceBindingRepository<GGRServiceBinding> serviceBindingRepository;

    @Autowired
    private CrossServiceInstanceService service;

    @Test(expected = ServiceBrokerInvalidParametersException.class)
    public void createServiceInstanceNotValidServiceInstanceId() {
        CreateServiceInstanceRequest request = createCreateServiceInstanceRequestMockWithServiceAndPlanId(CrossPlan.STANDARD.getPlanId());
        when(request.getServiceInstanceId()).thenReturn("1");
        service.createServiceInstance(request);
    }

    @Test(expected = ServiceInstanceExistsException.class)
    public void createServiceInstanceServiceInstanceExistedButWithDifferentServiceId() {
        createServiceInstanceServiceInstanceExistedButWithDifferentParams(true, false);
    }

    @Test(expected = ServiceInstanceExistsException.class)
    public void createServiceInstanceServiceInstanceExistedButWithDifferentPlanId() {

        createServiceInstanceServiceInstanceExistedButWithDifferentParams(false, true);
    }

    @Test
    public void createServiceInstanceServiceInstanceExisted() {
        Mono<CreateServiceInstanceResponse> response = createServiceInstanceServiceInstanceExistedButWithDifferentParams(
                false, false);

        response.subscribe(r -> {
            TestCase.assertNull(r.getOperation());
            TestCase.assertTrue(r.isInstanceExisted());
        });
    }

    @Test
    public void createServiceInstanceNewInstanceCreated() {
        String planId = CrossPlan.STANDARD.getPlanId();
        CreateServiceInstanceRequest request = createCreateServiceInstanceRequestMockWithServiceAndPlanId(planId);

        String serviceInstanceId = UUID.randomUUID().toString();
        String randomUuid = UUID.randomUUID().toString();

        when(request.getServiceInstanceId()).thenReturn(serviceInstanceId);
        when(request.getPlanId()).thenReturn(planId);
        when(request.getServiceDefinitionId()).thenReturn(randomUuid);

        Mono<CreateServiceInstanceResponse> response = service.createServiceInstance(request);

        response.subscribe(r -> {
            TestCase.assertNull(r.getOperation());
            assertFalse(r.isInstanceExisted());
            assertNotNull(r.getDashboardUrl());
        });
    }

    @Test(expected = ServiceBrokerInvalidParametersException.class)
    public void createServiceInstanceWithNonExistedServiceDefinition() {
        CreateServiceInstanceRequest request = mock(CreateServiceInstanceRequest.class);

        when(request.getServiceDefinitionId()).thenReturn(UUID.randomUUID().toString());

        service.createServiceInstance(request);
    }

    @Test(expected = ServiceBrokerInvalidParametersException.class)
    public void createServiceInstanceWithNonExistedServiceDefinitionPlan() {
        CreateServiceInstanceRequest request = mock(CreateServiceInstanceRequest.class);

        when(request.getServiceDefinition()).thenReturn(mock(ServiceDefinition.class));
        when(request.getPlanId()).thenReturn(UUID.randomUUID().toString());

        service.createServiceInstance(request);
    }

    @Test
    public void deleteExistedServiceInstance() {
        DeleteServiceInstanceRequest request = createDeleteServiceInstanceRequestMockWithServiceAndPlanId();
        ServiceInstance instance = createServiceInstance();
        List<GGRServiceBinding> bindings = new ArrayList<>();

        String name = RandomStringUtils.randomAlphabetic(5);
        String pass = RandomStringUtils.randomNumeric(5);
        for (int i = 0; i < 2; i++) {
            GGRServiceBinding binding = new GGRServiceBinding(instance.getServiceInstanceId(), UUID.randomUUID().toString(), "testappId", name, pass);
            bindings.add(binding);
            serviceBindingRepository.save(binding);
        }

        String serviceInstanceId = instance.getServiceInstanceId();
        when(request.getServiceInstanceId()).thenReturn(serviceInstanceId);
        when(request.getPlanId()).thenReturn(instance.getPlanId());
        when(request.getServiceDefinitionId()).thenReturn(instance.getServiceId());

        service.deleteServiceInstance(request);

        assertFalse(serviceInstanceRepository.findById(instance.getServiceInstanceId()).isPresent());

        for (GGRServiceBinding binding : bindings) {
            assertFalse(serviceBindingRepository.findById(binding.getServiceBindingId()).isPresent());
        }
    }

    @Test(expected = ServiceInstanceDoesNotExistException.class)
    public void deleteNotExistedServiceInstance() {
        ServiceInstance serviceInstance = generateServiceInstance();
        DeleteServiceInstanceRequest request = createDeleteServiceInstanceRequestMockWithServiceAndPlanId();
        String serviceInstanceId = serviceInstance.getServiceInstanceId();

        when(request.getServiceInstanceId()).thenReturn(serviceInstanceId);
        when(request.getPlanId()).thenReturn(serviceInstance.getPlanId());
        when(request.getServiceDefinitionId()).thenReturn(serviceInstance.getServiceId());

        service.deleteServiceInstance(request);
    }

    @Test(expected = ServiceInstanceDoesNotExistException.class)
    public void deleteExistedServiceInstanceWithDifferentPlanParams() {
        ServiceInstance serviceInstance = createServiceInstance();
        DeleteServiceInstanceRequest request = createDeleteServiceInstanceRequestMockWithServiceAndPlanId();
        String serviceInstanceId = serviceInstance.getServiceInstanceId();

        when(request.getServiceInstanceId()).thenReturn(serviceInstanceId);
        when(request.getPlanId()).thenReturn(CrossPlan.EXTENDED.getPlanId());
        when(request.getPlan()).thenReturn(Plan.builder().id(CrossPlan.EXTENDED.getPlanId()).build());
        when(request.getServiceDefinitionId()).thenReturn(serviceInstance.getServiceId());

        service.deleteServiceInstance(request);
    }

    @Test(expected = ServiceInstanceDoesNotExistException.class)
    public void deleteExistedServiceInstanceWithDifferentServiceIdParams() {
        ServiceInstance serviceInstance = createServiceInstance();
        DeleteServiceInstanceRequest request = createDeleteServiceInstanceRequestMockWithServiceAndPlanId();
        String serviceInstanceId = serviceInstance.getServiceInstanceId();

        when(request.getServiceInstanceId()).thenReturn(serviceInstanceId);
        when(request.getPlanId()).thenReturn(serviceInstance.getPlanId());
        when(request.getServiceDefinitionId()).thenReturn(UUID.randomUUID().toString());

        service.deleteServiceInstance(request);
    }

    @Test(expected = ServiceBrokerInvalidParametersException.class)
    public void deleteServiceInstanceWithNonExistedServiceDefinition() {
        DeleteServiceInstanceRequest request = mock(DeleteServiceInstanceRequest.class);

        when(request.getServiceDefinitionId()).thenReturn(UUID.randomUUID().toString());

        service.deleteServiceInstance(request);
    }

    @Test(expected = ServiceBrokerInvalidParametersException.class)
    public void deleteServiceInstanceWithNonExistedServiceDefinitionPlan() {
        DeleteServiceInstanceRequest request = mock(DeleteServiceInstanceRequest.class);

        when(request.getServiceDefinition()).thenReturn(mock(ServiceDefinition.class));
        when(request.getPlanId()).thenReturn(UUID.randomUUID().toString());

        service.deleteServiceInstance(request);
    }

    @Test(expected = ServiceInstanceDoesNotExistException.class)
    public void getNonExistingServiceInstance() {
        GetServiceInstanceRequest request = mock(GetServiceInstanceRequest.class);

        when(request.getServiceInstanceId()).thenReturn(UUID.randomUUID().toString());

        service.getServiceInstance(request);
    }

    @Test
    public void getExistingServiceInstance() {
        GetServiceInstanceRequest request = mock(GetServiceInstanceRequest.class);
        ServiceInstance serviceInstanceInfo = createServiceInstance();

        String serviceInstanceId = serviceInstanceInfo.getServiceInstanceId();
        when(request.getServiceInstanceId()).thenReturn(serviceInstanceId);

        Mono<GetServiceInstanceResponse> response = service.getServiceInstance(request);

        response.subscribe(r -> {
            assertEquals(serviceInstanceInfo.getServiceId(), r.getServiceDefinitionId());
            assertEquals(serviceInstanceInfo.getPlanId(), r.getPlanId());
        });
    }

    private Mono<CreateServiceInstanceResponse> createServiceInstanceServiceInstanceExistedButWithDifferentParams(
            boolean differentServiceId, boolean differentPlanId) {

        final String existedServiceId = "1";
        final String existedPlanId = CrossPlan.STANDARD.getPlanId();

        final String newServiceId;
        final String newPlanId;

        if (differentServiceId) {
            newServiceId = "2";
        } else {
            newServiceId = existedServiceId;
        }

        if (differentPlanId) {
            newPlanId = CrossPlan.EXTENDED.getPlanId();
        } else {
            newPlanId = existedPlanId;
        }

        String serviceInstanceId = UUID.randomUUID().toString();

        CreateServiceInstanceRequest requestOld = createCreateServiceInstanceRequestMockWithServiceAndPlanId(existedPlanId);

        when(requestOld.getServiceInstanceId()).thenReturn(serviceInstanceId);
        when(requestOld.getServiceDefinitionId()).thenReturn(existedServiceId);
        when(requestOld.getPlanId()).thenReturn(existedPlanId);
        when(requestOld.getPlan()).thenReturn(Plan.builder().id(existedPlanId).build());

        service.createServiceInstance(requestOld);
        CreateServiceInstanceRequest requestDiff = createCreateServiceInstanceRequestMockWithServiceAndPlanId(existedPlanId);

        when(requestDiff.getServiceInstanceId()).thenReturn(serviceInstanceId);
        when(requestDiff.getServiceDefinitionId()).thenReturn(newServiceId);
        when(requestDiff.getPlanId()).thenReturn(newPlanId);
        when(requestDiff.getPlan()).thenReturn(Plan.builder().id(newPlanId).build());

        return service.createServiceInstance(requestDiff);
    }

    protected ServiceInstance generateServiceInstance() {
        String serviceInstanceId = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();
        String plainId = CrossPlan.STANDARD.getPlanId();

        return new ServiceInstance(serviceInstanceId, serviceId, plainId);
    }

    protected ServiceInstance createServiceInstance() {
        String serviceInstanceId = UUID.randomUUID().toString();
        String serviceId = UUID.randomUUID().toString();
        String planId = CrossPlan.STANDARD.getPlanId();

        CreateServiceInstanceRequest request = createCreateServiceInstanceRequestMockWithServiceAndPlanId(planId);

        when(request.getServiceInstanceId()).thenReturn(serviceInstanceId);
        when(request.getPlanId()).thenReturn(planId);
        when(request.getServiceDefinitionId()).thenReturn(serviceId);

        Mono<CreateServiceInstanceResponse> response = service.createServiceInstance(request);

        return serviceInstanceRepository.findById(serviceInstanceId).get();
    }

    protected ServiceInstance createServiceInstance(CreateServiceInstanceRequest request) {
        Mono<CreateServiceInstanceResponse> response = service.createServiceInstance(request);
        return serviceInstanceRepository.findById(request.getServiceInstanceId()).get();
    }

    private CreateServiceInstanceRequest createCreateServiceInstanceRequestMockWithServiceAndPlanId(String planId) {
        CreateServiceInstanceRequest request = mock(CreateServiceInstanceRequest.class);

        when(request.getServiceDefinition()).thenReturn(mock(ServiceDefinition.class));
        when(request.getPlan()).thenReturn(Plan.builder().id(planId).build());

        return request;
    }

    private DeleteServiceInstanceRequest createDeleteServiceInstanceRequestMockWithServiceAndPlanId() {
        DeleteServiceInstanceRequest request = mock(DeleteServiceInstanceRequest.class);

        when(request.getServiceDefinition()).thenReturn(mock(ServiceDefinition.class));
        when(request.getPlan()).thenReturn(Plan.builder().id(CrossPlan.STANDARD.getPlanId()).build());

        return request;
    }
}
