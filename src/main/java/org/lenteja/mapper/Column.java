package org.lenteja.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;
import org.lenteja.mapper.handler.ColumnHandler;
import org.lenteja.mapper.query.ELike;
import org.lenteja.mapper.query.Restrictions;
import org.lenteja.mapper.reflect.Accessor;

public class Column<E, T> implements Aliasable, Mapable<T> {

    final Table<E> parentTable;
    final Class<T> columnClass;
    final String columnName;
    final boolean isPk;
    final ColumnHandler<T> handler;
    final Accessor accessor;

    public Column(Table<E> parentTable, Class<T> columnClass, String propertyPath, String columnName, boolean isPk,
            ColumnHandler<T> handler) {
        super();
        this.parentTable = parentTable;
        this.columnClass = columnClass;
        this.accessor = new Accessor(parentTable.getEntityClass(), propertyPath);
        this.columnName = columnName;
        this.isPk = isPk;
        this.handler = handler;
    }

    public Class<T> getColumnClass() {
        return columnClass;
    }

    public boolean isPk() {
        return isPk;
    }

    public String getColumnName() {
        return columnName;
    }

    public Accessor getAccessor() {
        return accessor;
    }

    @Override
    public String getAliasedName() {
        if (parentTable.getAlias() == null) {
            return columnName;
        } else {
            return parentTable.getAlias() + "." + columnName;
        }
    }

    @Override
    public T map(ResultSet rs) throws SQLException {
        return handler.readValue(rs, columnName);
    }

    public void loadValue(E entity, ResultSet rs) throws SQLException {
        T value = handler.readValue(rs, columnName);
        accessor.set(entity, value);
    }

    @SuppressWarnings("unchecked")
    public Object storeValue(E entity) {
        T value = (T) accessor.get(entity);
        return handler.getJdbcValue(value);
    }

    protected IQueryObject binaryOp(String op, T value) {
        QueryObject q = new QueryObject();
        q.append(getAliasedName());
        q.append(op);
        q.append("?");
        q.addArg(handler.getJdbcValue(value));
        return q;
    }

    public IQueryObject eq(T value) {
        return binaryOp("=", value);
    }

    public IQueryObject ne(T value) {
        return binaryOp("<>", value);
    }

    public IQueryObject lt(T value) {
        return binaryOp("<", value);
    }

    public IQueryObject gt(T value) {
        return binaryOp(">", value);
    }

    public IQueryObject le(T value) {
        return binaryOp("<=", value);
    }

    public IQueryObject ge(T value) {
        return binaryOp(">=", value);
    }

    //

    protected IQueryObject binaryOp(String op, Column<?, T> c) {
        QueryObject q = new QueryObject();
        q.append(getAliasedName());
        q.append(op);
        q.append(c.getAliasedName());
        return q;
    }

    public IQueryObject eq(Column<?, T> c) {
        return binaryOp("=", c);
    }

    public IQueryObject ne(Column<?, T> c) {
        return binaryOp("<>", c);
    }

    public IQueryObject lt(Column<?, T> c) {
        return binaryOp("<", c);
    }

    public IQueryObject gt(Column<?, T> c) {
        return binaryOp(">", c);
    }

    public IQueryObject le(Column<?, T> c) {
        return binaryOp("<=", c);
    }

    public IQueryObject ge(Column<?, T> c) {
        return binaryOp(">=", c);
    }

    //

    protected IQueryObject unaryOp(String prefix, String postfix) {
        QueryObject q = new QueryObject();
        q.append(prefix);
        q.append(getAliasedName());
        q.append(postfix);
        return q;
    }

    public IQueryObject isNull() {
        return unaryOp("", " IS NULL");
    }

    public IQueryObject isNotNull() {
        return unaryOp("", " IS NOT NULL");
    }

    //

    public IQueryObject in(List<T> values) {

        QueryObject r = new QueryObject();
        r.append(getAliasedName());
        r.append(" in (");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                r.append(",");
            }
            r.append("?");
            r.addArg(handler.getJdbcValue(values.get(i)));
        }
        r.append(")");
        return r;
    }

    @SuppressWarnings("unchecked")
    public IQueryObject in(T... values) {
        return in(Arrays.asList(values));
    }

    public IQueryObject notIn(List<T> values) {
        return Restrictions.not(in(values));
    }

    @SuppressWarnings("unchecked")
    public IQueryObject notIn(T... values) {
        return Restrictions.not(in(Arrays.asList(values)));
    }

    public IQueryObject between(T value1, T value2) {
        QueryObject r = new QueryObject();
        r.append(getAliasedName());
        r.append(" between ? and ?");
        r.addArg(handler.getJdbcValue(value1));
        r.addArg(handler.getJdbcValue(value2));
        return r;
    }

    public IQueryObject like(ELike like, String value) {
        QueryObject r = new QueryObject();
        r.append(getAliasedName());
        r.append(" like ?");
        r.addArg(like.process(value));
        return r;
    }

    public IQueryObject ilike(ELike like, String value) {
        QueryObject r = new QueryObject();
        r.append("upper(");
        r.append(getAliasedName());
        r.append(") like upper(?)");
        r.addArg(like.process(value));
        return r;
    }

}