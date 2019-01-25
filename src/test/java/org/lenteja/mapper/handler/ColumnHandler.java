package org.lenteja.mapper.handler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.lenteja.jdbc.query.QueryObject;

public interface ColumnHandler<T> {

    /**
     * donat el valor de bean, retorna el valor a tipus de jdbc, apte per a setejar
     * en un {@link QueryObject} o en {@link PreparedStatement}.
     */
    default Object getJdbcValue(T value) {
        return value;
    }

    /**
     * retorna el valor de bean a partir del {@link ResultSet}.
     */
    T readValue(ResultSet rs, String columnName) throws SQLException;
}
