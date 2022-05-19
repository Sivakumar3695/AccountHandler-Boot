package com.application.acchandler.model.DeviceDetails;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DeviceDetailsRowMapper implements RowMapper<DeviceDetails> {

    @Override
    public DeviceDetails mapRow(ResultSet rs, int rowNum) throws SQLException {

        DeviceDetails deviceDetails = new DeviceDetails();

        deviceDetails
                .setSessionPrimaryID(rs.getString("PRIMARY_ID"))
                .setSessionId(rs.getString("SESSION_ID"))
                .setDeviceName(rs.getString("DEVICE_NAME"))
                .setIpAddress(rs.getString("IP_ADDRESS"))
                .setLocation(rs.getString("LOCATION"));

        return deviceDetails;
    }
}
