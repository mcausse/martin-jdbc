package org.lenteja.mapper.autogen;

import org.lenteja.Column;
import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.mapper.query.Operations;

public abstract class Generator<T> {

    final ScalarHandler<T> handler;

    public Generator(Column<?, T> column) {
        super();
        this.handler = ScalarMappers.getScalarMapperFor(column.getColumnClass());
    }

    public T generate(DataAccesFacade facade) {
        return new Operations().query(handler).append(getQuery()).getExecutor(facade).loadUnique();
    }

    protected abstract IQueryObject getQuery();

}
