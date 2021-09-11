package com.demo.obs.crossbrowser.repository.binding;

import com.demo.obs.crossbrowser.service.ServiceBinding;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DatabaseServiceBindingRepository<T extends ServiceBinding> extends CrudRepository<T, String> {
    List<T> findAll();

    @Query("select b.serviceBindingId from ServiceBinding as b")
    List<String> getServiceBindingIdList();

    void deleteServiceBindingsByServiceInstanceId(String serviceInstanceId);

    List<T> getServiceBindingsByServiceInstanceId(String serviceInstanceId);
}

