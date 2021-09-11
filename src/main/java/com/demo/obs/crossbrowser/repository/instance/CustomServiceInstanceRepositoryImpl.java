package com.demo.obs.crossbrowser.repository.instance;

import com.demo.obs.crossbrowser.service.ServiceInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class CustomServiceInstanceRepositoryImpl<T extends ServiceInstance> implements CustomServiceInstanceRepository<T> {
    private final DatabaseServiceInstanceRepository<T> databaseServiceInstanceRepository;

    @Autowired
    public CustomServiceInstanceRepositoryImpl(DatabaseServiceInstanceRepository<T> databaseServiceInstanceRepository) {
        this.databaseServiceInstanceRepository = databaseServiceInstanceRepository;
    }

    @Transactional
    public T putIfAbsent(T serviceInstance) {
        String serviceInstanceId = serviceInstance.getServiceInstanceId();
        Optional<T> optionalInstance = this.databaseServiceInstanceRepository.findById(serviceInstanceId);
        if (optionalInstance.isPresent()) {
            return optionalInstance.get();
        } else {
            this.databaseServiceInstanceRepository.save(serviceInstance);
            return null;
        }
    }
}
