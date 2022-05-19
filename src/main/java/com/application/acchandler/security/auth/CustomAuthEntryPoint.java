package com.application.acchandler.security.auth;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        try
        {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            JSONObject responseJson = new JSONObject();
            responseJson.put("message", "Please ensure if the OTP is valid.");

            response.getWriter().write(responseJson.toString());
        }
        catch (Exception e)
        {
            logger.info(e.getMessage());
        }
    }
}
