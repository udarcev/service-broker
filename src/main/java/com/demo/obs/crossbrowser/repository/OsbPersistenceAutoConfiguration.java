package com.demo.obs.crossbrowser.repository;

import com.demo.obs.crossbrowser.service.ServiceBinding;
import com.demo.obs.crossbrowser.service.ServiceInstance;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@Configuration
@AutoConfigureAfter({JpaRepositoriesAutoConfiguration.class})
@EnableJpaRepositories
@EntityScan(
        basePackageClasses = {ServiceBinding.class, ServiceInstance.class},
        basePackages = {"com.demo.osb"}
)
public class OsbPersistenceAutoConfiguration {
    public OsbPersistenceAutoConfiguration() {
    }
}
