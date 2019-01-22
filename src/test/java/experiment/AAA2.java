package experiment;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;
import org.lenteja.mapper.autogen.ScalarHandler;
import org.lenteja.mapper.autogen.ScalarMappers;
import org.lenteja.mapper.query.Operations;

/**
 * <pre>
 * 
 *  1) rollo hql
 *  2) generar constants per a entitats, typed.
 *  3) amb aquestes constants de columna => fer criteria type-safe
 * 
 * </pre>
 * 
 * @author mhoms
 */
public class AAA2 {

    public static void main(String[] args) {
        List<Property> ps = new AAA2().propertyScanner(Exp.class);
        for (Property p : ps) {
            System.out.println(p);
        }
    }

    public List<Property> propertyScanner(Class<?> beanClass) {
        List<Property> r = new ArrayList<>();
        List<String> fullName = new ArrayList<>();
        List<Accessor> accessors = new ArrayList<>();
        Set<Annotation> annos = new LinkedHashSet<>();
        propertyScanner(r, beanClass, beanClass, fullName, accessors, annos);
        return r;
    }

    protected void propertyScanner(List<Property> r, Class<?> originalBeanClass, Class<?> targetBeanClass,
            List<String> fullName, List<Accessor> accessors, Set<Annotation> annos) {

        Class<?> p = targetBeanClass;
        while (!p.equals(Object.class)) {
            for (Field f : p.getDeclaredFields()) {

                if (f.getName().contains("$")) {
                    continue;
                }

                Method getter = null;
                Method setter = null;
                for (Method m : p.getDeclaredMethods()) {
                    if (isGetterMethodOf(f, m)) {
                        getter = m;
                    } else if (isSetterMethodOf(f, m)) {
                        setter = m;
                    }
                }

                final Accessor a;
                if (getter != null && setter != null) {
                    a = new PropertyAccessor(p, setter, getter);
                } else {
                    a = new FieldAccessor(p, f);
                }

                List<String> fullName2 = new ArrayList<>(fullName);
                List<Accessor> accessors2 = new ArrayList<>(accessors);
                Set<Annotation> annos2 = new LinkedHashSet<>(annos);
                fullName2.add(a.getPropertyName());
                accessors2.add(a);
                for (Annotation anno : f.getAnnotations()) {
                    annos2.add(anno);
                }

                if (f.getType().getAnnotation(Embeddable.class) == null) {
                    StringJoiner propfullName = new StringJoiner(".");
                    for (String part : fullName2) {
                        propfullName.add(part);
                    }
                    r.add(new Property(propfullName.toString(), accessors2, annos2));
                } else {
                    propertyScanner(r, originalBeanClass, f.getType(), fullName2, accessors2, annos2);
                }
            }
            p = p.getSuperclass();
        }

    }

    protected boolean isSetterMethodOf(Field f, Method m) {
        String methodName = "set" + Character.toUpperCase(f.getName().charAt(0)) + f.getName().substring(1);
        if (m.getName().equals(methodName) && m.getParameterTypes().length == 1
                && m.getReturnType().equals(void.class)) {
            return true;
        }
        return false;
    }

    protected boolean isGetterMethodOf(Field f, Method m) {
        String methodName;
        if (f.getType().equals(boolean.class)) {
            methodName = "is" + Character.toUpperCase(f.getName().charAt(0)) + f.getName().substring(1);
        } else {
            methodName = "get" + Character.toUpperCase(f.getName().charAt(0)) + f.getName().substring(1);
        }
        if (m.getName().equals(methodName) && m.getParameterTypes().length == 0
                && !m.getReturnType().equals(void.class)) {
            return true;
        }
        return false;
    }

    public static class Property {

        public final String fullName;
        public final List<Accessor> accessors;
        public final Set<Annotation> annos;

        public Property(String fullName, List<Accessor> accessors, Set<Annotation> annos) {
            super();
            this.fullName = fullName;
            this.accessors = accessors;
            this.annos = annos;
        }

        @Override
        public String toString() {
            return "Property [fullName=" + fullName + ", accessors=" + accessors + ", annos=" + annos + "]";
        }

    }

    public static abstract class Accessor {

        final Class<?> beanClass;
        final Class<?> propertyType;

        public Accessor(Class<?> beanClass, Class<?> propertyType) {
            super();
            this.beanClass = beanClass;
            this.propertyType = propertyType;
        }

        protected void verifyTargetBeanClass(Object targetBean) {
            if (!beanClass.isAssignableFrom(targetBean.getClass())) {
                throw new RuntimeException(
                        toString() + " expects an " + beanClass.getName() + " instance, but received: "
                                + targetBean.getClass().getName() + " -- " + targetBean.toString());
            }
        }

        protected void verifyPropertyValueClass(Object propertyValue) {
            if (propertyValue != null) {
                if (!propertyType.isAssignableFrom(propertyValue.getClass())) {
                    throw new RuntimeException(toString() + " expected property type: " + propertyType.getName()
                            + ", but received: " + propertyValue.getClass().getName());
                }
            }
        }

