package cat.lechuga;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.lenteja.jdbc.query.IQueryObject;

import cat.lechuga.mql.Orders;
import cat.lechuga.mql.QueryBuilder;
import cat.lechuga.tsmql.MetaTable;
import cat.lechuga.tsmql.TOrders;
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

    public TypeSafeQueryBuilder buildTypeSafeQuery() {
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

    public E loadUniqueBy(MetaTable<E> metaTable, IQueryObject criterion) {
        TypeSafeQueryBuilder tsq = buildTypeSafeQuery();
        tsq.addAlias(metaTable);
        tsq.append("select {} from {} where {}", metaTable.all(), metaTable, criterion);
        return tsq.getExecutor(getEntityClass()).loadUnique();
    }

    public E loadFirstBy(MetaTable<E> metaTable, IQueryObject criterion) {
        TypeSafeQueryBuilder tsq = buildTypeSafeQuery();
        tsq.addAlias(metaTable);
        tsq.append("select {} from {} where {}", metaTable.all(), metaTable, criterion);
        return tsq.getExecutor(getEntityClass()).loadFirst();
    }

    public List<E> loadBy(MetaTable<E> metaTable, IQueryObject criterion) {
        return loadBy(metaTable, criterion, null);
    }

    public List<E> loadBy(MetaTable<E> metaTable, IQueryObject criterion, TOrders orders) {
        TypeSafeQueryBuilder tsq = buildTypeSafeQuery();
        tsq.addAlias(metaTable);
        tsq.append("select {} from {} where {}", metaTable.all(), metaTable, criterion);
        if (orders != null && !orders.getOrders().isEmpty()) {
            tsq.append(" order by {}", orders);
        }
        return tsq.getExecutor(getEntityClass()).load();
    }

    // ===========================================================
    // ===========================================================
    // ===========================================================

    public E loadUniqueByExample(E example) {
        return em.loadUniqueByExample(example);
    }

    public List<E> loadByExample(E example) {
        return em.loadByExample(example);
    }

    public List<E> loadByExample(E example, Orders orders) {
        return em.loadByExample(example, orders);
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
        em.refresh(entity);
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
