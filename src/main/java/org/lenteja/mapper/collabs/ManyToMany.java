package org.lenteja.mapper.collabs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.mapper.Column;
import org.lenteja.mapper.query.Operations;
import org.lenteja.mapper.query.Order;
import org.lenteja.mapper.query.Query;
import org.lenteja.mapper.query.Restrictions;

public class ManyToMany<S, I, R> {

    final OneToMany<S, I> oneToMany;
    final ManyToOne<I, R> manyToOne;

    public ManyToMany(OneToMany<S, I> oneToMany, ManyToOne<I, R> manyToOne) {
        super();
        this.oneToMany = oneToMany;
        this.manyToOne = manyToOne;
    }

    public List<R> fetch(DataAccesFacade facade, S entity) {
        return fetch(facade, entity, Collections.emptyList());
    }

    @SuppressWarnings("unchecked")
    public List<R> fetch(DataAccesFacade facade, S entity, List<Order<R>> orders) {
        Operations ops = new Operations();

        List<IQueryObject> restrictions = new ArrayList<>();
        for (JoinColumn<S, I, ?> jc : oneToMany.getJoinColumns()) {
            Column<S, Object> selfc = (Column<S, Object>) jc.getSelfColumn();
            Column<I, Object> refc = (Column<I, Object>) jc.getRefColumn();
            restrictions.add(refc.eq(selfc.getAccessor().get(entity)));
        }

        List<IQueryObject> onRestrictions = new ArrayList<>();
        for (JoinColumn<I, R, ?> jc : manyToOne.getJoinColumns()) {
            Column<I, Object> selfc = (Column<I, Object>) jc.getSelfColumn();
            Column<R, Object> refc = (Column<R, Object>) jc.getRefColumn();
            onRestrictions.add(refc.eq(selfc));
        }

        Query<R> q = ops.query(manyToOne.getRefTable()) //
                .append("select {} ", manyToOne.getRefTable().all()) //
                .append("from {} ", manyToOne.getSelfTable()) //
                .append("join {} ", manyToOne.getRefTable()) //
                .append("on {} ", Restrictions.and(onRestrictions)) //
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

    public OneToMany<S, I> getOneToMany() {
        return oneToMany;
    }

    public ManyToOne<I, R> getManyToOne() {
        return manyToOne;
    }

}
