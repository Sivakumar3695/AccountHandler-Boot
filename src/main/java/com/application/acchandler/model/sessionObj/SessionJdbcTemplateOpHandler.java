package com.application.acchandler.model.sessionObj;

import com.application.acchandler.model.DeviceDetails.DeviceDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SessionJdbcTemplateOpHandler {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public SessionObj findSessionDetailsBySessionId(String sessionId)
    {
        String sql = "select * from SPRING_SESSION where SESSION_ID = ?";

        List<SessionObj> sessionObjs = jdbcTemplate.query(sql, new SessionObjRowMapper(), sessionId);
        return sessionObjs.isEmpty() ? null : sessionObjs.get(0);
    }

    public SessionObj findSessionDetailsByPrimaryIdAndPrincipalName(String primaryId, List<String> principalName)
    {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("primary_id", primaryId);
        params.addValue("principal_names", principalName);
        String sql = "select * from SPRING_SESSION where PRIMARY_ID = :primary_id and PRINCIPAL_NAME IN (:principal_names)";

        List<SessionObj> sessionObjs = new NamedParameterJdbcTemplate(jdbcTemplate)
                .query(sql, params, new SessionObjRowMapper());
        return sessionObjs.isEmpty() ? null : sessionObjs.get(0);
    }
}
