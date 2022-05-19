package com.application.acchandler.exceptions;

import javax.servlet.ServletException;

public class CustomException extends ServletException {
    public static final int CUSTOM_EXCEPTION_FAILURE_HTTP_STATUS_CODE = 520;
    String message;

    public CustomException(String message)
    {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
