package com.demo.obs.crossbrowser.service;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(
        name = "SERVICE_INSTANCE"
)
public class ServiceInstance {
    @Id
    @Column(
            name = "SERVICE_INSTANCE_ID"
    )
    private String serviceInstanceId;
    @Column(
            name = "SERVICE_ID",
            nullable = false
    )
    private String serviceId;
    @Column(
            name = "PLAN_ID",
            nullable = false
    )
    private String planId;
    @Column(
            name = "PLATFORM"
    )
    private String platform;
    @Column(
            name = "SOLUTION_ID"
    )
    private String solutionId;

    public ServiceInstance() {
    }

    public ServiceInstance(String serviceInstanceId, String serviceId, String planId) {
        this.serviceInstanceId = serviceInstanceId;
        this.serviceId = serviceId;
        this.planId = planId;
    }

    public ServiceInstance(String serviceInstanceId, String serviceId, String planId, String platform, String solutionId) {
        this.serviceInstanceId = serviceInstanceId;
        this.serviceId = serviceId;
        this.planId = planId;
        this.platform = platform;
        this.solutionId = solutionId;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null && this.getClass() == obj.getClass()) {
            ServiceInstance that = (ServiceInstance) obj;
            return this.serviceInstanceId.equals(that.serviceInstanceId);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.serviceInstanceId});
    }

    public String getServiceInstanceId() {
        return this.serviceInstanceId;
    }

    public void setServiceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    public String getServiceId() {
        return this.serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getPlanId() {
        return this.planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getPlatform() {
        return this.platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getSolutionId() {
        return this.solutionId;
    }

    public void setSolutionId(String solutionId) {
        this.solutionId = solutionId;
    }
}
