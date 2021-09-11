package com.demo.obs.crossbrowser.repository.instance;

import com.demo.obs.crossbrowser.service.ServiceInstance;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DatabaseServiceInstanceRepository<T extends ServiceInstance> extends CrudRepository<T, String> {
    List<T> findAll();

    @Query("select s.serviceInstanceId from ServiceInstance as s")
    List<String> getServiceInstanceIdList();
}

