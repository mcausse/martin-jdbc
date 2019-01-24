package cat.lechuga.jdbc;

import java.util.List;

import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.exception.UnexpectedResultException;
import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.mapper.Mapable;
import org.lenteja.mapper.autogen.ScalarMappers;

import cat.lechuga.jdbc.reflect.ReflectUtils;

public class EntityManager<E, ID> {

    private final DataAccesFacade facade;
    private final EntityMeta<E> entityMeta;
    private final EntityManagerOperations<E> ops;

    private final Mapable<E> entityMapable;

    public EntityManager(DataAccesFacade facade, EntityMeta<E> entityMeta) {
        super();
        this.facade = facade;
        this.entityMeta = entityMeta;
        this.ops = new EntityManagerOperations<>(entityMeta);
        this.entityMapable = rs -> {
            E r = ReflectUtils.newInstance(entityMeta.getEntityClass());
            for (PropertyMeta p : entityMeta.getAllProps()) {
                p.readValue(r, rs);
            }
            return r;
        };
    }

    public List<E> loadAll() {
        IQueryObject q = ops.loadAll();
        return facade.load(q, entityMapable);
    }

    public E loadById(ID id) {
        IQueryObject q = ops.loadById(id);
        return facade.loadUnique(q, entityMapable);
    }

    public void refresh(E entity) {
        IQueryObject q = ops.refresh(entity);
        E e = facade.loadUnique(q, entityMapable);

        for (PropertyMeta p : entityMeta.getRegularProps()) {
            Object value = p.getProp().get(e);
            p.getProp().set(entity, value);
        }
    }

    public void update(E entity) {
        IQueryObject q = ops.update(entity);
        int affectedRows = facade.update(q);
        if (affectedRows != 1) {
            throw new UnexpectedResultException(
                    "expected affectedRows=1, but affectedRows=" + affectedRows + " for " + q);
        }
    }

    public void insert(E entity) {
        IQueryObject q = ops.update(entity);
        facade.update(q);
    }

    public void deleteById(ID id) {
        IQueryObject q = ops.deleteById(id);
        int affectedRows = facade.update(q);
        if (affectedRows != 1) {
            throw new UnexpectedResultException(
                    "expected affectedRows=1, but affectedRows=" + affectedRows + " for " + q);
        }
    }

    public boolean existsById(ID id) {
        IQueryObject q = ops.existsById(id);
        long rows = facade.loadUnique(q, ScalarMappers.LONG);
        return rows > 0L;
    }

    public void delete(E entity) {
        IQueryObject q = ops.delete(entity);
        int affectedRows = facade.update(q);
        if (affectedRows != 1) {
            throw new UnexpectedResultException(
                    "expected affectedRows=1, but affectedRows=" + affectedRows + " for " + q);
        }
    }

    public boolean exists(E entity) {
        IQueryObject q = ops.exists(entity);
        long rows = facade.loadUnique(q, ScalarMappers.LONG);
        return rows > 0L;
    }

}