package org.lenteja.mapper.autogen.impl;

import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;
import org.lenteja.mapper.Column;
import org.lenteja.mapper.autogen.Generator;

public class OracleSequence<T> extends Generator<T> {

    final String sequenceName;

    public OracleSequence(Column<?, T> column, String sequenceName) {
        super(column, true);
        this.sequenceName = sequenceName;
    }

    @Override
    protected IQueryObject getQuery() {
        QueryObject q = new QueryObject();
        q.append("select " + sequenceName + ".nextval from dual");
        return q;
    }
}
