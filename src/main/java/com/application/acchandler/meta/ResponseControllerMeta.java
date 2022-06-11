package com.application.acchandler.meta;

import com.google.common.collect.ImmutableTable;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

import javax.servlet.http.HttpServletRequest;

class ResponseMeta
{
    private final String responseCode;
    private final String successMessage;

    ResponseMeta(String responseCode, String successMessage) {
        this.responseCode = responseCode;
        this.successMessage = successMessage;
    }

    public String getResponseCode()
    {
        return this.responseCode;
    }

    public String getSuccessMessage()
    {
        return this.successMessage;
    }
}

public class ResponseControllerMeta {

    private static final Logger logger = LoggerFactory.getLogger(ResponseControllerMeta.class);

    public static ImmutableTable<String, HttpMethod, ResponseMeta> responseMetaTable = new ImmutableTable.Builder<String, HttpMethod, ResponseMeta>()
            .put("/send-otp", HttpMethod.POST, new ResponseMeta("send_otp", "OTP has been sent to your mobile."))
            .put("/users/me", HttpMethod.GET, new ResponseMeta("user_info", null))
            .put("/sessions", HttpMethod.GET, new ResponseMeta("session_info", null))
            .put("/users/me", HttpMethod.PUT, new ResponseMeta("update_user", "User details updated."))
            .put("/users/me/profile-picture", HttpMethod.GET, new ResponseMeta("display_picture_get", null))
            .put("/profile-picture", HttpMethod.POST, new ResponseMeta("display_picture_update", "Successfully updated your profile picture."))
            .put("/devices/{ID}/logout", HttpMethod.POST, new ResponseMeta("remote_logout", "Successful signed out."))
            .put("/verify-otp", HttpMethod.POST, new ResponseMeta("verify_otp", "Login Successful."))
            .put("/logout", HttpMethod.POST, new ResponseMeta("log_out", "Successfully logged out."))
            .build();


    public static String getResponseDetails(JSONObject responseJson, HttpServletRequest request)
    {
        if (responseJson == null) responseJson = new JSONObject();

        String uri = request.getRequestURI();
        uri = uri.replaceAll("\\/api\\/v1", "");
        uri = uri.replaceAll("\\/(?=[a-zA-Z]*\\d)[a-zA-Z\\d-]+", "/{ID}");

        logger.info(uri);
        ResponseMeta responseMeta = responseMetaTable.get(uri, HttpMethod.resolve(request.getMethod()));

        if (responseMeta == null)
            return null;

        responseJson.put("code", responseMeta.getResponseCode());
        responseJson.put("message", responseMeta.getSuccessMessage());

        return responseJson.toString();
    }
}
