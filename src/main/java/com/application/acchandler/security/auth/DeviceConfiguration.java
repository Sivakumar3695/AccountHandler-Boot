package com.application.acchandler.security.auth;

import com.maxmind.geoip2.DatabaseReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;
import ua_parser.Parser;

import java.io.File;
import java.io.IOException;

@Configuration
public class DeviceConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(DeviceConfiguration.class);

    @Value("classpath:maxmind/GeoLite2-City.mmdb")
    private Resource database;

    @Bean
    public Parser uaParser() throws IOException {
        return new Parser();
    }

    @Bean(name="GeoIPCity")
    public DatabaseReader databaseReader() throws IOException {
//        File database = ResourceUtils
//                .getFile("classpath:maxmind/GeoLite2-City.mmdb");

        logger.info(String.valueOf(database != null));
        logger.info(database.toString());

        return new DatabaseReader.Builder(database.getInputStream())
                .build();
    }
}
