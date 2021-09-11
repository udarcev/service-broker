package com.demo.obs.crossbrowser.service;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Inheritance(
        strategy = InheritanceType.SINGLE_TABLE
)
@Table(
        name = "SERVICE_BINDING"
)
public class ServiceBinding {
    @Id
    @Column(
            name = "SERVICE_BINDING_ID"
    )
    private String serviceBindingId;
    @Column(
            name = "SERVICE_INSTANCE_ID",
            nullable = false
    )
    private String serviceInstanceId;
    @Column(
            name = "APPLICATION_ID"
    )
    private String applicationId;

    public ServiceBinding() {
    }

    public ServiceBinding(String serviceInstanceId, String serviceBindingId) {
        this.serviceInstanceId = serviceInstanceId;
        this.serviceBindingId = serviceBindingId;
    }

    public ServiceBinding(String serviceInstanceId, String serviceBindingId, String applicationId) {
        this.serviceInstanceId = serviceInstanceId;
        this.serviceBindingId = serviceBindingId;
        this.applicationId = applicationId;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null && this.getClass() == obj.getClass()) {
            ServiceBinding binding = (ServiceBinding) obj;
            return this.serviceBindingId.equals(binding.serviceBindingId);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.serviceBindingId});
    }

    public String getServiceBindingId() {
        return this.serviceBindingId;
    }

    public void setServiceBindingId(String serviceBindingId) {
        this.serviceBindingId = serviceBindingId;
    }

    public String getServiceInstanceId() {
        return this.serviceInstanceId;
    }

    public void setServiceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    public String getApplicationId() {
        return this.applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }
}
