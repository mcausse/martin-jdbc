package org.lenteja.mapper.reflect;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

public class Accessor {

    protected final Class<?> beanClass;
    // id.login
    protected final String propertyPath;
    protected final String[] propertyNameParts;
    protected final List<PropertyDescriptor> propertyPathList = new ArrayList<>();

    public Accessor(Class<?> beanClass, String propertyPath) {
        super();
        this.propertyPath = propertyPath;
        this.beanClass = beanClass;
        this.propertyNameParts = propertyPath.split("\\.");

        try {
            Class<?> c = beanClass;
            for (String part : propertyNameParts) {
                PropertyDescriptor pd = findProp(c, part);
                this.propertyPathList.add(pd);
                c = pd.getPropertyType();
            }
        } catch (Exception e) {
            throw new RuntimeException("describing " + beanClass.getName() + "#" + propertyPath, e);
        }
    }

    protected static PropertyDescriptor findProp(Class<?> beanClass, String propertyName) {
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
                return pd;
            }
        }
        throw new RuntimeException("property not found: '" + beanClass.getName() + "#" + propertyName + "'");
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
                o = propertyPathList.get(i).getReadMethod().invoke(o);
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
                PropertyDescriptor p = propertyPathList.get(i);
                Object o2 = p.getReadMethod().invoke(o);
                if (o2 == null) {
                    o2 = p.getPropertyType().newInstance();
                    p.getWriteMethod().invoke(o, o2);
                }
                o = o2;
            }
            propertyPathList.get(propertyPathList.size() - 1).getWriteMethod().invoke(o, propertyValue);
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
