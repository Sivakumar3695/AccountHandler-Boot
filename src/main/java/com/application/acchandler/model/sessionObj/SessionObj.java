package com.application.acchandler.model.sessionObj;

public class SessionObj {

    private String primary_id;
    private String session_id;

    public SessionObj setSessionId(String session_id){
        this.session_id = session_id;
        return this;
    }

    public String getSession_id(){
        return this.session_id;
    }

    public SessionObj setPrimaryID(String primaryID){
        this.primary_id = primaryID;
        return this;
    }

    public String getSession_primary_id(){
        return this.primary_id;
    }
}
