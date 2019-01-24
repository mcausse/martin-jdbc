package cat.lechuga.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.mapper.Conventions;
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
import cat.lechuga.jdbc.generator.Generator;
import cat.lechuga.jdbc.reflect.Property;
import cat.lechuga.jdbc.reflect.PropertyScanner;
import cat.lechuga.jdbc.reflect.ReflectUtils;

public class EntityManagerFactory {

    public <E, ID> EntityManager<E, ID> buildEntityManager(DataAccesFacade facade, Class<E> entityClass) {
        EntityMeta<E> entityMeta = buildEntityMeta(entityClass);
        return new EntityManager<>(facade, entityMeta);
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
                    Class<? extends Generator> generatorClass = anno.value();
                    String[] generatorArgs = anno.args();
                    generator = ReflectUtils.newInstance(generatorClass, generatorArgs);
                } else {
                    generator = null;
                }
            }

            propMetas.add(new PropertyMeta(prop, columnName, isId, handler, generator));
        }

        return new EntityMeta<>(entityClass, tableName, propMetas);
    }

}