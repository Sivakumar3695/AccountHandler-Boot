package com.application.acchandler.model.DeviceDetails;

public class DeviceDetails {

    private String session_id;
    private String session_primary_id;
    private String device_name;
    private String ip_address;
    private String location;

    public DeviceDetails setSessionId(String session_id){
        this.session_id = session_id;
        return this;
    }

    public String getSession_id(){
        return this.session_id;
    }

    public DeviceDetails setSessionPrimaryID(String session_primary_id){
        this.session_primary_id = session_primary_id;
        return this;
    }

    public String getSession_primary_id(){
        return this.session_primary_id;
    }

    public DeviceDetails setDeviceName(String deviceName){
        this.device_name = deviceName;
        return this;
    }

    public String getDevice_name(){
        return this.device_name;
    }

    public DeviceDetails setIpAddress(String ipAddress){
        this.ip_address = ipAddress;
        return this;
    }

    public String getIp_address(){
        return this.ip_address;
    }

    public DeviceDetails setLocation(String location){
        this.location = location;
        return this;
    }

    public String getLocation(){
        return this.location;
    }
}
