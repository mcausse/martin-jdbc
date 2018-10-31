package org.lenteja.mapper.collabs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.mapper.Column;
import org.lenteja.mapper.Table;
import org.lenteja.mapper.query.Operations;
import org.lenteja.mapper.query.Order;
import org.lenteja.mapper.query.Query;
import org.lenteja.mapper.query.Restrictions;

public class OneToMany<S, R> {

    final Table<S> selfTable;
    final Table<R> refTable;
    final JoinColumn<S, R, ?>[] joinColumns;

    @SafeVarargs
    public OneToMany(Table<S> selfTable, Table<R> refTable, JoinColumn<S, R, ?>... joinColumns) {
        super();
        this.selfTable = selfTable;
        this.refTable = refTable;
        this.joinColumns = joinColumns;
    }

    public List<R> fetch(DataAccesFacade facade, S entity) {
        return fetch(facade, entity, Collections.emptyList());
    }

    @SuppressWarnings("unchecked")
    public List<R> fetch(DataAccesFacade facade, S entity, List<Order<R>> orders) {
        Operations ops = new Operations();

        List<IQueryObject> restrictions = new ArrayList<>();
        for (JoinColumn<S, R, ?> jc : joinColumns) {

            Column<S, Object> selfc = (Column<S, Object>) jc.getSelfColumn();
            Column<R, Object> refc = (Column<R, Object>) jc.getRefColumn();
            restrictions.add(refc.eq(selfc.getAccessor().get(entity)));
        }

        Query<R> q = ops.query(refTable) //
                .append("select {} from {} ", refTable.all(), refTable) //
                .append("where {}", Restrictions.and(restrictions));

        if (!orders.isEmpty()) {
            q.append(" order by ");
            List<IQueryObject> qs = new ArrayList<>();
            for (Order<R> o : orders) {
                qs.add(o);
            }
            q.append(Restrictions.list(qs));
        }
        return q.getExecutor(facade).load();
    }

    public Map<S, List<R>> fetch(DataAccesFacade facade, Iterable<S> entities) {
        return fetch(facade, entities, Collections.emptyList());
    }

    public Map<S, List<R>> fetch(DataAccesFacade facade, Iterable<S> entities, List<Order<R>> orders) {
        Map<S, List<R>> r = new LinkedHashMap<>();
        for (S e : entities) {
            r.put(e, fetch(facade, e, orders));
        }
        return r;
    }

    public Table<S> getSelfTable() {
        return selfTable;
    }

    public Table<R> getRefTable() {
        return refTable;
    }

    public JoinColumn<S, R, ?>[] getJoinColumns() {
        return joinColumns;
    }

}