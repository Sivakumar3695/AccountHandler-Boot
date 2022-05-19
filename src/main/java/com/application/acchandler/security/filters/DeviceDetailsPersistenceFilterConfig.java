package com.application.acchandler.security.filters;

import com.application.acchandler.security.auth.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class DeviceDetailsPersistenceFilterConfig {

    @Autowired
    private DeviceDetailsPersistenceFilter deviceDetailsPersistenceFilter;

    @Autowired
    private GoogleOneTapAuthFilter googleOneTapAuthFilter;

    @Bean
    public FilterRegistrationBean<DeviceDetailsPersistenceFilter> customFilter_1()
    {
        FilterRegistrationBean<DeviceDetailsPersistenceFilter> bean = new FilterRegistrationBean<>();

        bean.setFilter(deviceDetailsPersistenceFilter);
        bean.setOrder(SecurityProperties.DEFAULT_FILTER_ORDER - 1);
        bean.addUrlPatterns("/users/me");

        return bean;
    }
}
