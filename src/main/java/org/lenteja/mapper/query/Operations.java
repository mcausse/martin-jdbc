package org.lenteja.mapper.query;

import java.util.StringJoiner;

import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;
import org.lenteja.mapper.Column;
import org.lenteja.mapper.Mapable;
import org.lenteja.mapper.Table;

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
        QueryObject q = new QueryObject();
        q.append("update ");
        q.append(table.getTableName());
        q.append(" set ");
        {
            StringJoiner j = new StringJoiner(", ");
            for (Column<E, ?> c : table.getColumns()) {
                if (!c.isPk()) {
                    j.add(c.getColumnName() + "=?");
                    q.addArg(c.storeValue(entity));
                }
            }
            q.append(j.toString());
        }
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
}
