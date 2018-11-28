package org.lenteja.mapper.reflect;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

// XXX i adaptar Accessor per a accedir a propietats publiques (sense
// getter/setters), seria la polla

public class Accessor {

    protected final Class<?> beanClass;
    // id.login
    protected final String propertyPath;
    protected final String[] propertyNameParts;
    protected final List<PropAccessor> propertyPathList = new ArrayList<>();

    public Accessor(Class<?> beanClass, String propertyPath) {
        super();
        this.propertyPath = propertyPath;
        this.beanClass = beanClass;
        this.propertyNameParts = propertyPath.split("\\.");

        try {
            Class<?> c = beanClass;
            for (String part : propertyNameParts) {
                PropAccessor pd = findGetSetProp(c, part);
                if (pd == null) {

                    pd = findFieldProp(c, part);

                    if (pd == null) {
                        throw new RuntimeException("property not found: '" + beanClass.getName() + "#" + part + "'");
                    }
                }
                this.propertyPathList.add(pd);
                c = pd.getPropertyType();
            }
        } catch (Exception e) {
            throw new RuntimeException("describing " + beanClass.getName() + "#" + propertyPath, e);
        }
    }

    /**
     * @return null si no troba field
     */
    protected FieldAccessor findFieldProp(Class<?> beanClass, String propertyName) {

        try {
            Field field = beanClass.getField(propertyName);
            return new FieldAccessor(beanClass, field);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return null si no troba getter/setter
     */
    protected static GetSetAccessor findGetSetProp(Class<?> beanClass, String propertyName) {
        BeanInfo info;
        try {
            info = Introspector.getBeanInfo(beanClass);
        } catch (IntrospectionException e) {
            throw new RuntimeException("describing " + beanClass.getName(), e);
        }
        PropertyDescriptor[] pds = info.getPropertyDescriptors();
        for (PropertyDescriptor pd : pds) {
            if (pd.getName().equals("class") || pd.getName().contains("$")) {
                continue;
            }
            if (pd.getName().equals(propertyName)) {
                return new GetSetAccessor(pd);
            }
        }
        return null;
    }

    public static interface PropAccessor {

        Class<?> getPropertyType();

        void set(Object targetBean, Object value);

        Object get(Object targetBean);

    }

    public static class GetSetAccessor implements PropAccessor {

        final PropertyDescriptor pd;

        public GetSetAccessor(PropertyDescriptor pd) {
            super();
            this.pd = pd;
        }

        @Override
        public Class<?> getPropertyType() {
            return pd.getPropertyType();
        }

        @Override
        public void set(Object targetBean, Object value) {
            try {
                pd.getWriteMethod().invoke(targetBean, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Object get(Object targetBean) {
            try {
                return pd.getReadMethod().invoke(targetBean);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class FieldAccessor implements PropAccessor {

        final Class<?> targetClass;
        final Field field;

        public FieldAccessor(Class<?> targetClass, Field field) {
            super();
            this.targetClass = targetClass;
            this.field = field;
        }

        @Override
        public Class<?> getPropertyType() {
            return field.getType();
        }

        @Override
        public void set(Object targetBean, Object value) {
            try {
                field.set(targetBean, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Object get(Object targetBean) {
            try {
                return field.get(targetBean);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Object get(Object bean) {
        return get(bean, 0);
    }

    public void set(Object bean, Object propertyValue) {
        set(bean, 0, propertyValue);
    }

    public Object get(Object bean, int startIndex) {
        try {
            Object o = bean;
            for (int i = startIndex; i < propertyPathList.size(); i++) {
                o = propertyPathList.get(i).get(o);
                if (o == null) {
                    return null;
                }
            }
            return o;
        } catch (Exception e) {
            throw new RuntimeException("invoking setter of " + beanClass.getName() + "#" + this.propertyPath, e);
        }
    }

    public void set(Object bean, int startIndex, Object propertyValue) {
        try {
            Object o = bean;
            for (int i = startIndex; i < propertyPathList.size() - 1; i++) {
                PropAccessor p = propertyPathList.get(i);
                Object o2 = p.get(o);
                if (o2 == null) {
                    o2 = p.getPropertyType().newInstance();
                    p.set(o, o2);
                }
                o = o2;
            }
            propertyPathList.get(propertyPathList.size() - 1).set(o, propertyValue);
        } catch (Exception e) {
            throw new RuntimeException("invoking setter of " + beanClass.getName() + "#" + this.propertyPath
                    + " with value '" + propertyValue + "'", e);
        }
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public String getPropertyName() {
        return propertyPath;
    }

    public Class<?> getPropertyFinalType() {
        return propertyPathList.get(propertyPathList.size() - 1).getPropertyType();
    }

    @Override
    public String toString() {
        return beanClass + "#" + propertyPath;
    }

}
