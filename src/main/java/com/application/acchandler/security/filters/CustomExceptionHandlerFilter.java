package com.application.acchandler.security.filters;

import com.application.acchandler.exceptions.CustomAuthenticationException;
import com.application.acchandler.exceptions.CustomException;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

@Component
public class CustomExceptionHandlerFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
            filterChain.doFilter(request, response);
        }
        catch (Exception exception)
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);

            logger.error(exception.getMessage());
            logger.error(exception);
            logger.error(sw.toString()); //stack trace

            JSONObject responseJson = new JSONObject();

            if (exception instanceof CustomException)
            {
                responseJson.put("message", exception.getMessage());
                response.setStatus(CustomException.CUSTOM_EXCEPTION_FAILURE_HTTP_STATUS_CODE);
            }
            else if (exception instanceof CustomAuthenticationException)
            {
                responseJson.put("message", exception.getMessage());
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
            }
            else if (exception instanceof MaxUploadSizeExceededException ||
                    exception instanceof SizeLimitExceededException ||
                    exception.getCause() instanceof MaxUploadSizeExceededException ||
                    exception.getCause() instanceof SizeLimitExceededException)
            {
                responseJson.put("message", "File too large. You can upload image up to 2 MB");
                response.setStatus(HttpStatus.PAYLOAD_TOO_LARGE.value());
            }
            else
            {
                responseJson.put("message", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }

            response.getWriter().write(responseJson.toString());
        }
    }
}
