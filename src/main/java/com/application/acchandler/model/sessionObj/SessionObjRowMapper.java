package com.application.acchandler.model.sessionObj;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SessionObjRowMapper implements RowMapper<SessionObj> {

    @Override
    public SessionObj mapRow(ResultSet rs, int rowNum) throws SQLException {

        SessionObj sessionObj = new SessionObj();
        sessionObj.setSessionId(rs.getString("SESSION_ID"));
        sessionObj.setPrimaryID(rs.getString("PRIMARY_ID"));

        return sessionObj;
    }
}
