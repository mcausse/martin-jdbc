package org.lenteja.mapper.autogen.impl;

import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;
import org.lenteja.mapper.Column;
import org.lenteja.mapper.autogen.Generator;

public class HsqldbSequence<T> extends Generator<T> {

    final String sequenceName;

    public HsqldbSequence(Column<?, T> column, String sequenceName) {
        super(column, true);
        this.sequenceName = sequenceName;
    }

    @Override
    protected IQueryObject getQuery() {
        QueryObject q = new QueryObject();
        q.append("call next value for " + sequenceName);
        return q;
    }
}
