package cat.lechuga.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;
import org.lenteja.mapper.Conventions;
import org.lenteja.mapper.autogen.Generator;
import org.lenteja.mapper.handler.ColumnHandler;
import org.lenteja.mapper.handler.EnumColumnHandler;
import org.lenteja.mapper.handler.Handlers;

import cat.lechuga.jdbc.anno.Column;
import cat.lechuga.jdbc.anno.Enumerated;
import cat.lechuga.jdbc.anno.Generated;
import cat.lechuga.jdbc.anno.Handler;
import cat.lechuga.jdbc.anno.Id;
import cat.lechuga.jdbc.anno.Table;
import cat.lechuga.jdbc.anno.Transient;
import cat.lechuga.jdbc.reflect.Property;
import cat.lechuga.jdbc.reflect.PropertyScanner;
import cat.lechuga.jdbc.reflect.ReflectUtils;

public class EntityManagerFactory {

    public static class EntityManager<E> {

        final EntityMeta<E> entityMeta;

        public EntityManager(EntityMeta<E> entityMeta) {
            super();
            this.entityMeta = entityMeta;
        }

    }

    public static class EntityManagerOperations<E> {

        final EntityMeta<E> entityMeta;

        public EntityManagerOperations(EntityMeta<E> entityMeta) {
            super();
            this.entityMeta = entityMeta;
        }

        public IQueryObject update(E entity) throws Exception {
            QueryObject q = new QueryObject();
            q.append("update ");
            q.append(entityMeta.getTableName());
            q.append(" set ");
            {
                int c = 0;
                for (PropertyMeta p : entityMeta.regularProps) {
                    if (c > 0) {
                        q.append(",");
                    }
                    q.append(p.getColumnName());
                    q.append("=?");
                    q.addArg(p.getJdbcValue(entity));
                    c++;
                }
            }
            q.append(" where ");
            {
                int c = 0;
                for (PropertyMeta p : entityMeta.idProps) {
                    if (c > 0) {
                        q.append(" and ");
                    }
                    q.append(p.getColumnName());
                    q.append("=?");
                    q.addArg(p.getJdbcValue(entity));
                    c++;
                }
            }
            return q;
        }

        public IQueryObject loadById(Object id) {
            QueryObject q = new QueryObject();
            q.append("select ");
            {
                int c = 0;
                for (PropertyMeta p : entityMeta.allProps) {
                    if (c > 0) {
                        q.append(",");
                    }
                    q.append(p.getColumnName());
                    c++;
                }
            }
            q.append(" from ");
            q.append(entityMeta.getTableName());
            q.append(" where ");
            {
                int c = 0;
                for (PropertyMeta p : entityMeta.idProps) {
                    if (c > 0) {
                        q.append(" and ");
                    }
                    q.append(p.getColumnName());
                    q.append("=?");
                    q.addArg(p.getJdbcValue(1, id));
                    c++;
                }
            }
            return q;
        }
    }

    public <E> EntityManager<E> buildEntityManager(Class<E> entityClass) {
        EntityMeta<E> entityMeta = buildEntityMeta(entityClass);
        return new EntityManager<>(entityMeta);
    }

    protected <E> EntityMeta<E> buildEntityMeta(Class<E> entityClass) {
        PropertyScanner ps = new PropertyScanner();
        Map<String, Property> props = ps.propertyScanner(entityClass);

        final String tableName;
        if (entityClass.getAnnotation(Table.class) == null) {
            tableName = Conventions.tableNameOf(entityClass);
        } else {
            Table anno = entityClass.getAnnotation(Table.class);
            tableName = anno.value();
        }

        List<PropertyMeta> propMetas = new ArrayList<>();
        for (Property prop : props.values()) {

            if (prop.containsAnnotation(Transient.class)) {
                continue;
            }

            final String columnName;
            {
                if (prop.containsAnnotation(Column.class)) {
                    Column anno = prop.getAnnotation(Column.class);
                    columnName = anno.value();
                } else {
                    columnName = Conventions.columnNameOf(prop.getLastName());
                }
            }

            final boolean isId;
            {
                isId = prop.containsAnnotation(Id.class);
            }

            final ColumnHandler handler;
            {
                if (prop.containsAnnotation(Enumerated.class)) {
                    handler = new EnumColumnHandler(prop.getType());
                } else if (prop.containsAnnotation(Handler.class)) {
                    Handler anno = prop.getAnnotation(Handler.class);
                    Class<ColumnHandler> handlerClass = anno.value();
                    String[] handlerArgs = anno.args();
                    handler = ReflectUtils.newInstance(handlerClass, handlerArgs);
                } else {
                    handler = Handlers.getHandlerFor(prop.getType());
                }
            }

            final Generator generator;
            {
                if (prop.containsAnnotation(Generated.class)) {
                    Generated anno = prop.getAnnotation(Generated.class);
                    Class<? extends Generator> handlerClass = anno.value();
                    String[] handlerArgs = anno.args();
                    generator = ReflectUtils.newInstance(handlerClass, handlerArgs);
                } else {
                    generator = null;
                }
            }

            propMetas.add(new PropertyMeta(prop, columnName, isId, handler, generator));
        }

        return new EntityMeta<>(entityClass, tableName, propMetas);
    }

    public static class EntityMeta<E> {

        final Class<E> entityClass;
        final String tableName;

        final List<PropertyMeta> allProps;
        final List<PropertyMeta> idProps;
        final List<PropertyMeta> regularProps;

        public EntityMeta(Class<E> entityClass, String tableName, List<PropertyMeta> allProps) {
            super();
            this.entityClass = entityClass;
            this.tableName = tableName;
            this.allProps = allProps;

            this.idProps = new ArrayList<>();
            this.regularProps = new ArrayList<>();
            for (PropertyMeta p : allProps) {
                if (p.isId) {
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
            return "EntityMeta [entityClass=" + entityClass + ", tableName=" + tableName + ", allProps=" + allProps
                    + "]";
        }

    }

    public static class PropertyMeta {

        final Property prop;

        final String columnName;
        final boolean isId;
        final ColumnHandler handler;
        final Generator generator;

        public PropertyMeta(Property prop, String columnName, boolean isId, ColumnHandler handler,
                Generator generator) {
            super();
            this.prop = prop;
            this.columnName = columnName;
            this.isId = isId;
            this.handler = handler;
            this.generator = generator;
        }

        public Object getJdbcValue(Object entity) throws Exception {
            Object value = prop.get(entity);
            return handler.getJdbcValue(value);
        }

        public Object getJdbcValue(int propertyOffset, Object entity) {
            Object value = prop.get(propertyOffset, entity);
            return handler.getJdbcValue(value);
        }

        public void readValue(Object entity, ResultSet rs) throws SQLException {
            Object value = handler.readValue(rs, getColumnName());
            prop.set(entity, value);
        }

        public Property getProp() {
            return prop;
        }

        public String getColumnName() {
            return columnName;
        }

        public boolean isId() {
            return isId;
        }

        public ColumnHandler getHandler() {
            return handler;
        }

        public Generator getGenerator() {
            return generator;
        }

        @Override
        public String toString() {
            return "PropertyMeta [prop=" + prop + ", columnName=" + columnName + ", isId=" + isId + ", handler="
                    + handler + ", generator=" + generator + "]";
        }

    }

}