package org.lenteja.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.mapper.autogen.Generator;
import org.lenteja.mapper.query.Operations;
import org.lenteja.mapper.query.Order;
import org.lenteja.mapper.query.Query;
import org.lenteja.mapper.query.Relational;

public class EntityManager {

    final DataAccesFacade facade;
    final Operations o = new Operations();

    public EntityManager(DataAccesFacade facade) {
        super();
        this.facade = facade;
    }

    public <E> void store(Table<E> table, E entity) {
        // TODO
    }

    public <E> void insert(Table<E> table, E entity) {

        for (Generator<?> ag : table.getAutoGens()) {
            if (ag.isBeforeInsert()) {
                Object autoGeneratedVal = ag.generate(facade);
                ag.getColumn().getAccessor().set(entity, autoGeneratedVal);
            }
        }

        IQueryObject q = o.insert(table, entity);
        facade.update(q);

        for (Generator<?> ag : table.getAutoGens()) {
            if (!ag.isBeforeInsert()) {
                Object autoGeneratedVal = ag.generate(facade);
                ag.getColumn().getAccessor().set(entity, autoGeneratedVal);
            }
        }
    }

    public <E> void update(Table<E> table, E entity) {
        IQueryObject q = o.update(table, entity);
        facade.update(q);
    }

    public <E> void delete(Table<E> table, E entity) {
        IQueryObject q = o.delete(table, entity);
        facade.update(q);
    }

    // ===========================================

    public <E> void storeAll(Table<E> table, Iterable<E> entities) {
        for (E entity : entities) {
            store(table, entity);
        }
    }

    public <E> void insertAll(Table<E> table, Iterable<E> entities) {
        for (E entity : entities) {
            insert(table, entity);
        }
    }

    public <E> void updateAll(Table<E> table, Iterable<E> entities) {
        for (E entity : entities) {
            update(table, entity);
        }
    }

    public <E> void deleteAll(Table<E> table, Iterable<E> entities) {
        for (E entity : entities) {
            delete(table, entity);
        }
    }

    // ===========================================

    public <E> Query<E> query(Table<E> table) {
        return o.query(table);
    }

    public <C> Query<C> scalarQuery(Column<?, C> column) {
        return o.query(column);
    }

    // ===========================================

    // TODO
    /**
     * @param orders
     *            usar {@link Sort#by(Order...)}
     */
    public <E> List<E> query(Table<E> table, IQueryObject restriction, List<Order<E>> orders) {
        Query<E> q = o.query(table);
        q.append("select * from {} where {}", table, restriction);
        if (!orders.isEmpty()) {
            q.append(" order by ");
            List<IQueryObject> qs = new ArrayList<>();
            for (Order<E> o : orders) {
                qs.add(o);
            }
            q.append(Relational.list(qs));
        }
        return q.getExecutor(facade).load();
    }

    public <E> List<E> query(Table<E> table, IQueryObject restriction) {
        return query(table, restriction, Collections.emptyList());
    }

    // TODO
    public <E> E queryUnique(Table<E> table, IQueryObject restrictions) {
        Query<E> q = o.query(table);
        q.append("select * from {} where {}", table, restrictions);
        return q.getExecutor(facade).loadUnique();
    }

    // TODO query by example
    /**
     * @param orders
     *            usar {@link Sort#by(Order...)}
     */
    @SuppressWarnings("unchecked")
    public <E> List<E> query(Table<E> table, E example, List<Order<E>> orders) {
        List<IQueryObject> restrictions = new ArrayList<>();
        for (Column<E, ?> column : table.getColumns()) {
            Column<E, Object> c = (Column<E, Object>) column;
            Object value = c.getAccessor().get(example);
            if (value != null) {
                restrictions.add(c.eq(value));
            }
        }
        return query(table, Relational.and(restrictions), orders);
    }

    public <E> List<E> query(Table<E> table, E example) {
        return query(table, example, Collections.emptyList());
    }

    // TODO query by example
    @SuppressWarnings("unchecked")
    public <E> E queryUnique(Table<E> table, E example) {
        List<IQueryObject> restrictions = new ArrayList<>();
        for (Column<E, ?> column : table.getColumns()) {
            Column<E, Object> c = (Column<E, Object>) column;
            Object value = c.getAccessor().get(example);
            if (value != null) {
                restrictions.add(c.eq(value));
            }
        }
        return queryUnique(table, Relational.and(restrictions));
    }
}