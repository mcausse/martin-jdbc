package cat.lechuga;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.query.QueryObject;

public class GenericDao<E, ID> implements FacadedMapable<E>, EntityMetable<E> {

    final EntityManager<E, ID> em;

    public GenericDao(EntityManager<E, ID> em) {
        super();
        this.em = em;
    }

    public EntityManager<E, ID> getEntityManager() {
        return em;
    }

    // ===========================================================
    // ===========================================================
    // ===========================================================

    public E loadUniqueByExample(E example) {
        QueryObject q = new QueryObject();
        q.append(em.getOperations().loadAll());
        q.append(" where 1=1");
        for (PropertyMeta p : em.getEntityMeta().getAllProps()) {
            Object v = p.getProp().get(example);
            if (v != null) {
                q.append(" and ");
                q.append(p.getColumnName());
                q.append("=?");
                q.addArg(p.getJdbcValue(example));
            }
        }
        return em.getFacade().loadUnique(q, em);
    }

    public List<E> loadByExample(E example) {
        return loadByExample(example, Collections.emptyList());
    }

    public List<E> loadByExample(E example, List<Order<E>> orders) {
        QueryObject q = new QueryObject();
        q.append(em.getOperations().loadAll());
        q.append(" where 1=1");
        for (PropertyMeta p : em.getEntityMeta().getAllProps()) {
            Object v = p.getProp().get(example);
            if (v != null) {
                q.append(" and ");
                q.append(p.getColumnName());
                q.append("=?");
                q.addArg(p.getJdbcValue(example));
            }
        }
        if (orders.size() > 0) {
            q.append(" order by ");
            StringJoiner j = new StringJoiner(", ");
            for (Order<E> o : orders) {
                PropertyMeta p = em.getEntityMeta().getProp(o.getPropName());
                j.add(p.getColumnName() + " " + o.getOrder());
            }
            q.append(j.toString());
        }
        return em.getFacade().load(q, em);
    }

    // ===========================================================
    // ================= FacadedMapable ========================
    // ===========================================================

    @Override
    public E map(ResultSet rs) throws SQLException {
        return em.map(rs);
    }

    @Override
    public DataAccesFacade getFacade() {
        return em.getFacade();
    }

    // ===========================================================
    // ===========================================================
    // ===========================================================

    @Override
    public EntityMeta<E> getEntityMeta() {
        return em.getEntityMeta();
    }

    public List<E> loadAll() {
        return em.loadAll();
    }

    public E loadById(ID id) {
        return em.loadById(id);
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

    // public void deleteById(ID id) {
    // em.deleteById(id);
    // }

    public void delete(E entity) {
        em.delete(entity);
    }

    public boolean existsById(ID id) {
        return em.existsById(id);
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
