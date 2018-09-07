package org.lenteja.mapper.collabs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.mapper.Table;
import org.lenteja.mapper.query.Operations;
import org.lenteja.mapper.query.Relational;

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
        Operations o = new Operations();

        List<IQueryObject> restrictions = new ArrayList<>();
        for (JoinColumn<S, R, ?> jc : joinColumns) {
            restrictions.add(jc.getRestriction());
        }

        return o.query(refTable) //
                .append("select * from {} ", refTable) //
                .append("where {}", Relational.and(restrictions)) //
                .getExecutor(facade) //
                .load();
    }

    public Map<S, List<R>> fetch(DataAccesFacade facade, Iterable<S> entities) {
        Map<S, List<R>> r = new LinkedHashMap<>();
        for (S e : entities) {
            r.put(e, fetch(facade, e));
        }
        return r;
    }
}