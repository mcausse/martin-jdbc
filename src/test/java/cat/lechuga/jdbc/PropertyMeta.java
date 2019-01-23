package cat.lechuga.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.lenteja.mapper.autogen.Generator;
import org.lenteja.mapper.handler.ColumnHandler;

import cat.lechuga.jdbc.reflect.Property;

public class PropertyMeta {

    private final Property prop;

    private final String columnName;
    private final boolean isId;
    private final ColumnHandler handler;
    private final Generator generator;

    public PropertyMeta(Property prop, String columnName, boolean isId, ColumnHandler handler, Generator generator) {
        super();
        this.prop = prop;
        this.columnName = columnName;
        this.isId = isId;
        this.handler = handler;
        this.generator = generator;
    }

    public Object getJdbcValue(Object entity) {
        Object value = prop.get(entity);
        return handler.getJdbcValue(value);
    }

    public Object getJdbcValue(int propertyOffset, Object entity) {
        Object value = prop.get(propertyOffset, entity);
        return handler.getJdbcValue(value);
    }

    public void readValue(Object entity, ResultSet rs) throws SQLException {
        Object value = handler.readValue(rs, getColumnName());
        prop.set(entity, value);
    }

    public Property getProp() {
        return prop;
    }

    public String getColumnName() {
        return columnName;
    }

    public boolean isId() {
        return isId;
    }

    public ColumnHandler getHandler() {
        return handler;
    }

    public Generator getGenerator() {
        return generator;
    }

    @Override
    public String toString() {
        return "PropertyMeta [prop=" + prop + ", columnName=" + columnName + ", isId=" + isId + ", handler=" + handler
                + ", generator=" + generator + "]";
    }

}