package com.demo.obs.crossbrowser.repository.binding;

import com.demo.obs.crossbrowser.service.ServiceBinding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class CustomServiceBindingRepositoryImpl<T extends ServiceBinding> implements CustomServiceBindingRepository<T> {
    private final DatabaseServiceBindingRepository<T> databaseServiceBindingRepository;

    @Autowired
    public CustomServiceBindingRepositoryImpl(DatabaseServiceBindingRepository<T> databaseServiceBindingRepository) {
        this.databaseServiceBindingRepository = databaseServiceBindingRepository;
    }

    @Transactional
    public T putIfAbsent(T serviceBinding) {
        String bindingId = serviceBinding.getServiceBindingId();
        Optional<T> optionalBinding = this.databaseServiceBindingRepository.findById(bindingId);
        if (optionalBinding.isPresent()) {
            return optionalBinding.get();
        } else {
            this.databaseServiceBindingRepository.save(serviceBinding);
            return null;
        }
    }
}
