package cat.lechuga.jdbc;

import java.util.ArrayList;
import java.util.List;

public class EntityMeta<E> {

    private final Class<E> entityClass;
    private final String tableName;

    private final List<PropertyMeta> allProps;
    private final List<PropertyMeta> idProps;
    private final List<PropertyMeta> regularProps;

    public EntityMeta(Class<E> entityClass, String tableName, List<PropertyMeta> allProps) {
        super();
        this.entityClass = entityClass;
        this.tableName = tableName;
        this.allProps = allProps;

        this.idProps = new ArrayList<>();
        this.regularProps = new ArrayList<>();
        for (PropertyMeta p : allProps) {
            if (p.isId()) {
                this.idProps.add(p);
            } else {
                this.regularProps.add(p);
            }
        }
    }

    public Class<E> getEntityClass() {
        return entityClass;
    }

    public String getTableName() {
        return tableName;
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

    @Override
    public String toString() {
        return "EntityMeta [entityClass=" + entityClass + ", tableName=" + tableName + ", allProps=" + allProps + "]";
    }

}