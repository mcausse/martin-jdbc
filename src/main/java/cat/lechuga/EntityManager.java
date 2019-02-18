package cat.lechuga;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.exception.JdbcException;
import org.lenteja.jdbc.exception.UnexpectedResultException;
import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;

import cat.lechuga.generator.Generator;
import cat.lechuga.generator.ScalarMappers;
import cat.lechuga.mql.Orders;
import cat.lechuga.mql.Orders.Order;
import cat.lechuga.mql.QueryBuilder;
import cat.lechuga.tsmql.TypeSafeQueryBuilder;

public class EntityManager {

    private final DataAccesFacade facade;
    private final Map<Class<?>, EntityMeta<?>> entityMetas;
    private final EntityManagerOperations ops;

    public EntityManager(DataAccesFacade facade, List<EntityMeta<?>> entityMetas) {
        super();
        this.facade = facade;
        this.entityMetas = new LinkedHashMap<>();
        for (EntityMeta<?> em : entityMetas) {
            this.entityMetas.put(em.getEntityClass(), em);
        }
        this.ops = new EntityManagerOperations();
    }

    // ===========================================================
    // ===========================================================
    // ===========================================================

    public QueryBuilder buildQuery() {
        return new QueryBuilder(this);
    }

    public TypeSafeQueryBuilder buildTypeSafeQuery() {
        return new TypeSafeQueryBuilder(this);
    }

    // ===========================================================
    // ===========================================================
    // ===========================================================

    @SuppressWarnings("unchecked")
    public <E> EntityMeta<E> getEntityMeta(Class<E> entityClass) {
        if (!entityMetas.containsKey(entityClass)) {
            throw new RuntimeException(
                    "entity not defined: " + entityClass.getName() + "; valid ones=" + entityMetas.keySet().toString());
        }
        return (EntityMeta<E>) entityMetas.get(entityClass);
    }

    public EntityManagerOperations getOperations() {
        return ops;
    }

    public <E> List<E> loadAll(Class<E> entityClass) {
        EntityMeta<E> entityMeta = getEntityMeta(entityClass);
        IQueryObject q = ops.loadAll(entityMeta);
        return facade.load(q, entityMeta);
    }

    public <E, ID> E loadById(Class<E> entityClass, ID id) {
        EntityMeta<E> entityMeta = getEntityMeta(entityClass);
        IQueryObject q = ops.loadById(entityMeta, id);
        return facade.loadUnique(q, entityMeta);
    }

