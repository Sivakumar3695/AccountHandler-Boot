package com.application.acchandler.security.auth;

import com.application.acchandler.model.DeviceDetails.DeviceDetails;
import com.application.acchandler.model.DeviceDetails.DeviceDetailsJdbcOpHandler;
import com.application.acchandler.model.DeviceDetails.DeviceDetailsRowMapper;
import com.application.acchandler.model.sessionObj.SessionJdbcTemplateOpHandler;
import com.application.acchandler.model.sessionObj.SessionObj;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.session.Session;
import org.springframework.session.jdbc.JdbcIndexedSessionRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.web.context.request.RequestContextHolder;
import ua_parser.Client;
import ua_parser.Parser;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.nonNull;

@Component
public class DeviceService {

    private static final String UNKNOWN = "UNKNOWN";

    private DatabaseReader databaseReader;
    private JdbcTemplate jdbcTemplate;
    private Parser parser;

    private SessionJdbcTemplateOpHandler sessionJdbcTemplateOpHandler;
    private DeviceDetailsJdbcOpHandler deviceDetailsJdbcOpHandler;

    @Autowired
    public DeviceService(
            DatabaseReader databaseReader,
            JdbcTemplate jdbcTemplate,
            Parser parser,
            SessionJdbcTemplateOpHandler sessionJdbcTemplateOpHandler,
            DeviceDetailsJdbcOpHandler deviceDetailsJdbcOpHandler
            )
    {
        this.databaseReader = databaseReader;
        this.jdbcTemplate = jdbcTemplate;
        this.parser = parser;
        this.sessionJdbcTemplateOpHandler = sessionJdbcTemplateOpHandler;
        this.deviceDetailsJdbcOpHandler = deviceDetailsJdbcOpHandler;
    }


    private String getDeviceDetails(String userAgent) {
        String deviceDetails = UNKNOWN;

        Client client = parser.parse(userAgent);
        if (Objects.nonNull(client)) {
            deviceDetails = client.userAgent.family + " " + client.userAgent.major + "." + client.userAgent.minor +
                    " - " + client.os.family;
        }

        System.out.println(deviceDetails);

        return deviceDetails;
    }

    public void handleDeviceDetailsTable(HttpServletRequest request)
    {
        try
        {
            String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId();

            DeviceDetails deviceInfo = deviceDetailsJdbcOpHandler.getDeviceDetailsBasedOnSessionId(sessionId);

            if (deviceInfo == null)
            {
                SessionObj session = null;
                if (Objects.nonNull(sessionId))
                {
                    session = sessionJdbcTemplateOpHandler.findSessionDetailsBySessionId(sessionId);

                    if (session == null)
                        return;
                }
                //insert new device details.

                String ip = extractIp(request);
                String location = getIpLocation(ip);

                System.out.println(ip);
                System.out.println(location);
                String deviceName = getDeviceDetails(request.getHeader("user-agent"));

                insertDeviceDetails(ip, location, deviceName, session);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Exception Occurred");
        }
    }

    private void insertDeviceDetails(String ip, String location, String deviceName, SessionObj session) throws Exception
    {
        DeviceDetails deviceDetails =
                new DeviceDetails()
                        .setSessionPrimaryID(session.getSession_primary_id())
                        .setSessionId(session.getSession_id())
                        .setIpAddress(ip)
                        .setLocation(location)
                        .setDeviceName(deviceName);

        deviceDetailsJdbcOpHandler.insertNewDeviceDetails(deviceDetails);
    }

    private String extractIp(HttpServletRequest request) {
        String clientIp;
        String clientXForwardedForIp = request
                .getHeader("x-forwarded-for");
        if (nonNull(clientXForwardedForIp)) {
            clientIp = parseXForwardedHeader(clientXForwardedForIp);
        } else {
            clientIp = request.getRemoteAddr();
        }
        return clientIp;
    }

    private String parseXForwardedHeader(String header) {
        return header.split(" *, *")[0];
    }

    private String getIpLocation(String ip) throws IOException, GeoIp2Exception {

        if (Objects.equals(ip, "0:0:0:0:0:0:0:1") || Objects.equals(ip, "127.0.0.1"))
        {
            return "HOST device";
        }
        File database = ResourceUtils
                .getFile("classpath:maxmind/GeoLite2-City.mmdb");
        DatabaseReader dbReader = new DatabaseReader.Builder(database).build();

        String location = null;
        InetAddress ipAddress = InetAddress.getByName(ip);
        CityResponse cityResponse = dbReader
                .city(ipAddress);

        if (!Strings.isNotBlank(cityResponse.getCity().getName())) {
            if (Objects.nonNull(cityResponse) && Objects.nonNull(cityResponse.getCity())) {
                location = cityResponse.getCity().getName();
            }
        }
        return location;
    }
}