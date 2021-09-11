package com.demo.obs.crossbrowser.repository.instance;

import com.demo.obs.crossbrowser.service.ServiceInstance;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceInstanceRepository<T extends ServiceInstance> extends CustomServiceInstanceRepository<T>, DatabaseServiceInstanceRepository<T> {
}