        public abstract String getPropertyName();

        public abstract void setValue(Object targetBean, Object propertyValue) throws Exception;

        public abstract Object getValue(Object targetBean) throws Exception;

        public Class<?> getBeanClass() {
            return beanClass;
        }

        public Class<?> getPropertyType() {
            return propertyType;
        }

        @Override
        public String toString() {
            return "Accessor[" + beanClass.getName() + "#" + getPropertyName() + "(" + propertyType.getName() + ")]";
        }

    }

    public static class FieldAccessor extends Accessor {

        final Field field;

        public FieldAccessor(Class<?> beanClass, Field field) {
            super(beanClass, field.getType());
            this.field = field;
        }

        @Override
        public String getPropertyName() {
            return field.getName();
        }

        @Override
        public void setValue(Object targetBean, Object propertyValue) throws Exception {
            verifyTargetBeanClass(targetBean);
            verifyPropertyValueClass(propertyValue);
            field.set(targetBean, propertyValue);
        }

        @Override
        public Object getValue(Object targetBean) throws Exception {
            verifyTargetBeanClass(targetBean);
            return field.get(targetBean);
        }

    }

    public static class PropertyAccessor extends Accessor {

        final Method setter;
        final Method getter;

        public PropertyAccessor(Class<?> beanClass, Method setter, Method getter) {
            super(beanClass, getter.getReturnType());
            this.setter = setter;
            this.getter = getter;
        }

        @Override
        public String getPropertyName() {
            return getter.getName();
        }

        @Override
        public void setValue(Object targetBean, Object propertyValue) throws Exception {
            verifyTargetBeanClass(targetBean);
            verifyPropertyValueClass(propertyValue);
            setter.invoke(targetBean, propertyValue);
        }

        @Override
        public Object getValue(Object targetBean) throws Exception {
            verifyTargetBeanClass(targetBean);
            return getter.invoke(targetBean);
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Table {
        String value() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Embeddable {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Id {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Column {
        String value() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Handler {
        Class<ColumnHandler> value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Enumerated {
        Class<? extends Enum<?>> value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Transient {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Generated {
        Class<Generator> value();
    }

    public static interface ColumnHandler {

        /**
         * donat el valor de bean, retorna el valor a tipus de jdbc, apte per a setejar
         * en un {@link QueryObject} o en {@link PreparedStatement}.
         */
        default Object getJdbcValue(Object value) {
            return value;
        }

        /**
         * retorna el valor de bean a partir del {@link ResultSet}.
         */
        Object readValue(ResultSet rs, String columnName) throws SQLException;
    }

    public static abstract class Generator {

        final ScalarHandler<?> handler;
        final boolean beforeInsert;

        public Generator(Class<?> columnClass, boolean beforeInsert) {
            super();
            this.handler = ScalarMappers.getScalarMapperFor(columnClass);
            this.beforeInsert = beforeInsert;
        }

        public Object generate(DataAccesFacade facade) {
            return new Operations().query(handler).append(getQuery()).getExecutor(facade).loadUnique();
        }

        public boolean isBeforeInsert() {
            return beforeInsert;
        }

        protected abstract IQueryObject getQuery();

    }

    @Embeddable
    public static class ExpId {

        Long idEns;
        Integer anyExp;
        @Column("num_exp")
        Long numExp;

        public ExpId() {
            super();
        }

        public ExpId(Long idEns, Integer anyExp, Long numExp) {
            super();
            this.idEns = idEns;
            this.anyExp = anyExp;
            this.numExp = numExp;
        }

        public Long getIdEns() {
            return idEns;
        }

        public void setIdEns(Long idEns) {
            this.idEns = idEns;
        }

        public Integer getAnyExp() {
            return anyExp;
        }

        public void setAnyExp(Integer anyExp) {
            this.anyExp = anyExp;
        }

        public Long getNumExp() {
            return numExp;
        }

        public void setNumExp(Long numExp) {
            this.numExp = numExp;
        }

        @Override
        public String toString() {
            return "ExpId [idEns=" + idEns + ", anyExp=" + anyExp + ", numExp=" + numExp + "]";
        }
    }

    @Table
    public static class Exp {

        @Id
        ExpId id;
        String name;
        @Column("fecha_ini")
        String fecIni;

        public Exp() {
            super();
        }

        public Exp(ExpId id, String name, String fecIni) {
            super();
            this.id = id;
            this.name = name;
            this.fecIni = fecIni;
        }

        public ExpId getId() {
            return id;
        }

        public void setId(ExpId id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFecIni() {
            return fecIni;
        }

        public void setFecIni(String fecIni) {
            this.fecIni = fecIni;
        }

        @Override
        public String toString() {
            return "Exp [id=" + id + ", name=" + name + ", fecIni=" + fecIni + "]";
        }
    }

}
