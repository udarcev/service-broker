package com.demo.obs.crossbrowser;

import com.demo.obs.crossbrowser.config.ServiceBrokerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Spring application entry point.
 */
@SpringBootApplication
@EnableConfigurationProperties({ServiceBrokerConfiguration.class})
@EntityScan(basePackages = "com.demo.obs")
public class CrossBrowserServiceBrokerApplication {
    public static void main(String[] args) {
        SpringApplication.run(CrossBrowserServiceBrokerApplication.class, args);
    }
}

