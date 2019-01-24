package cat.lechuga.jdbc.handler;

import java.sql.ResultSet;
import java.sql.SQLException;

@SuppressWarnings("rawtypes")
public class EnumColumnHandler implements ColumnHandler {

    final Class<Enum> enumClass;

    public EnumColumnHandler(Class<Enum> enumClass) {
        super();
        this.enumClass = enumClass;
    }

    @Override
    public Object getJdbcValue(Object value) {
        if (value == null) {
            return null;
        }
        return ((Enum<?>) value).name();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object readValue(ResultSet rs, String columnName) throws SQLException {
        String name = rs.getString(columnName);
        if (name == null) {
            return null;
        }
        return Enum.valueOf(enumClass, name);
    }
}