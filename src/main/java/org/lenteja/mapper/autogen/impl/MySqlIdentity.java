package org.lenteja.mapper.autogen.impl;

import org.lenteja.Column;
import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;
import org.lenteja.mapper.autogen.Generator;

public class MySqlIdentity<T> extends Generator<T> {

    public MySqlIdentity(Column<?, T> column) {
        super(column);
    }

    @Override
    protected IQueryObject getQuery() {
        QueryObject q = new QueryObject();
        q.append("select last_insert_id()");
        return q;
    }
}
