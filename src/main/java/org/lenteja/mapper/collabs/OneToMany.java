package org.lenteja.mapper.collabs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;
import org.lenteja.mapper.Column;
import org.lenteja.mapper.EntityManager;
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

        // validar que la part esquerra de joinColumns siguin columnes PK
        for (JoinColumn<S, R, ?> jc : joinColumns) {
            if (!jc.selfColumn.isPk()) {
                throw new RuntimeException("required a PK column, but received: " + jc.selfColumn.toString());
            }
        }
    }

    public List<R> fetchLazy(DataAccesFacade facade, S entity) {
        return fetchLazy(facade, entity, Collections.emptyList());
    }

    public List<R> fetchLazy(DataAccesFacade facade, S entity, List<Order<R>> orders) {
        Query<R> q = getFetchQuery(entity, orders);
        return new EntitiesLazyList<>(q.getExecutor(facade));
    }

    public List<R> fetch(DataAccesFacade facade, S entity) {
        return fetch(facade, entity, Collections.emptyList());
    }

    public List<R> fetch(DataAccesFacade facade, S entity, List<Order<R>> orders) {
        Query<R> q = getFetchQuery(entity, orders);
        return q.getExecutor(facade).load();
    }

    protected Query<R> getFetchQuery(S entity, List<Order<R>> orders) {
        Operations ops = new Operations();

        List<IQueryObject> restrictions = new ArrayList<>();
        for (JoinColumn<S, R, ?> jc : joinColumns) {
            restrictions.add(jc.getRestriction(entity));
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
        return q;
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

    // TODO i no es poden posar les relacions OneToMany-ManyToOne a la Table ????
    // sembla q no, dóna error de referències circulars en instanciar... i si és
    // static ??

    public static enum StoreOrphansStrategy {
        /**
         * delete orphans
         */
        DELETE,
        /**
         * set foreign keys as null of orphans
         */
        NULL,
        /**
         * do nothing (don't kill the orphans)
         */
        NOTHING;
    }

    /**
     * @return processed orphans number (depending of {@link StoreOrphansStrategy},
     *         in the case of {@link StoreOrphansStrategy#NOTHING} always return 0).
     */
    public int storeChilds(DataAccesFacade facade, S parentEntity, Iterable<R> childs) {
        return storeChilds(facade, parentEntity, childs, StoreOrphansStrategy.DELETE);
    }

    // TODO el param StoreOrphansStrategy hauria de ser un membre d'instància
    /**
     * @return processed orphans number (depending of {@link StoreOrphansStrategy},
     *         in the case of {@link StoreOrphansStrategy#NOTHING} always return 0).
     */
    @SuppressWarnings("unchecked")
    public int storeChilds(DataAccesFacade facade, S parentEntity, Iterable<R> childs,
            StoreOrphansStrategy orphansStrategy) {

        /**
         * informa les FK dels childs amb els valors PK del pare (seguint les
         * joinColumns definides)
         */
        for (JoinColumn<S, R, ?> jc : joinColumns) {
            Object parentPkValue = jc.selfColumn.getAccessor().get(parentEntity);
            for (R child : childs) {
                jc.refColumn.getAccessor().set(child, parentPkValue);
            }
        }

        EntityManager em = new EntityManager(facade);
        em.storeAll(refTable, childs);

        /**
         * delete orphans (other references, not included in this storation)
         */
        if (orphansStrategy == StoreOrphansStrategy.DELETE || orphansStrategy == StoreOrphansStrategy.NULL) {

            QueryObject where;
            {
                List<IQueryObject> eqs = new ArrayList<>();
                for (JoinColumn<S, R, ?> jc : joinColumns) {
                    Column<R, Object> refc = (Column<R, Object>) jc.refColumn;
                    Object v = jc.selfColumn.getAccessor().get(parentEntity);
                    eqs.add(refc.eq(v));
                }
                where = new QueryObject();
                where.append(Restrictions.and(eqs));
            }

            QueryObject notInq;
            {
                notInq = new QueryObject();
                StringJoiner pklist = new StringJoiner(",");
                for (Column<R, ?> pkc : refTable.getPkColumns()) {
                    pklist.add(pkc.getAliasedName());
                }
                notInq.append(pklist.toString());
            }

            QueryObject notIn;
            {
                notIn = new QueryObject();
                StringJoiner rows = new StringJoiner(",");
                for (R child : childs) {
                    StringJoiner row = new StringJoiner(",");
                    for (Column<R, ?> pkc : refTable.getPkColumns()) {
                        row.add("?");
                        notIn.addArg(pkc.storeValue(child));
                    }
                    rows.add("(" + row.toString() + ")");
                }
                notIn.append(rows.toString());
            }

            if (orphansStrategy == StoreOrphansStrategy.DELETE) {
                /**
                 * delete from TEX tex where (tex.ID_ENS=? and tex.ANY_EXP=? and tex.NUM_EXP=?)
                 * and (tex.ID_TEX) not in ((?),(?)) -- [8200(Long), 2018(Integer), 10(Long),
                 * 101(Long), 104(Long)]
                 */
                int orphansRemoved = em.queryFor(null) //
                        .append("delete from {} ", refTable) //
                        .append("where ({}) ", where) //
                        .append("and ({}) not in ({}) ", notInq, notIn) //
                        .getExecutor(facade) //
                        .update() //
                ;
                return orphansRemoved;
            } else if (orphansStrategy == StoreOrphansStrategy.NULL) {

                QueryObject setNull;
                {
                    setNull = new QueryObject();
                    StringJoiner pklist = new StringJoiner(",");
                    for (JoinColumn<S, R, ?> jc : joinColumns) {
                        pklist.add(jc.refColumn.getAliasedName() + "=?");
                        setNull.addArg(null);
                    }
                    setNull.append(pklist.toString());
                }

                int orphansRemoved = em.queryFor(null) //
                        .append("update {} ", refTable) //
                        .append("set {} ", setNull) //
                        .append("where ({}) ", where) //
                        .append("and ({}) not in ({}) ", notInq, notIn) //
                        .getExecutor(facade) //
                        .update() //
                ;
                return orphansRemoved;
            } else {
                throw new RuntimeException();
            }
        } else if (orphansStrategy == StoreOrphansStrategy.NOTHING) {
            return 0;
        } else {
            throw new RuntimeException();
        }
    }

    /**
     * @return the number of deleted childs
     */
    @SuppressWarnings("unchecked")
    public int deleteChilds(DataAccesFacade facade, S parentEntity) {

        QueryObject where;
        {
            List<IQueryObject> eqs = new ArrayList<>();
            for (JoinColumn<S, R, ?> jc : joinColumns) {
                Column<R, Object> refc = (Column<R, Object>) jc.refColumn;
                Object v = jc.selfColumn.getAccessor().get(parentEntity);
                eqs.add(refc.eq(v));
            }
            where = new QueryObject();
            where.append(Restrictions.and(eqs));
        }

        EntityManager em = new EntityManager(facade);

        int childsRemoved = em.queryFor(null) //
                .append("delete from {} ", refTable) //
                .append("where ({}) ", where) //
                .getExecutor(facade) //
                .update() //
        ;
        return childsRemoved;
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