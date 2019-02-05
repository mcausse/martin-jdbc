package cat.lechuga;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.StringJoiner;

import org.lenteja.jdbc.query.QueryObject;

import cat.lechuga.mql.Orders;
import cat.lechuga.mql.Orders.Order;
import cat.lechuga.mql.QueryBuilder;
import cat.lechuga.tsmql.TypeSafeQueryBuilder;

public class GenericDao<E, ID> {

    final EntityManager em;
    final Class<E> entityClass;
    final EntityMeta<E> entityMeta;

    public GenericDao(EntityManager em, Class<E> entityClass) {
        super();
        this.em = em;
        this.entityClass = entityClass;
        this.entityMeta = em.getEntityMeta(entityClass);
    }

    /**
     * for derived classes only
     */
    @SuppressWarnings("unchecked")
    public GenericDao(EntityManager em) {
        super();
        this.em = em;
        this.entityClass = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
        this.entityMeta = em.getEntityMeta(entityClass);
    }

    // ===========================================================
    // ===========================================================
    // ===========================================================

    public QueryBuilder buildQuery() {
        return em.buildQuery();
    }

    public TypeSafeQueryBuilder buildTypedQuery() {
        return em.buildTypeSafeQuery();
    }

    public EntityManager getEntityManager() {
        return em;
    }

    public Class<E> getEntityClass() {
        return entityClass;
    }

    // ===========================================================
    // ===========================================================
    // ===========================================================

    public E loadUniqueByExample(E example) {
        QueryObject q = new QueryObject();
        q.append(em.getOperations().loadAll(entityMeta));
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
        return em.getFacade().loadUnique(q, entityMeta);
    }

    public List<E> loadByExample(E example) {
        return loadByExample(example, null);
    }

    public List<E> loadByExample(E example, Orders<E> orders) {
        QueryObject q = new QueryObject();
        q.append(em.getOperations().loadAll(entityMeta));
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
        if (orders != null) {
            q.append(" order by ");
            StringJoiner j = new StringJoiner(", ");
            for (Order<E> o : orders.getOrders()) {
                PropertyMeta p = entityMeta.getProp(o.getPropName());
                j.add(p.getColumnName() + " " + o.getOrder());
            }
            q.append(j.toString());
        }
        return em.getFacade().load(q, entityMeta);
    }

    // ===========================================================
    // ===========================================================
    // ===========================================================

    public List<E> loadAll() {
        return em.loadAll(entityClass);
    }

    public E loadById(ID id) {
        return em.loadById(entityClass, id);
    }

    public void refresh(E entity) {
        em.refresh(entityClass, entity);
    }

    public void store(E entity) {
        em.store(entity);
    }

    public void update(E entity) {
        em.update(entity);
    }

    public void insert(E entity) {
        em.insert(entity);
    }

    public void delete(E entity) {
        em.delete(entity);
    }

    public boolean existsById(ID id) {
        return em.existsById(entityClass, id);
    }

    public boolean exists(E entity) {
        return em.exists(entity);
    }

    // ===========================================================
    // ===========================================================
    // ===========================================================

    public void storeAll(Iterable<E> entities) {
        for (E e : entities) {
            store(e);
        }
    }

    public void updateAll(Iterable<E> entities) {
        for (E e : entities) {
            update(e);
        }
    }

    public void insertAll(Iterable<E> entities) {
        for (E e : entities) {
            insert(e);
        }
    }

    public void deleteAll(Iterable<E> entities) {
        for (E e : entities) {
            delete(e);
        }
    }

}
