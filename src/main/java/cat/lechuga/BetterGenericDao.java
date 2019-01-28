package cat.lechuga;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.lenteja.jdbc.DataAccesFacade;

import cat.lechuga.mql.QueryBuilder;
import cat.lechuga.tsmql.Criterion;

public class BetterGenericDao<E, ID> implements FacadedMapable<E>, EntityMetable<E> {

    final GenericDao<E, ID> genericDao;

    public BetterGenericDao(EntityManager<E, ID> em) {
        super();
        this.genericDao = new GenericDao<>(em);
    }

    public E loadUniqueBy(Criterion criterion) { // TODO testar
        QueryBuilder qb = new QueryBuilder();
        qb.addAlias("e", genericDao);
        qb.append("select {e.*} ");
        qb.append("from {e.#} ");
        qb.append("where {}", criterion);
        return qb.getExecutor(genericDao.getEntityManager()).loadUnique();
    }

    public List<E> loadBy(Criterion criterion) { // TODO testar
        QueryBuilder qb = new QueryBuilder();
        qb.addAlias("e", genericDao);
        qb.append("select {e.*} ");
        qb.append("from {e.#} ");
        qb.append("where {}", criterion);
        return qb.getExecutor(genericDao.getEntityManager()).load();
    }

    public List<E> loadBy(Criterion criterion, List<Order<E>> orders) { // TODO testar
        QueryBuilder qb = new QueryBuilder();
        qb.addAlias("e", genericDao);
        qb.append("select {e.*} ");
        qb.append("from {e.#} ");
        qb.append("where {}", criterion);
        if (!orders.isEmpty()) {
            qb.append("order by ");// TODO aixo fallara
            int c = 0;
            for (Order<E> o : orders) {
                if (c > 0) {
                    qb.append(",");
                }
                qb.append("{e.");
                qb.append(o.getPropName());
                qb.append("} ");
                qb.append(o.getOrder());
                c++;
            }
        }
        return qb.getExecutor(genericDao.getEntityManager()).load();
    }

    // ===========================================================
    // ================= FacadedMapable ========================
    // ===========================================================

    @Override
    public E map(ResultSet rs) throws SQLException {
        return genericDao.map(rs);
    }

    @Override
    public DataAccesFacade getFacade() {
        return genericDao.getFacade();
    }

    // ===========================================================
    // ===========================================================
    // ===========================================================

    public E loadUniqueByExample(E example) {
        return genericDao.loadUniqueByExample(example);
    }

    public List<E> loadByExample(E example) {
        return genericDao.loadByExample(example);
    }

    public List<E> loadByExample(E example, List<Order<E>> orders) {
        return genericDao.loadByExample(example, orders);
    }

    @Override
    public EntityMeta<E> getEntityMeta() {
        return genericDao.getEntityMeta();
    }

    public List<E> loadAll() {
        return genericDao.loadAll();
    }

    public E loadById(ID id) {
        return genericDao.loadById(id);
    }

    public void refresh(E entity) {
        genericDao.refresh(entity);
    }

    public void store(E entity) {
        genericDao.store(entity);
    }

    public void update(E entity) {
        genericDao.update(entity);
    }

    public void insert(E entity) {
        genericDao.insert(entity);
    }

    public void delete(E entity) {
        genericDao.delete(entity);
    }

    public boolean existsById(ID id) {
        return genericDao.existsById(id);
    }

    public boolean exists(E entity) {
        return genericDao.exists(entity);
    }

    public void storeAll(Iterable<E> entities) {
        genericDao.storeAll(entities);
    }

    public void updateAll(Iterable<E> entities) {
        genericDao.updateAll(entities);
    }

    public void insertAll(Iterable<E> entities) {
        genericDao.insertAll(entities);
    }

    public void deleteAll(Iterable<E> entities) {
        genericDao.deleteAll(entities);
    }

}
