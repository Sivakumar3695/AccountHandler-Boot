package com.application.acchandler.security.filters;

import com.application.acchandler.model.sessionObj.SessionJdbcTemplateOpHandler;
import com.application.acchandler.model.sessionObj.SessionObj;
import com.application.acchandler.security.auth.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.web.http.SessionRepositoryFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class DeviceDetailsPersistenceFilter extends OncePerRequestFilter {

    DeviceService deviceService;

    @Autowired
    public DeviceDetailsPersistenceFilter(DeviceService deviceService){
        this.deviceService = deviceService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        filterChain.doFilter(request, response);

        try {
            deviceService.handleDeviceDetailsTable(request);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}