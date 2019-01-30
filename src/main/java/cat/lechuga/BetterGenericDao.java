package cat.lechuga;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.lenteja.jdbc.DataAccesFacade;

import cat.lechuga.Orders.Order;
import cat.lechuga.tsmql.Criterion;
import cat.lechuga.tsmql.MetaTable;
import cat.lechuga.tsmql.TOrders;
import cat.lechuga.tsmql.TOrders.TOrder;
import cat.lechuga.tsmql.TypeSafeQueryBuilder;

public class BetterGenericDao<E, ID> implements Facaded, Mapable<E>, EntityMetable<E> {

    final GenericDao<E, ID> genericDao;
    final MetaTable<E> metaTable;

    public BetterGenericDao(EntityManager<E, ID> em, MetaTable<E> metaTable) {
        super();
        this.genericDao = new GenericDao<>(em);
        this.metaTable = metaTable;
    }

    public E loadUniqueBy(Criterion criterion) {
        TypeSafeQueryBuilder qb = new TypeSafeQueryBuilder();
        qb.addAlias(metaTable);
        qb.append("select {} ", metaTable.all());
        qb.append("from {} ", metaTable);
        qb.append("where {}", criterion);
        return qb.getExecutor(genericDao.getEntityManager()).loadUnique();
    }

    public List<E> loadBy(Criterion criterion) {
        return loadBy(criterion, null);
    }

    public List<E> loadBy(Criterion criterion, TOrders<E> orders) {
        TypeSafeQueryBuilder qb = new TypeSafeQueryBuilder();
        qb.addAlias(metaTable);
        qb.append("select {} ", metaTable.all());
        qb.append("from {} ", metaTable);
        qb.append("where {}", criterion);
        if (orders != null) {
            qb.append("order by ");
            int c = 0;
            for (TOrder<E> o : orders.getOrders()) {
                if (c > 0) {
                    qb.append(",");
                }
                qb.append("{} ", o.getMetaColumn());
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

    public List<E> loadByExample(E example, Orders<E> orders) {
        return genericDao.loadByExample(example, orders);
    }

    public List<E> loadByExample(E example, TOrders<E> orders) {
        List<Order<E>> os = new ArrayList<>();
        for (TOrder<E> o : orders.getOrders()) {
            if (Order.ASC.equals(o.getOrder())) {
                os.add(Order.asc(o.getMetaColumn().getPropertyName()));
            } else {
                os.add(Order.desc(o.getMetaColumn().getPropertyName()));
            }
        }
        return loadByExample(example, new Orders<>(os));
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
