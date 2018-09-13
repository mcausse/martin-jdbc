package org.lenteja.mapper.autogen;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.lenteja.jdbc.ResultSetUtils;
import org.lenteja.jdbc.exception.JdbcException;

public class ScalarMappers {

    public static final ScalarHandler<String> STRING = (rs) -> ResultSetUtils.getString(rs);
    public static final ScalarHandler<Boolean> BOOLEAN = (rs) -> ResultSetUtils.getBoolean(rs);
    public static final ScalarHandler<Date> TIMESTAMP = (rs) -> ResultSetUtils.getTimestamp(rs);
    public static final ScalarHandler<byte[]> BYTE_ARRAY = (rs) -> ResultSetUtils.getBytes(rs);

    public static final ScalarHandler<Byte> BYTE = (rs) -> ResultSetUtils.getByte(rs);
    public static final ScalarHandler<Short> SHORT = (rs) -> ResultSetUtils.getShort(rs);
    public static final ScalarHandler<Integer> INTEGER = (rs) -> ResultSetUtils.getInteger(rs);
    public static final ScalarHandler<Long> LONG = (rs) -> ResultSetUtils.getLong(rs);
    public static final ScalarHandler<Float> FLOAT = (rs) -> ResultSetUtils.getFloat(rs);
    public static final ScalarHandler<Double> DOUBLE = (rs) -> ResultSetUtils.getDouble(rs);
    public static final ScalarHandler<BigDecimal> BIG_DECIMAL = (rs) -> ResultSetUtils.getBigDecimal(rs);

    public static final ScalarHandler<Byte> PBYTE = (rs) -> rs.getByte(1);
    public static final ScalarHandler<Short> PSHORT = (rs) -> rs.getShort(1);
    public static final ScalarHandler<Integer> PINTEGER = (rs) -> rs.getInt(1);
    public static final ScalarHandler<Long> PLONG = (rs) -> rs.getLong(1);
    public static final ScalarHandler<Float> PFLOAT = (rs) -> rs.getFloat(1);
    public static final ScalarHandler<Double> PDOUBLE = (rs) -> rs.getDouble(1);

    static final Map<Class<?>, ScalarHandler<?>> scalarMappers = new LinkedHashMap<>();

    static {
        scalarMappers.put(String.class, STRING);
        scalarMappers.put(Date.class, TIMESTAMP);
        scalarMappers.put(byte[].class, BYTE_ARRAY);
        scalarMappers.put(BigDecimal.class, BIG_DECIMAL);

        scalarMappers.put(Boolean.class, BOOLEAN);
        scalarMappers.put(Byte.class, BYTE);
        scalarMappers.put(Short.class, SHORT);
        scalarMappers.put(Integer.class, INTEGER);
        scalarMappers.put(Long.class, LONG);
        scalarMappers.put(Float.class, FLOAT);
        scalarMappers.put(Double.class, DOUBLE);

        // scalarMappers.put(boolean.class, PBOOLEAN);
        scalarMappers.put(byte.class, PBYTE);
        scalarMappers.put(short.class, PSHORT);
        scalarMappers.put(int.class, PINTEGER);
        scalarMappers.put(long.class, PLONG);
        scalarMappers.put(float.class, PFLOAT);
        scalarMappers.put(double.class, PDOUBLE);
    }

    @SuppressWarnings("unchecked")
    public static <T> ScalarHandler<T> getScalarMapperFor(Class<?> columnClass) {
        if (!scalarMappers.containsKey(columnClass)) {
            throw new JdbcException("no scalar mapper defined for: " + columnClass.getName());
        }
        return (ScalarHandler<T>) scalarMappers.get(columnClass);
    }

}