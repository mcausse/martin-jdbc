package cat.lechuga.repository;

import java.util.List;
import java.util.Optional;

import org.lenteja.jdbc.exception.EmptyResultException;

import cat.lechuga.EntityManager;
import cat.lechuga.mql.QueryBuilder;
import cat.lechuga.tsmql.MetaTable;
import cat.lechuga.tsmql.TypeSafeQueryBuilder;

public class Repository<E, ID, E_ extends MetaTable<E>> implements IRepository<E, ID, E_> {

    final EntityManager em;
    final E_ meta;
    final Class<E> entityClass;

    public Repository(EntityManager em, E_ meta) {
        super();
        this.em = em;
        this.meta = meta;
        this.entityClass = meta.getEntityClass();
    }

    public EntityManager getEntityManager() {
        return em;
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

    // ===========================================================
    // ===========================================================
    // ===========================================================

    // public E loadUniqueByExample(E example) {
    // return em.loadUniqueByExample(example);
    // }
    //
    // public List<E> loadByExample(E example) {
    // return em.loadByExample(example, (IQueryObject) null);
    // }
    //
    // public List<E> loadByExample(E example, Sort<E_> sort) {
    // TypeSafeQueryBuilder q = em.buildTypeSafeQuery() //
    // .addAlias(meta) //
    // .append("select {} from {} where 1=1", meta.all(), meta) //
    // ;
    // if (sort != null) {
    // q.append("{}", sort.toPredicate(meta));
    // }
    // return q.getExecutor(entityClass).load();
    // }

    // ===========================================================
    // ===========================================================
    // ===========================================================

    @Override
    public Optional<E> findUniqueBy(Specification<E_> spec) {
        try {
            TypeSafeQueryBuilder q = em.buildTypeSafeQuery().addAlias(meta);
            q.append("select {} from {}", meta.all(), meta);
            if (spec != null) {
                q.append(" where {}", spec.toPredicate(meta));
            }
            E r = q.getExecutor(entityClass).loadUnique();
            return Optional.of(r);
        } catch (EmptyResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<E> findBy(Specification<E_> spec) {
        return findBy(spec, null);
    }

    @Override
    public List<E> findBy(Specification<E_> spec, Sort<E_> sorting) {
        TypeSafeQueryBuilder q = em.buildTypeSafeQuery().addAlias(meta);
        q.append("select {} from {}", meta.all(), meta);
        if (spec != null) {
            q.append(" where {}", spec.toPredicate(meta));
        }
        if (sorting != null) {
            q.append(" order by {}", sorting.toPredicate(meta));
        }
        List<E> r = q.getExecutor(entityClass).load();
        return r;
    }

    @Override
    public Optional<E> findById(ID id) {
        try {
            E e = em.loadById(entityClass, id);
            return Optional.of(e);
        } catch (EmptyResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public void save(E entity) {
        em.store(entity);
    }

    @Override
    public void saveAll(Iterable<E> entities) {
        for (E e : entities) {
            save(e);
        }
    }

    @Override
    public boolean existsById(ID id) {
        return em.existsById(entityClass, id);
    }

    @Override
    public boolean exists(E entity) {
        return em.exists(entity);
    }

    @Override
    public List<E> findAll() {
        return em.loadAll(entityClass);
    }

    @Override
    public void deleteById(ID id) {
        E e = em.loadById(entityClass, id);
        em.delete(e);
    }

    @Override
    public void delete(E entity) {
        em.delete(entity);
    }

    @Override
    public void deleteAll(Iterable<E> entities) {
        for (E e : entities) {
            delete(e);
        }
    }

}