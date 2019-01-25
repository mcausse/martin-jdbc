package org.lenteja.mapper.handler;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BoolStringHandler implements ColumnHandler<Boolean> {

    public static final String DEFAULT_TRUE_STRING = "T";
    public static final String DEFAULT_FALSE_STRING = "F";

    final String trueString;
    final String falseString;

    public BoolStringHandler() {
        this(DEFAULT_TRUE_STRING, DEFAULT_FALSE_STRING);
    }

    public BoolStringHandler(String trueString, String falseString) {
        super();
        this.trueString = trueString;
        this.falseString = falseString;
    }

    @Override
    public Object getJdbcValue(Boolean value) {
        if (value == null) {
            return null;
        }
        if (value) {
            return trueString;
        } else {
            return falseString;
        }
    }

    @Override
    public Boolean readValue(ResultSet rs, String columnName) throws SQLException {
        String v = rs.getString(columnName);
        if (v == null) {
            return null;
        }
        return v.equals(trueString);
    }

}