    @SuppressWarnings("unchecked")
    public <E> void refresh(E entity) {
        Class<E> entityClass = (Class<E>) entity.getClass();
        EntityMeta<E> entityMeta = getEntityMeta(entityClass);
        IQueryObject q = ops.refresh(entityMeta, entity);
        E e = facade.loadUnique(q, entityMeta);

        for (PropertyMeta p : entityMeta.getAllProps()) {
            Object value = p.getProp().get(e);
            p.getProp().set(entity, value);
        }
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
    public <E> void store(E entity) {

        Class<E> entityClass = (Class<E>) entity.getClass();

        EntityMeta<E> entityMeta = getEntityMeta(entityClass);

        if (entityMeta.getListeners() != null) {
            entityMeta.getListeners().forEach(l -> l.beforeStore(entity));
        }

        /**
         * si una propietat PK és primitiva, el seu valor mai serà null (p.ex. serà 0) i
         * l'store() no funcionarà. Si es té una PK primitiva, usar insert()/update() en
         * comptes d'store().
         */

        for (PropertyMeta p : entityMeta.getIdProps()) {
            if (p.getProp().getType().isPrimitive()) {
                throw new JdbcException(
                        "PK column is mapped as of primitive type: use insert()/update() instead of store(): "
                                + entity.getClass().getSimpleName() + "#" + p.getProp());
            }
        }

        boolean algunaPkAutogen = !entityMeta.getAutogenProps().isEmpty();
        if (algunaPkAutogen) {

            for (PropertyMeta p : entityMeta.getIdProps()) {
                if (p.getGenerator() == null) {
                    if (p.getProp().get(entity) == null) {
                        throw new JdbcException("una propietat PK no-autogenerada té valor null en store(): "
                                + entity.getClass().getSimpleName() + "#" + p.getProp());
                    }
                }
            }

            boolean algunaPkValNull = false;
            for (PropertyMeta p : entityMeta.getIdProps()) {
                if (p.getProp().get(entity) == null) {
                    algunaPkValNull = true;
                    break;
                }
            }

            if (algunaPkValNull) {
                insert(entity);
            } else {
                update(entity);
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
            for (PropertyMeta p : entityMeta.getIdProps()) {
                if (p.getProp().get(entity) == null) {
                    throw new JdbcException("una propietat PK no-autogenerada té valor null en store(): "
                            + entity.getClass().getSimpleName() + "#" + p.getProp());
                }
            }

            if (exists(entity)) {
                update(entity);
            } else {
                insert(entity);
            }

        }

        if (entityMeta.getListeners() != null) {
            entityMeta.getListeners().forEach(l -> l.afterStore(entity));
        }

    }

    @SuppressWarnings("unchecked")
    public <E> void update(E entity) {

        Class<E> entityClass = (Class<E>) entity.getClass();

        EntityMeta<E> entityMeta = getEntityMeta(entityClass);

        if (entityMeta.getListeners() != null) {
            entityMeta.getListeners().forEach(l -> l.beforeUpdate(entity));
        }

        IQueryObject q = ops.update(entityMeta, entity);
        int affectedRows = facade.update(q);
        if (affectedRows != 1) {
            throw new UnexpectedResultException(
                    "expected affectedRows=1, but affectedRows=" + affectedRows + " for " + q);
        }

        if (entityMeta.getListeners() != null) {
            entityMeta.getListeners().forEach(l -> l.afterUpdate(entity));
        }
    }

    @SuppressWarnings("unchecked")
    public <E> void insert(E entity) {

        Class<E> entityClass = (Class<E>) entity.getClass();
        EntityMeta<E> entityMeta = getEntityMeta(entityClass);

        if (entityMeta.getListeners() != null) {
            entityMeta.getListeners().forEach(l -> l.beforeInsert(entity));
        }

        for (PropertyMeta autoGenProp : entityMeta.getAutogenProps()) {
            Generator ag = autoGenProp.getGenerator();
            if (ag.isBeforeInsert()) {
                if (autoGenProp.getProp().get(entity) != null) {
                    throw new RuntimeException("autogen must be null for insert(): " + autoGenProp.toString()
                            + ": entity=" + entity.toString());
                }
                Object autoGeneratedVal = ag.generate(facade);
                autoGenProp.getProp().set(entity, autoGeneratedVal);
            }
        }

        IQueryObject q = ops.insert(entityMeta, entity);
        facade.update(q);

        for (PropertyMeta autoGenProp : entityMeta.getAutogenProps()) {
            Generator ag = autoGenProp.getGenerator();
            if (!ag.isBeforeInsert()) {
                if (autoGenProp.getProp().get(entity) != null) {
                    throw new RuntimeException("autogen must be null for insert(): " + autoGenProp.toString()
                            + ": entity=" + entity.toString());
                }
                Object autoGeneratedVal = ag.generate(facade);
                autoGenProp.getProp().set(entity, autoGeneratedVal);
            }
        }

        if (entityMeta.getListeners() != null) {
            entityMeta.getListeners().forEach(l -> l.afterInsert(entity));
        }

    }

    @SuppressWarnings("unchecked")
    public <E> void delete(E entity) {

        Class<E> entityClass = (Class<E>) entity.getClass();
        EntityMeta<E> entityMeta = getEntityMeta(entityClass);

        if (entityMeta.getListeners() != null) {
            entityMeta.getListeners().forEach(l -> l.beforeDelete(entity));
        }

        IQueryObject q = ops.delete(entityMeta, entity);
        int affectedRows = facade.update(q);
        if (affectedRows != 1) {
            throw new UnexpectedResultException(
                    "expected affectedRows=1, but affectedRows=" + affectedRows + " for " + q);
        }

        if (entityMeta.getListeners() != null) {
            entityMeta.getListeners().forEach(l -> l.afterDelete(entity));
        }
    }

    public <E, ID> boolean existsById(Class<E> entityClass, ID id) {

        EntityMeta<E> entityMeta = getEntityMeta(entityClass);

        IQueryObject q = ops.existsById(entityMeta, id);
        long rows = facade.loadUnique(q, ScalarMappers.LONG);
        return rows > 0L;
    }

    @SuppressWarnings("unchecked")
    public <E> boolean exists(E entity) {

        Class<E> entityClass = (Class<E>) entity.getClass();
        EntityMeta<E> entityMeta = getEntityMeta(entityClass);

        IQueryObject q = ops.exists(entityMeta, entity);
        long rows = facade.loadUnique(q, ScalarMappers.LONG);
        return rows > 0L;
    }

    // ===========================================================
    // ===========================================================
    // ===========================================================

    @SuppressWarnings("unchecked")
    public <E> E loadUniqueByExample(E example) {

        Class<E> entityClass = (Class<E>) example.getClass();
        EntityMeta<E> entityMeta = getEntityMeta(entityClass);

        QueryObject q = new QueryObject();
        q.append(getOperations().loadAll(entityMeta));
        q.append(" where 1=1");
        for (PropertyMeta p : entityMeta.getAllProps()) {
            Object v = p.getProp().get(example);
            if (v != null) {
                q.append(" and ");
                q.append(p.getColumnName());
                q.append("=?");
                q.addArg(p.getJdbcValue(example));
            }
        }
        return getFacade().loadUnique(q, entityMeta);
    }

    public <E> List<E> loadByExample(E example) {
        return loadByExample(example, null);
    }

    @SuppressWarnings("unchecked")
    public <E> List<E> loadByExample(E example, Orders<E> orders) {

        Class<E> entityClass = (Class<E>) example.getClass();
        EntityMeta<E> entityMeta = getEntityMeta(entityClass);

        QueryObject q = new QueryObject();
        q.append(getOperations().loadAll(entityMeta));
        q.append(" where 1=1");
        for (PropertyMeta p : entityMeta.getAllProps()) {
            Object v = p.getProp().get(example);
            if (v != null) {
                q.append(" and ");
                q.append(p.getColumnName());
                q.append("=?");
                q.addArg(p.getJdbcValue(example));
            }
        }
        if (orders != null && !orders.getOrders().isEmpty()) {
            q.append(" order by ");
            StringJoiner j = new StringJoiner(", ");
            for (Order<E> o : orders.getOrders()) {
                PropertyMeta p = entityMeta.getProp(o.getPropName());
                j.add(p.getColumnName() + " " + o.getOrder());
            }
            q.append(j.toString());
        }
        return getFacade().load(q, entityMeta);
    }

    // ===========================================================
    // ===========================================================
    // ===========================================================

    public DataAccesFacade getFacade() {
        return facade;
    }

    public void begin() {
        facade.begin();
    }

    public void commit() {
        facade.commit();
    }

    public void rollback() {
        facade.rollback();
    }

}