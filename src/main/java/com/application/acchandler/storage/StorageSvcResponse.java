package com.application.acchandler.storage;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StorageSvcResponse
{
    @JsonProperty("file-name")
    String fileName;

    String message;
    String code;

    public StorageSvcResponse()
    {

    }

    public StorageSvcResponse(String fileName, String message, String code)
    {
        this.fileName = fileName;
        this.message = message;
        this.code = code;
    }

    public void setFileName(String fileName){
        this.fileName = fileName;
    }

    public String getFileName(){
        return this.fileName;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public String getMessage(){
        return this.message;
    }

    public void setCode(String code){
        this.code = code;
    }

    public String getCode(){
        return this.code;
    }
}
