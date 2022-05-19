package com.application.acchandler.security.filters;

import com.application.acchandler.meta.ResponseControllerMeta;
import org.apache.logging.log4j.util.Strings;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class ResponseHandlerFilter extends OncePerRequestFilter
{

    private static final Logger logger = LoggerFactory.getLogger(ResponseHandlerFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try
        {
            ByteArrayPrinter pw = new ByteArrayPrinter();
            HttpServletResponse wrappedResponse = new HttpServletResponseWrapper(response)
            {
                @Override
                public PrintWriter getWriter() {
                    return pw.getWriter();
                }

                @Override
                public ServletOutputStream getOutputStream() {
                    return pw.getStream();
                }
            };

            filterChain.doFilter(request, wrappedResponse);

            if (response.getStatus() == HttpStatus.UNAUTHORIZED.value())
                return;

            byte[] bytes = pw.toByteArray();

            if (response.containsHeader(HttpHeaders.CONTENT_DISPOSITION)) {
                response.getOutputStream().write(bytes);
                return;
            }

            String responseBody = new String(bytes);
            JSONObject responseJson = null;
            if (Strings.isNotBlank(responseBody))
            {
                responseJson = new JSONObject(responseBody);
            }

            String responseMeta = ResponseControllerMeta.getResponseDetails(responseJson, request);

            if (responseMeta != null && Strings.isNotBlank(responseMeta))
            {
                response.getOutputStream().write(responseMeta.getBytes());
            }
        }
        catch (Exception exception)
        {
            throw exception;
        }

    }

    private class ByteArrayPrinter {

        private ByteArrayOutputStream baos = new ByteArrayOutputStream();

        private PrintWriter pw = new PrintWriter(baos);

        private ServletOutputStream sos = new ByteArrayServletStream(baos);

        public PrintWriter getWriter() {
            return pw;
        }

        public ServletOutputStream getStream() {
            return sos;
        }

        byte[] toByteArray() {
            return baos.toByteArray();
        }
    }

    private class ByteArrayServletStream extends ServletOutputStream {

        ByteArrayOutputStream baos;

        ByteArrayServletStream(ByteArrayOutputStream baos) {
            this.baos = baos;
        }

        @Override
        public void write(int param) throws IOException {
            baos.write(param);
        }

        @Override
        public boolean isReady() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {

        }
    }
}
