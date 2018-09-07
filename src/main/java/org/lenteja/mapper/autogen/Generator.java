package org.lenteja.mapper.autogen;

import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.mapper.Column;
import org.lenteja.mapper.query.Operations;

public abstract class Generator<T> {

    final Column<?, T> column;
    final ScalarHandler<T> handler;
    final boolean beforeInsert;

    public Generator(Column<?, T> column, boolean beforeInsert) {
        super();
        this.column = column;
        this.handler = ScalarMappers.getScalarMapperFor(column.getColumnClass());
        this.beforeInsert = beforeInsert;
    }

    public Column<?, T> getColumn() {
        return column;
    }

    public T generate(DataAccesFacade facade) {
        return new Operations().query(handler).append(getQuery()).getExecutor(facade).loadUnique();
    }

    public boolean isBeforeInsert() {
        return beforeInsert;
    }

    protected abstract IQueryObject getQuery();

}
