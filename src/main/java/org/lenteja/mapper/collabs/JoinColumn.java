package org.lenteja.mapper.collabs;

import org.lenteja.Column;
import org.lenteja.jdbc.query.IQueryObject;

public class JoinColumn<S, R, C> {

    public final Column<S, C> selfColumn;
    public final Column<R, C> refColumn;

    public JoinColumn(Column<S, C> selfColumn, Column<R, C> refColumn) {
        super();
        this.selfColumn = selfColumn;
        this.refColumn = refColumn;
    }

    @SuppressWarnings("unchecked")
    public IQueryObject getRestriction() {
        Column<S, Object> selfc = (Column<S, Object>) selfColumn;
        Column<R, Object> refc = (Column<R, Object>) refColumn;
        return refc.eq(selfc);
    }
}