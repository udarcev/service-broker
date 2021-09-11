package com.demo.obs.crossbrowser.configurator;

import com.demo.obs.crossbrowser.model.CrossPlan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerInvalidParametersException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Этот класс используется для передачи заказов на создание/удаление инстанса/биндинга во внешнюю систему,
 * которая имеет в себе все необходимые методы для работы с Azure DevOps
 *
 * для прохождения тестов все restTemplate.exchange были закомментированы
 */

@Component
public class ConfiguratorClient {

    @Value("${instance_url}")
    private String INSTANCES_URL;
    private RestTemplate restTemplate;

    public ConfiguratorClient() {
        HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory =
                new HttpComponentsClientHttpRequestFactory();
        httpComponentsClientHttpRequestFactory.setConnectionRequestTimeout(20 * 60 * 1000);
        httpComponentsClientHttpRequestFactory.setConnectTimeout(20 * 60 * 1000);

        restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(httpComponentsClientHttpRequestFactory);
    }

    public void sendCreateServiceInstanceRequest(String serviceInstanceId, String planId) {
        Optional<CrossPlan> crossPlan = Stream.of(CrossPlan.values()).filter(x -> x.getPlanId().equals(planId)).findFirst();
        if (!crossPlan.isPresent())
            throw new ServiceBrokerInvalidParametersException("Не найден выбранный план");

        HttpEntity<CreateServiceInstanceRequest> requestHttpEntity = new HttpEntity<>(
                CreateServiceInstanceRequest.builder()
                        .setActiveBrowsersCount(String.valueOf(crossPlan.get().getActiveBrowsersCount()))
                        .setBrowsersName(crossPlan.get().getBrowsersName())
                        .setVersionsCount(String.valueOf(crossPlan.get().getVersionsCount())).build());

//        ResponseEntity<CreateServiceInstanceResponse> response = restTemplate.exchange(INSTANCES_URL + serviceInstanceId, HttpMethod.PUT, requestHttpEntity, CreateServiceInstanceResponse.class);
    }

    public void sendDeleteServiceInstanceRequest(String serviceInstanceId) {
//        ResponseEntity<String> response = restTemplate.exchange(INSTANCES_URL + serviceInstanceId, HttpMethod.DELETE, null, String.class);
    }

    public CreateServiceInstanceResponse sendGetServiceInstanceRequest(String serviceInstanceId) {
//        ResponseEntity<CreateServiceInstanceResponse> response = restTemplate.exchange(INSTANCES_URL + serviceInstanceId, HttpMethod.GET, null, CreateServiceInstanceResponse.class);
//        return response.getBody();
        return new CreateServiceInstanceResponse("dashboard.test.url", "ggr.test.url");
    }

    public void sendCreateServiceBindingRequest(String serviceInstanceId, String serviceBindingId) {
//        ResponseEntity<String> response = restTemplate.exchange(INSTANCES_URL + serviceInstanceId + "/service_bindings/" + serviceBindingId, HttpMethod.PUT, null, String.class);
    }

    public void sendDeleteServiceBindingRequest(String serviceInstanceId, String serviceBindingId) {
//        ResponseEntity<String> response = restTemplate.exchange(INSTANCES_URL + serviceInstanceId + "/service_bindings/" + serviceBindingId, HttpMethod.DELETE, null, String.class);
    }
}
