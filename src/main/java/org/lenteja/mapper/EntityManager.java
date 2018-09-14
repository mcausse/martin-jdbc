package org.lenteja.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.exception.JdbcException;
import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.mapper.autogen.Generator;
import org.lenteja.mapper.autogen.ScalarMappers;
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

    // TODO testar
    @SuppressWarnings("unchecked")
    public <E, ID> E loadById(Table<E> table, ID id) {
        List<IQueryObject> where = new ArrayList<>();
        for (Column<E, ?> c : table.getColumns()) {
            if (c.isPk()) {
                Object pkValue = c.getAccessor().get(id, 1);
                Column<E, Object> c2 = (Column<E, Object>) c;
                where.add(c2.eq(pkValue));
            }
        }

        Query<E> q = queryFor(table).append("select {} from {} where {}", table.all(), table, Relational.and(where));
        return q.getExecutor(facade).loadUnique();
    }

    /**
     * fa un {@link #insert(Object)} o un {@link #update(Object)}, segons convingui.
     *
     * <pre>
     * si almenys una PK és Autogen:
     *         si alguna PK no-Autogen té valor null => error
     *         insert: alguna PK val null
     *         update: cap PK val null
     * sino
     *         si almenys una PK està a null =&gt; error
     *
     *         si exist()
     *             update()
     *         sino
     *             insert()
     *         fisi
     * fisi
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public <E> void store(Table<E> table, E entity) {

        /**
         * si una propietat PK és primitiva, el seu valor mai serà null (p.ex. serà 0) i
         * l'store() no funcionarà. Si es té una PK primitiva, usar insert()/update() en
         * comptes d'store().
         */
        for (Column<E, ?> p : table.getPkColumns()) {
            if (p.getColumnClass().isPrimitive()) {
                throw new JdbcException(
                        "PK column is mapped as of primitive type: use insert()/update() instead of store(): "
                                + entity.getClass().getSimpleName() + "#" + p.getColumnName());
            }
        }

        boolean algunaPkAutogen = false;
        {
            List<Generator<?>> autogens = table.getAutoGens();
            for (Generator<?> g : autogens) {
                if (g.getColumn().isPk()) {
                    algunaPkAutogen = true;
                    break;
                }
            }
        }

        Set<Column<E, ?>> pks = new LinkedHashSet<>(table.getPkColumns());

        /**
         * si almenys una PK és Autogen:
         */
        if (algunaPkAutogen) {

            Set<Column<E, ?>> autogens = new LinkedHashSet<>();
            for (Generator<?> g : table.getAutoGens()) {
                autogens.add((Column<E, ?>) g.getColumn());
            }
            Set<Column<E, ?>> pksNoAutoGens = new LinkedHashSet<>(pks);
            pksNoAutoGens.removeAll(autogens);

            /**
             * si alguna PK no-Autogen té valor null => error
             */
            for (Column<E, ?> c : pksNoAutoGens) {
                if (c.getAccessor().get(entity) == null) {
                    throw new JdbcException("una propietat PK no-autogenerada té valor null en store(): "
                            + entity.getClass().getSimpleName() + "#" + c.getColumnName());
                }
            }

            boolean algunaPkValNull = false;
            for (Column<E, ?> c : pks) {
                if (c.getAccessor().get(entity) == null) {
                    algunaPkValNull = true;
                    break;
                }
            }

            if (algunaPkValNull) {
                /**
                 * insert: alguna PK val null
                 */
                insert(table, entity);
            } else {
                /**
                 * update: cap PK val null
                 */
                update(table, entity);
            }

        } else {

            /**
             * <pre>
             *         si almenys una PK està a null =&gt; error
             *
             *         si exist()
             *             update()
             *         sino
             *             insert()
             *         fisi
             * </pre>
             */
            for (Column<E, ?> c : pks) {
                if (c.getAccessor().get(entity) == null) {
                    throw new JdbcException("una propietat PK no-autogenerada té valor null en store(): "
                            + entity.getClass().getSimpleName() + "#" + c.getColumnName());
                }
            }

            if (exists(table, entity)) {
                update(table, entity);
            } else {
                insert(table, entity);
            }
        }
    }

    public <E> boolean exists(Table<E> table, E entity) {
        IQueryObject q = o.exists(table, entity);
        long count = facade.loadUnique(q, ScalarMappers.LONG);
        return count > 0L;
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

    public <E> Query<E> queryFor(Table<E> table) {
        return o.query(table);
    }

    public <C> Query<C> scalarQueryFor(Column<?, C> column) {
        return o.query(column);
    }

    // ===========================================

    // // TODO
    // publFSic <E> E loadById()
    // // TODO

    /**
     * @param orders usar {@link Sort#by(Order...)}
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

    // public <E> List<E> query(Table<E> table, List<Order<E>> orders) {
    // return query(table, Relational.all(), orders);
    // }

    public <E> List<E> query(Table<E> table, IQueryObject restriction) {
        return query(table, restriction, Collections.emptyList());
    }

    public <E> E queryUnique(Table<E> table, IQueryObject restrictions) {
        Query<E> q = o.query(table);
        q.append("select * from {} where {}", table, restrictions);
        return q.getExecutor(facade).loadUnique();
    }

    /**
     * @param orders usar {@link Sort#by(Order...)}
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