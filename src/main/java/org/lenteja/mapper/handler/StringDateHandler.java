package org.lenteja.mapper.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class StringDateHandler implements ColumnHandler<String> {

    public static final String DEFAULT_DATE_FORMAT = "yyyyMMdd";

    final String dateFormat;

    public StringDateHandler(String dateFormat) {
        super();
        this.dateFormat = dateFormat;
    }

    public StringDateHandler() {
        this(DEFAULT_DATE_FORMAT);
    }

    @Override
    public Object getJdbcValue(String value) {
        if (value == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        try {
            return sdf.parse(value);
        } catch (ParseException e) {
            throw new RuntimeException("parsing '" + value + "' with format: '" + dateFormat + "'", e);
        }
    }

    @Override
    public String readValue(ResultSet rs, String columnName) throws SQLException {
        Timestamp v = rs.getTimestamp(columnName);
        if (v == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(v);
    }

}