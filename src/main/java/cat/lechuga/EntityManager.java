package cat.lechuga;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.exception.JdbcException;
import org.lenteja.jdbc.exception.UnexpectedResultException;
import org.lenteja.jdbc.query.IQueryObject;

import cat.lechuga.generator.Generator;
import cat.lechuga.generator.ScalarMappers;
import cat.lechuga.reflect.ReflectUtils;

public class EntityManager<E, ID> implements Facaded, Mapable<E>, EntityMetable<E> {

    private final DataAccesFacade facade;
    private final EntityMeta<E> entityMeta;
    private final EntityManagerOperations<E> ops;

    public EntityManager(DataAccesFacade facade, EntityMeta<E> entityMeta) {
        super();
        this.facade = facade;
        this.entityMeta = entityMeta;
        this.ops = new EntityManagerOperations<>(entityMeta);
    }

    void executeListenerCallback(Consumer<EntityListener<E>> callbackMethod, E entity) {

    }

    // ===========================================================
    // ================= FacadedMapable ========================
    // ===========================================================

    @Override
    public E map(ResultSet rs) throws SQLException {
        E entity = ReflectUtils.newInstance(entityMeta.getEntityClass());
        for (PropertyMeta p : entityMeta.getAllProps()) {
            p.readValue(entity, rs);
        }

        if (entityMeta.getListeners() != null) {
            entityMeta.getListeners().forEach(l -> l.afterLoad(entity));
        }

        return entity;
    }

    @Override
    public DataAccesFacade getFacade() {
        return facade;
    }

    // ===========================================================
    // ===========================================================
    // ===========================================================

    @Override
    public EntityMeta<E> getEntityMeta() {
        return entityMeta;
    }

    public EntityManagerOperations<E> getOperations() {
        return ops;
    }

    public List<E> loadAll() {
        IQueryObject q = ops.loadAll();
        return facade.load(q, this);
    }

    public E loadById(ID id) {
        IQueryObject q = ops.loadById(id);
        return facade.loadUnique(q, this);
    }

    public void refresh(E entity) {
        IQueryObject q = ops.refresh(entity);
        E e = facade.loadUnique(q, this);

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
    public void store(E entity) {

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

    public void update(E entity) {

        if (entityMeta.getListeners() != null) {
            entityMeta.getListeners().forEach(l -> l.beforeUpdate(entity));
        }

        IQueryObject q = ops.update(entity);
        int affectedRows = facade.update(q);
        if (affectedRows != 1) {
            throw new UnexpectedResultException(
                    "expected affectedRows=1, but affectedRows=" + affectedRows + " for " + q);
        }

        if (entityMeta.getListeners() != null) {
            entityMeta.getListeners().forEach(l -> l.afterUpdate(entity));
        }
    }

    public void insert(E entity) {

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

        IQueryObject q = ops.insert(entity);
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

    // public void deleteById(ID id) {
    //
    // if (entityMeta.getListeners() != null) {
    // entityMeta.getListeners().forEach(l -> l.beforeDelete(entity));
    // }
    //
    // IQueryObject q = ops.deleteById(id);
    // int affectedRows = facade.update(q);
    // if (affectedRows != 1) {
    // throw new UnexpectedResultException(
    // "expected affectedRows=1, but affectedRows=" + affectedRows + " for " + q);
    // }
    //
    // if (entityMeta.getListeners() != null) {
    // entityMeta.getListeners().forEach(l -> l.afterDelete(entity));
    // }
    // }

    public void delete(E entity) {

        if (entityMeta.getListeners() != null) {
            entityMeta.getListeners().forEach(l -> l.beforeDelete(entity));
        }

        IQueryObject q = ops.delete(entity);
        int affectedRows = facade.update(q);
        if (affectedRows != 1) {
            throw new UnexpectedResultException(
                    "expected affectedRows=1, but affectedRows=" + affectedRows + " for " + q);
        }

        if (entityMeta.getListeners() != null) {
            entityMeta.getListeners().forEach(l -> l.afterDelete(entity));
        }
    }

    public boolean existsById(ID id) {
        IQueryObject q = ops.existsById(id);
        long rows = facade.loadUnique(q, ScalarMappers.LONG);
        return rows > 0L;
    }

    public boolean exists(E entity) {
        IQueryObject q = ops.exists(entity);
        long rows = facade.loadUnique(q, ScalarMappers.LONG);
        return rows > 0L;
    }

}