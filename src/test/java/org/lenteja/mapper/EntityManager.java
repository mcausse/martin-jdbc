package org.lenteja.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.exception.EmptyResultException;
import org.lenteja.jdbc.exception.JdbcException;
import org.lenteja.jdbc.exception.TooManyResultsException;
import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.mapper.autogen.Generator;
import org.lenteja.mapper.query.Operations;
import org.lenteja.mapper.query.Order;
import org.lenteja.mapper.query.Query;
import org.lenteja.mapper.query.Restrictions;

import cat.lechuga.generator.ScalarMappers;

public class EntityManager {

    final DataAccesFacade facade;
    final Operations o = new Operations();

    public EntityManager(DataAccesFacade facade) {
        super();
        this.facade = facade;
    }

    @SuppressWarnings("unchecked")
    public <E> void refresh(Table<E> table, E entity) {

        List<IQueryObject> where = new ArrayList<>();
        for (Column<E, ?> c : table.getColumns()) {
            if (c.isPk()) {
                Object pkValue = c.getAccessor().get(entity);
                Column<E, Object> c2 = (Column<E, Object>) c;
                where.add(c2.eq(pkValue));
            }
        }

        E r = queryUnique(table, Restrictions.and(where));

        /**
         * refresca
         */
        for (Column<E, ?> c : table.getColumns()) {
            if (!c.isPk()) {
                Object value = c.getAccessor().get(r);
                c.getAccessor().set(entity, value);
            }
        }
    }

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

        return queryUnique(table, Restrictions.and(where));
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
        if (count > 1) {
            throw new TooManyResultsException(q.toString());
        }
        return count == 1L;
    }

    public <E> boolean existsById(Table<E> table, Object id) {
        IQueryObject q = o.existsById(table, id);
        long count = facade.loadUnique(q, ScalarMappers.LONG);
        if (count > 1) {
            throw new TooManyResultsException(q.toString());
        }
        return count == 1L;
    }

    public <E> void insert(Table<E> table, E entity) {

        for (Generator<?> ag : table.getAutoGens()) {
            if (ag.isBeforeInsert()) {
                if (ag.getColumn().getAccessor().get(entity) != null) {
                    throw new RuntimeException("autogen must be null for insert(): " + ag.getColumn().toString());
                }
                Object autoGeneratedVal = ag.generate(facade);
                ag.getColumn().getAccessor().set(entity, autoGeneratedVal);
            }
        }

        IQueryObject q = o.insert(table, entity);
        facade.update(q);

        for (Generator<?> ag : table.getAutoGens()) {
            if (!ag.isBeforeInsert()) {
                if (ag.getColumn().getAccessor().get(entity) != null) {
                    throw new RuntimeException("autogen must be null for insert(): " + ag.getColumn().toString());
                }
                Object autoGeneratedVal = ag.generate(facade);
                ag.getColumn().getAccessor().set(entity, autoGeneratedVal);
            }
        }
    }

    public <E> void update(Table<E> table, E entity) {
        IQueryObject q = o.update(table, entity);
        int affectedRows = facade.update(q);
        if (affectedRows == 0) {
            throw new EmptyResultException(q.toString());
        } else if (affectedRows > 1) {
            throw new TooManyResultsException(q.toString());
        }
    }

    public <E> void update(Table<E> table, E entity, Iterable<Column<E, ?>> columnsToUpdate) {
        IQueryObject q = o.update(table, entity, columnsToUpdate);
        int affectedRows = facade.update(q);
        if (affectedRows == 0) {
            throw new EmptyResultException(q.toString());
        } else if (affectedRows > 1) {
            throw new TooManyResultsException(q.toString());
        }
    }

    /**
     * @return # of affected rows
     */
    public <E> int update(Table<E> table, E example, Iterable<Column<E, ?>> columnsToUpdate,
            IQueryObject wherePredicate) {
        IQueryObject q = o.update(table, example, columnsToUpdate, wherePredicate);
        return facade.update(q);
    }

    public <E> void delete(Table<E> table, E entity) {
        IQueryObject q = o.delete(table, entity);
        int affectedRows = facade.update(q);
        if (affectedRows == 0) {
            throw new EmptyResultException(q.toString());
        } else if (affectedRows > 1) {
            throw new TooManyResultsException(q.toString());
        }
    }

    public <E> void delete(Table<E> table, IQueryObject wherePredicate) {
        IQueryObject q = o.delete(table, wherePredicate);
        int affectedRows = facade.update(q);
        if (affectedRows == 0) {
            throw new EmptyResultException(q.toString());
        } else if (affectedRows > 1) {
            throw new TooManyResultsException(q.toString());
        }
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

    /**
     * @param orders usar {@link Sort#by(Order...)}
     */
    public <E> List<E> query(Table<E> table, IQueryObject whereRestriction, List<Order<E>> orders) {
        Query<E> q = o.query(table);
        q.append("select {} from {} where {}", table.all(), table, whereRestriction);
        if (!orders.isEmpty()) {
            q.append(" order by ");
            List<IQueryObject> qs = new ArrayList<>();
            for (Order<E> o : orders) {
                qs.add(o);
            }
            q.append(Restrictions.list(qs));
        }
        return q.getExecutor(facade).load();
    }

    public <E> List<E> query(Table<E> table, IQueryObject whereRestriction) {
        return query(table, whereRestriction, Collections.emptyList());
    }

    public <E> E queryUnique(Table<E> table, IQueryObject whereRestriction) {
        Query<E> q = o.query(table);
        q.append("select {} from {} where {}", table.all(), table, whereRestriction);
        return q.getExecutor(facade).loadUnique();
    }

    // ===========================================

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
        return query(table, Restrictions.and(restrictions), orders);
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
        return queryUnique(table, Restrictions.and(restrictions));
    }

}