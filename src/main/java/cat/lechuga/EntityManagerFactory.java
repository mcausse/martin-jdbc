package cat.lechuga;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.lenteja.jdbc.DataAccesFacade;

import cat.lechuga.anno.Column;
import cat.lechuga.anno.EntityListeners;
import cat.lechuga.anno.Enumerated;
import cat.lechuga.anno.Generated;
import cat.lechuga.anno.Handler;
import cat.lechuga.anno.Id;
import cat.lechuga.anno.Table;
import cat.lechuga.anno.Transient;
import cat.lechuga.generator.Generator;
import cat.lechuga.generator.ScalarHandler;
import cat.lechuga.generator.ScalarMappers;
import cat.lechuga.handler.ColumnHandler;
import cat.lechuga.handler.EnumColumnHandler;
import cat.lechuga.handler.Handlers;
import cat.lechuga.reflect.Property;
import cat.lechuga.reflect.PropertyScanner;
import cat.lechuga.reflect.ReflectUtils;

public class EntityManagerFactory {

    private final DataAccesFacade facade;

    public EntityManagerFactory(DataAccesFacade facade) {
        super();
        this.facade = facade;
    }

    public <E, ID> EntityManager<E, ID> buildEntityManager(Class<E> entityClass) {
        EntityMeta<E> entityMeta = buildEntityMeta(entityClass);
        return new EntityManager<>(facade, entityMeta);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <E> EntityMeta<E> buildEntityMeta(Class<E> entityClass) {
        PropertyScanner ps = new PropertyScanner();
        Map<String, Property> props = ps.propertyScanner(entityClass);

        final String tableName;
        if (entityClass.getAnnotation(Table.class) == null) {
            tableName = Conventions.tableNameOf(entityClass);
        } else {
            Table anno = entityClass.getAnnotation(Table.class);
            tableName = anno.value();
        }

        final List<EntityListener<E>> listeners;
        {
            if (entityClass.getAnnotation(EntityListeners.class) == null) {
                listeners = null;
            } else {
                listeners = new ArrayList<>();
                EntityListeners anno = entityClass.getAnnotation(EntityListeners.class);
                Class<? extends EntityListener<?>>[] listenerClasses = anno.value();
                for (Class<? extends EntityListener<?>> listenerClass : listenerClasses) {
                    listeners.add((EntityListener<E>) ReflectUtils.newInstance(listenerClass));
                }
            }
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
                    handler = new EnumColumnHandler((Class<Enum>) prop.getType());
                } else if (prop.containsAnnotation(Handler.class)) {
                    Handler anno = prop.getAnnotation(Handler.class);
                    Class<? extends ColumnHandler> handlerClass = anno.value();
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
                    Class<? extends Generator> generatorClass = anno.value();
                    String[] generatorArgs = anno.args();
                    generator = ReflectUtils.newInstance(generatorClass, generatorArgs);

                    ScalarHandler<Object> scalarMapper = ScalarMappers.getScalarMapperFor(prop.getType());
                    generator.setScalarHandler(scalarMapper);
                } else {
                    generator = null;
                }
            }

            propMetas.add(new PropertyMeta(prop, columnName, isId, handler, generator));
        }

        return new EntityMeta<>(entityClass, tableName, listeners, propMetas);
    }

}