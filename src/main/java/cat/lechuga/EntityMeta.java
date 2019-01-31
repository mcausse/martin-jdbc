package cat.lechuga;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cat.lechuga.reflect.ReflectUtils;

public class EntityMeta<E> implements Mapable<E> {

    private final Class<E> entityClass;
    private final String tableName;
    private final List<EntityListener<E>> listeners;

    private final List<PropertyMeta> allProps;
    private final List<PropertyMeta> idProps;
    private final List<PropertyMeta> regularProps;
    private final List<PropertyMeta> autogenProps;

    public EntityMeta(Class<E> entityClass, String tableName, List<EntityListener<E>> listeners,
            List<PropertyMeta> allProps) {
        super();
        this.entityClass = entityClass;
        this.tableName = tableName;
        this.listeners = listeners;
        this.allProps = allProps;

        this.idProps = new ArrayList<>();
        this.regularProps = new ArrayList<>();
        this.autogenProps = new ArrayList<>();
        for (PropertyMeta p : allProps) {
            if (p.isId()) {
                this.idProps.add(p);
            } else {
                this.regularProps.add(p);
            }
            if (p.getGenerator() != null) {
                this.autogenProps.add(p);
            }
        }
    }

    @Override
    public E map(ResultSet rs) throws SQLException {
        E entity = ReflectUtils.newInstance(getEntityClass());
        for (PropertyMeta p : getAllProps()) {
            p.readValue(entity, rs);
        }

        if (getListeners() != null) {
            getListeners().forEach(l -> l.afterLoad(entity));
        }

        return entity;
    }

    public Class<E> getEntityClass() {
        return entityClass;
    }

    public String getTableName() {
        return tableName;
    }

    public List<EntityListener<E>> getListeners() {
        return listeners;
    }

    public List<PropertyMeta> getAllProps() {
        return allProps;
    }

    public List<PropertyMeta> getIdProps() {
        return idProps;
    }

    public List<PropertyMeta> getRegularProps() {
        return regularProps;
    }

    public List<PropertyMeta> getAutogenProps() {
        return autogenProps;
    }

    public PropertyMeta getProp(String propFullName) {
        for (PropertyMeta p : allProps) {
            if (p.getProp().getFullName().equals(propFullName)) {
                return p;
            }
        }
        throw new RuntimeException("property not found: '" + propFullName + "; in " + toString());
    }

    @Override
    public String toString() {
        return "EntityMeta [entityClass=" + entityClass + ", tableName=" + tableName + ", allProps=" + allProps + "]";
    }

}