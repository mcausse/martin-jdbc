package org.lenteja.mapper.handler;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EnumColumnHandler<T extends Enum<T>> implements ColumnHandler<T> {

    final Class<T> enumClass;

    public EnumColumnHandler(Class<T> enumClass) {
        super();
        this.enumClass = enumClass;
    }

    @Override
    public Object getJdbcValue(T value) {
        if (value == null) {
            return null;
        }
        return value.name();
    }

    @Override
    public T readValue(ResultSet rs, String columnName) throws SQLException {
        String name = rs.getString(columnName);
        if (name == null) {
            return null;
        }
        return Enum.valueOf(enumClass, name);
    }
}