package org.lenteja;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Table<E> implements Aliasable, Mapable<E> {

    final String alias;
    final Class<E> entityClass;
    final String tableName;
    final List<Column<E, ?>> columns;

    public Table(Class<E> entityClass, String tableName, String alias) {
        super();
        this.entityClass = entityClass;
        this.tableName = tableName;
        this.columns = new ArrayList<>();
        this.alias = alias;
    }

    public Table(Class<E> entityClass, String tableName) {
        this(entityClass, tableName, null);
    }

    protected <T> Column<E, T> addColumn(Class<T> columnClass, String propertyPath, String columnName) {
        Column<E, T> c = new Column<>(this, columnClass, propertyPath, columnName, false,
                Handlers.getHandlerFor(columnClass));
        this.columns.add(c);
        return c;
    }

    protected <T> Column<E, T> addPkColumn(Class<T> columnClass, String propertyPath, String columnName) {
        Column<E, T> c = new Column<>(this, columnClass, propertyPath, columnName, true,
                Handlers.getHandlerFor(columnClass));
        this.columns.add(c);
        return c;
    }

    protected <T> Column<E, T> addColumn(Class<T> columnClass, String propertyPath, String columnName,
            ColumnHandler<T> handler) {
        Column<E, T> c = new Column<>(this, columnClass, propertyPath, columnName, false, handler);
        this.columns.add(c);
        return c;
    }

    protected <T> Column<E, T> addPkColumn(Class<T> columnClass, String propertyPath, String columnName,
            ColumnHandler<T> handler) {
        Column<E, T> c = new Column<>(this, columnClass, propertyPath, columnName, true, handler);
        this.columns.add(c);
        return c;
    }

    public Class<E> getEntityClass() {
        return entityClass;
    }

    public String getTableName() {
        return tableName;
    }

    public List<Column<E, ?>> getColumns() {
        return columns;
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public String getAliasedName() {
        if (alias == null) {
            return tableName;
        } else {
            return tableName + " " + alias;
        }
    }

    @Override
    public E map(ResultSet rs) throws SQLException {
        try {
            E r = entityClass.newInstance();
            for (Column<E, ?> c : columns) {
                c.loadValue(r, rs);
            }
            return r;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
