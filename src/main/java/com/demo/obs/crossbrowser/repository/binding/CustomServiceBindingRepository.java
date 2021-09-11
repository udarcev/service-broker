package com.demo.obs.crossbrowser.repository.binding;

import com.demo.obs.crossbrowser.service.ServiceBinding;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomServiceBindingRepository<T extends ServiceBinding> {
    T putIfAbsent(T serviceBinding);
}
