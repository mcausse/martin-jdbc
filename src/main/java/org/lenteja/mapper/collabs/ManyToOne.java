package org.lenteja.mapper.collabs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.mapper.Column;
import org.lenteja.mapper.Table;
import org.lenteja.mapper.query.Operations;
import org.lenteja.mapper.query.Restrictions;

public class ManyToOne<S, R> {

    final Table<S> selfTable;
    final Table<R> refTable;
    final JoinColumn<S, R, ?>[] joinColumns;

    @SafeVarargs
    public ManyToOne(Table<S> selfTable, Table<R> refTable, JoinColumn<S, R, ?>... joinColumns) {
        super();
        this.selfTable = selfTable;
        this.refTable = refTable;
        this.joinColumns = joinColumns;
    }

    @SuppressWarnings("unchecked")
    public R fetch(DataAccesFacade facade, S entity) {
        Operations o = new Operations();

        List<IQueryObject> onRestrictions = new ArrayList<>();
        for (JoinColumn<S, R, ?> jc : joinColumns) {
            onRestrictions.add(jc.getRestriction());
        }

        List<IQueryObject> whereRestrictions = new ArrayList<>();
        for (Column<S, ?> selfc : selfTable.getColumns()) {
            if (selfc.isPk()) {
                Object whereValue = selfc.getAccessor().get(entity);
                Column<S, Object> selfColumn = (Column<S, Object>) selfc;
                whereRestrictions.add(selfColumn.eq(whereValue));
            }
        }

        return o.query(refTable) //
                .append("select * from {} ", refTable) //
                .append("join {} ", selfTable) //
                .append("on {} ", Restrictions.and(onRestrictions)) //
                .append("where {}", Restrictions.and(whereRestrictions)) //
                .getExecutor(facade) //
                .loadUnique();
    }

    public Map<S, R> fetch(DataAccesFacade facade, Iterable<S> entities) {
        Map<S, R> r = new LinkedHashMap<>();
        for (S e : entities) {
            r.put(e, fetch(facade, e));
        }
        return r;
    }
}