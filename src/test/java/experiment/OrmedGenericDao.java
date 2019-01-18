package experiment;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.mapper.EntityManager;
import org.lenteja.mapper.Table;
import org.lenteja.mapper.query.Order;

public class OrmedGenericDao {

    public static class EntityLifeCicleListener<E> {

        public void afterLoad(OrmedGenericDao odao, E entity) {
        }

        public void beforeStore(OrmedGenericDao odao, E entity) {
        }

        public void afterStore(OrmedGenericDao odao, E entity) {
        }

        public void beforeDelete(OrmedGenericDao odao, E entity) {
        }

        public void afterDelete(OrmedGenericDao odao, E entity) {
        }
    }

    final DataAccesFacade facade;
    final EntityManager em;

    final Map<Class<?>, Table<?>> tables = new LinkedHashMap<>();
    final Map<Class<?>, EntityLifeCicleListener<?>> listeners = new LinkedHashMap<>();

    public OrmedGenericDao(DataAccesFacade facade) {
        super();
        this.facade = facade;
        this.em = new EntityManager(facade);
    }

    public <E> void addEntity(Class<E> entityClass, Table<E> table, EntityLifeCicleListener<E> listener) {
        this.tables.put(entityClass, table);
        this.listeners.put(entityClass, listener);
    }

    public <E> void addEntity(Class<E> entityClass, Table<E> table) {
        this.tables.put(entityClass, table);
    }

    ///////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    protected <E> Table<E> getTable(Class<?> entityClass) {
        if (!tables.containsKey(entityClass)) {
            throw new RuntimeException("table not defined for entity class: " + entityClass);
        }
        Table<E> table = (Table<E>) tables.get(entityClass);
        return table;
    }

    @SuppressWarnings("unchecked")
    protected <E> void executeListener(E entity, BiConsumer<EntityLifeCicleListener<E>, E> method) {
        Class<? extends Object> entityClass = entity.getClass();
        if (listeners.containsKey(entityClass)) {
            EntityLifeCicleListener<E> listener = (EntityLifeCicleListener<E>) listeners.get(entityClass);
            method.accept(listener, entity);
        }
    }

    public <E> void refresh(E entity) {
        Table<E> table = getTable(entity.getClass());
        em.refresh(table, entity);
        executeListener(entity, (l, e) -> l.afterLoad(this, e));
    }

    public <E> E loadById(Class<E> entityClass, Object id) {
        Table<E> table = getTable(entityClass);
        E r = em.loadById(table, id);
        executeListener(r, (l, e) -> l.afterLoad(this, e));
        return r;
    }

    public <E> void store(E entity) {
        Table<E> table = getTable(entity.getClass());
        executeListener(entity, (l, e) -> l.beforeStore(this, e));
        em.store(table, entity);
        executeListener(entity, (l, e) -> l.afterStore(this, e));
    }

    public <E> void insert(E entity) {
        Table<E> table = getTable(entity.getClass());
        executeListener(entity, (l, e) -> l.beforeStore(this, e));
        em.insert(table, entity);
        executeListener(entity, (l, e) -> l.afterStore(this, e));
    }

    public <E> void update(E entity) {
        Table<E> table = getTable(entity.getClass());
        executeListener(entity, (l, e) -> l.beforeStore(this, e));
        em.update(table, entity);
        executeListener(entity, (l, e) -> l.afterStore(this, e));
    }

    public <E> void delete(E entity) {
        Table<E> table = getTable(entity.getClass());
        executeListener(entity, (l, e) -> l.beforeDelete(this, e));
        em.delete(table, entity);
        executeListener(entity, (l, e) -> l.afterDelete(this, e));
    }

    public <E> boolean existsById(Class<E> entityClass, Object id) {
        Table<E> table = getTable(entityClass);
        boolean r = em.existsById(table, id);
        return r;
    }

    public <E> boolean exists(E entity) {
        Table<E> table = getTable(entity.getClass());
        boolean r = em.exists(table, entity);
        return r;
    }

    // public <E> Query<E> queryFor(Class<E> entityClass) {
    // Table<E> table = getTable(entityClass);
    // Query<E> r = em.queryFor(table);
    // return r;
    // }
    //
    // public <E, C> Query<C> scalarQueryFor(Column<E, C> column) {
    // Query<C> r = em.scalarQueryFor(column);
    // return r;
    // }

    public <E> List<E> query(Class<E> entityClass, IQueryObject restriction, List<Order<E>> orders) {
        Table<E> table = getTable(entityClass);
        List<E> r = em.query(table, restriction, orders);
        r.forEach(entity -> executeListener(entity, (l, e) -> l.afterLoad(this, e)));
        return r;
    }

    public <E> List<E> query(Class<E> entityClass, IQueryObject restriction) {
        Table<E> table = getTable(entityClass);
        List<E> r = em.query(table, restriction);
        r.forEach(entity -> executeListener(entity, (l, e) -> l.afterLoad(this, e)));
        return r;
    }

    public <E> E queryUnique(Class<E> entityClass, IQueryObject restrictions) {
        Table<E> table = getTable(entityClass);
        E r = em.queryUnique(table, restrictions);
        executeListener(r, (l, e) -> l.afterLoad(this, e));
        return r;
    }

    public <E> List<E> query(E example, List<Order<E>> orders) {
        Table<E> table = getTable(example.getClass());
        List<E> r = em.query(table, example, orders);
        r.forEach(entity -> executeListener(entity, (l, e) -> l.afterLoad(this, e)));
        return r;
    }

    public <E> List<E> query(E example) {
        Table<E> table = getTable(example.getClass());
        List<E> r = em.query(table, example);
        r.forEach(entity -> executeListener(entity, (l, e) -> l.afterLoad(this, e)));
        return r;
    }

    public <E> E queryUnique(E example) {
        Table<E> table = getTable(example.getClass());
        E r = em.queryUnique(table, example);
        executeListener(r, (l, e) -> l.afterLoad(this, e));
        return r;
    }

    //////////////////////////////////////////////

    public <E> void storeAll(Iterable<E> entities) {
        entities.forEach(e -> store(e));
    }

    public <E> void insertAll(Iterable<E> entities) {
        entities.forEach(e -> insert(e));
    }

    public <E> void updateAll(Iterable<E> entities) {
        entities.forEach(e -> update(e));
    }

    public <E> void deleteAll(Iterable<E> entities) {
        entities.forEach(e -> delete(e));
    }

    //////////////////////////////////////////////

    public DataAccesFacade getFacade() {
        return facade;
    }

    public EntityManager getEntityManager() {
        return em;
    }

}
