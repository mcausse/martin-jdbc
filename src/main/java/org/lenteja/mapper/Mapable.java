package org.lenteja.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface Mapable<T> {
    T map(ResultSet rs) throws SQLException;
}