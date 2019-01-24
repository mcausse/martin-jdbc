package cat.lechuga.jdbc.handler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.lenteja.jdbc.query.QueryObject;

public interface ColumnHandler {

    /**
     * donat el valor de bean, retorna el valor a tipus de jdbc, apte per a setejar
     * en un {@link QueryObject} o en {@link PreparedStatement}.
     */
    default Object getJdbcValue(Object value) {
        return value;
    }

    /**
     * retorna el valor de bean a partir del {@link ResultSet}.
     */
    Object readValue(ResultSet rs, String columnName) throws SQLException;
}
