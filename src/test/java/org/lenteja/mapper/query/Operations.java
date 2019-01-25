package org.lenteja.mapper.query;

import java.util.StringJoiner;

import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;
import org.lenteja.mapper.Column;
import org.lenteja.mapper.Table;
import org.lenteja.mapper.handler.ColumnHandler;

import cat.lechuga.Mapable;

public class Operations {

    public <E> Query<E> query(Mapable<E> mapable) {
        return new Query<>(mapable);
    }

    public <E> IQueryObject insert(Table<E> table, E entity) {
        QueryObject q = new QueryObject();
        q.append("insert into ");
        q.append(table.getTableName());
        q.append(" (");
        {
            StringJoiner j = new StringJoiner(", ");
            for (Column<E, ?> c : table.getColumns()) {
                j.add(c.getColumnName());
            }
            q.append(j.toString());
        }
        q.append(") values (");
        {
            StringJoiner j = new StringJoiner(", ");
            for (Column<E, ?> c : table.getColumns()) {
                j.add("?");
                q.addArg(c.storeValue(entity));
            }
            q.append(j.toString());
        }
        q.append(")");
        return q;
    }

    public <E> IQueryObject update(Table<E> table, E entity) {
        return update(table, entity, table.getNonPkColumns());
    }

    public <E> IQueryObject update(Table<E> table, E entity, Iterable<Column<E, ?>> columnsToUpdate) {

        QueryObject wherePredicate;
        {
            wherePredicate = new QueryObject();
            StringJoiner j = new StringJoiner(" and ");
            for (Column<E, ?> c : table.getColumns()) {
                if (c.isPk()) {
                    j.add(c.getColumnName() + "=?");
                    wherePredicate.addArg(c.storeValue(entity));
                }
            }
            wherePredicate.append(j.toString());
        }
        return update(table, entity, columnsToUpdate, wherePredicate);
    }

    public <E> IQueryObject update(Table<E> table, E example, Iterable<Column<E, ?>> columnsToUpdate,
            IQueryObject wherePredicate) {

        QueryObject q = new QueryObject();
        q.append("update ");
        q.append(table.getTableName());
        q.append(" set ");
        {
            StringJoiner j = new StringJoiner(", ");
            for (Column<E, ?> c : columnsToUpdate) {
                if (c.isPk()) {
                    throw new RuntimeException("cannot update PK column: " + c.getColumnName());
                } else {
                    j.add(c.getColumnName() + "=?");
                    q.addArg(c.storeValue(example));
                }
            }
            q.append(j.toString());
        }
        q.append(" where ");
        q.append(wherePredicate);
        return q;
    }

    public <E> IQueryObject delete(Table<E> table, E entity) {
        QueryObject q = new QueryObject();
        q.append("delete from ");
        q.append(table.getTableName());
        q.append(" where ");
        {
            StringJoiner j = new StringJoiner(" and ");
            for (Column<E, ?> c : table.getColumns()) {
                if (c.isPk()) {
                    j.add(c.getColumnName() + "=?");
                    q.addArg(c.storeValue(entity));
                }
            }
            q.append(j.toString());
        }
        return q;
    }

    public <E> IQueryObject delete(Table<E> table, IQueryObject wherePredicate) {
        QueryObject q = new QueryObject();
        q.append("delete from ");
        q.append(table.getTableName());
        q.append(" where ");
        q.append(wherePredicate);
        return q;
    }

    public <E> IQueryObject exists(Table<E> table, E entity) {
        QueryObject q = new QueryObject();
        q.append("select count(*) from ");
        q.append(table.getTableName());
        q.append(" where ");
        {
            StringJoiner j = new StringJoiner(" and ");
            for (Column<E, ?> c : table.getColumns()) {
                if (c.isPk()) {
                    j.add(c.getColumnName() + "=?");
                    q.addArg(c.storeValue(entity));
                }
            }
            q.append(j.toString());
        }
        return q;
    }

    @SuppressWarnings("unchecked")
    public <E> IQueryObject existsById(Table<E> table, Object id) {
        QueryObject q = new QueryObject();
        q.append("select count(*) from ");
        q.append(table.getTableName());
        q.append(" where ");
        {
            StringJoiner j = new StringJoiner(" and ");
            for (Column<E, ?> c : table.getColumns()) {
                if (c.isPk()) {
                    j.add(c.getColumnName() + "=?");
                    Object value = c.getAccessor().get(id, 1);
                    ColumnHandler<Object> handler = (ColumnHandler<Object>) c.getHandler();
                    q.addArg(handler.getJdbcValue(value));
                }
            }
            q.append(j.toString());
        }
        return q;
    }

}
