package com.application.acchandler.model.DeviceDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

import javax.persistence.OneToOne;
import javax.servlet.http.HttpSession;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class DeviceDetailsJdbcOpHandler {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public DeviceDetails getDeviceDetailsBasedOnSessionId(String sessionId)
    {
        List<DeviceDetails> deviceDetailsList = jdbcTemplate.query("Select * from DEVICE_DETAILS where SESSION_ID=?",
                new DeviceDetailsRowMapper(), sessionId);

        return deviceDetailsList.isEmpty() ? null : deviceDetailsList.get(0);
    }

    public void insertNewDeviceDetails(DeviceDetails deviceDetails)
    {
        jdbcTemplate.update("INSERT INTO DEVICE_DETAILS (PRIMARY_ID, SESSION_ID, DEVICE_NAME, IP_ADDRESS, LOCATION)" +
                        " VALUES (? , ?, ?, ?, ?)",
                deviceDetails.getSession_primary_id(),
                deviceDetails.getSession_id(),
                deviceDetails.getDevice_name(),
                deviceDetails.getIp_address(),
                deviceDetails.getLocation());
    }

    public List<Map> getDeviceDetailsByPrincipalName(List<String> possiblePrincipalNames)
    {

        SqlParameterSource params = new MapSqlParameterSource("principal_name_values", possiblePrincipalNames);
        final String sql = "Select device.PRIMARY_ID, session.SESSION_ID as SESSION_ID, LAST_ACCESS_TIME, CREATION_TIME, DEVICE_NAME, IP_ADDRESS, LOCATION " +
                "FROM DEVICE_DETAILS as device " +
                "INNER JOIN SPRING_SESSION as session ON session.PRIMARY_ID = device.PRIMARY_ID " +
                "WHERE session.PRINCIPAL_NAME IN (:principal_name_values)";

        String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId();

        List<Map> result = new NamedParameterJdbcTemplate(jdbcTemplate).query(
                sql, params,
                (RowMapper<Map>) (rs, rowNum) -> {
                    DateFormat f = new SimpleDateFormat("MMM dd, YYYY HH:mm:ss");

                    Map resultMap = new HashMap();
                    resultMap.put("device_id", rs.getString("PRIMARY_ID"));
                    resultMap.put("last_activity_time", f.format(new Date(rs.getLong("LAST_ACCESS_TIME"))));
                    resultMap.put("login_time", f.format(new Date(rs.getLong("CREATION_TIME"))));
                    resultMap.put("device", rs.getString("DEVICE_NAME"));
                    resultMap.put("ip_address", rs.getString("IP_ADDRESS"));
                    resultMap.put("location", rs.getString("LOCATION"));
                    resultMap.put("this_device", Objects.equals(sessionId, rs.getString("SESSION_ID")));
                    return resultMap;
                }
        );
        return result.isEmpty() ? new ArrayList<>() : result;
    }
}
