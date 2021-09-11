package com.demo.obs.crossbrowser.repository.instance;

import com.demo.obs.crossbrowser.service.ServiceInstance;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomServiceInstanceRepository<T extends ServiceInstance> {
    T putIfAbsent(T serviceInstance);
}
