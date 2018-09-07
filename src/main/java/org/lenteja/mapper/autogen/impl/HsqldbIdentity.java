package org.lenteja.mapper.autogen.impl;

import org.lenteja.Column;
import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;
import org.lenteja.mapper.autogen.Generator;

public class HsqldbIdentity<T> extends Generator<T> {

    public HsqldbIdentity(Column<?, T> column) {
        super(column);
    }

    @Override
    protected IQueryObject getQuery() {
        QueryObject q = new QueryObject();
        q.append("call identity()");
        return q;
    }
}