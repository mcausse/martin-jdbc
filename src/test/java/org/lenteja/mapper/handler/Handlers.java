package org.lenteja.mapper.handler;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.lenteja.jdbc.ResultSetUtils;
import org.lenteja.jdbc.exception.JdbcException;

public class Handlers {

    public static final ColumnHandler<String> STRING = (rs, c) -> ResultSetUtils.getString(rs, c);
    public static final ColumnHandler<Date> DATE = (rs, c) -> ResultSetUtils.getTimestamp(rs, c);
    public static final ColumnHandler<byte[]> BYTE_ARRAY = (rs, c) -> ResultSetUtils.getBytes(rs, c);
    public static final ColumnHandler<BigDecimal> BIG_DECIMAL = (rs, c) -> ResultSetUtils.getBigDecimal(rs, c);

    public static final ColumnHandler<Boolean> BOOLEAN = (rs, c) -> ResultSetUtils.getBoolean(rs, c);
    public static final ColumnHandler<Byte> BYTE = (rs, c) -> ResultSetUtils.getByte(rs, c);
    public static final ColumnHandler<Short> SHORT = (rs, c) -> ResultSetUtils.getShort(rs, c);
    public static final ColumnHandler<Integer> INTEGER = (rs, c) -> ResultSetUtils.getInteger(rs, c);
    public static final ColumnHandler<Long> LONG = (rs, c) -> ResultSetUtils.getLong(rs, c);
    public static final ColumnHandler<Float> FLOAT = (rs, c) -> ResultSetUtils.getFloat(rs, c);
    public static final ColumnHandler<Double> DOUBLE = (rs, c) -> ResultSetUtils.getDouble(rs, c);

    public static final ColumnHandler<Boolean> PBOOLEAN = (rs, c) -> rs.getBoolean(c);
    public static final ColumnHandler<Byte> PBYTE = (rs, c) -> rs.getByte(c);
    public static final ColumnHandler<Short> PSHORT = (rs, c) -> rs.getShort(c);
    public static final ColumnHandler<Integer> PINTEGER = (rs, c) -> rs.getInt(c);
    public static final ColumnHandler<Long> PLONG = (rs, c) -> rs.getLong(c);
    public static final ColumnHandler<Float> PFLOAT = (rs, c) -> rs.getFloat(c);
    public static final ColumnHandler<Double> PDOUBLE = (rs, c) -> rs.getDouble(c);

    static final Map<Class<?>, ColumnHandler<?>> HANDLERS = new LinkedHashMap<>();
    static {
        HANDLERS.put(String.class, STRING);
        HANDLERS.put(Date.class, DATE);
        HANDLERS.put(byte[].class, BYTE_ARRAY);
        HANDLERS.put(BigDecimal.class, BIG_DECIMAL);

        HANDLERS.put(Boolean.class, BOOLEAN);
        HANDLERS.put(Byte.class, BYTE);
        HANDLERS.put(Short.class, SHORT);
        HANDLERS.put(Integer.class, INTEGER);
        HANDLERS.put(Long.class, LONG);
        HANDLERS.put(Float.class, FLOAT);
        HANDLERS.put(Double.class, DOUBLE);

        HANDLERS.put(boolean.class, PBOOLEAN);
        HANDLERS.put(byte.class, PBYTE);
        HANDLERS.put(short.class, PSHORT);
        HANDLERS.put(int.class, PINTEGER);
        HANDLERS.put(long.class, PLONG);
        HANDLERS.put(float.class, PFLOAT);
        HANDLERS.put(double.class, PDOUBLE);
    }

    /**
     * Els tipus aquí contemplats especifiquen els que poden tenir les propietats de
     * les entitats a tractar. Per a algun tipus diferent, anotar amb
     * {@link CustomHandler}.
     */
    @SuppressWarnings("unchecked")
    public static <T> ColumnHandler<T> getHandlerFor(Class<T> type) {
        if (!HANDLERS.containsKey(type)) {
            throw new JdbcException("unsupported column type: " + type.getName() + ": please specify a concrete "
                    + ColumnHandler.class.getName());
        }
        return (ColumnHandler<T>) HANDLERS.get(type);
    }

}