package cat.lechuga.jdbc.reflect;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

public class Property {

    public final String fullName;
    public final List<Accessor> accessors;
    public final Set<Annotation> annos;

    public Property(String fullName, List<Accessor> accessors, Set<Annotation> annos) {
        super();
        this.fullName = fullName;
        this.accessors = accessors;
        this.annos = annos;
    }

    public <A extends Annotation> boolean containsAnnotation(Class<A> annotationClass) {
        for (Annotation a : annos) {
            if (a.annotationType().equals(annotationClass)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        for (Annotation a : annos) {
            if (a.annotationType().equals(annotationClass)) {
                return (A) a;
            }
        }
        throw new RuntimeException("internal error; not found: " + annotationClass);
    }

    public String getLastName() {
        return accessors.get(accessors.size() - 1).getPropertyName();
    }

    public Class<?> getType() {
        return accessors.get(accessors.size() - 1).getPropertyType();
    }

    public Object get(Object bean) {
        return get(0, bean);
    }

    public Object get(int propertyOffset, Object bean) {
        try {
            Object o = bean;
            for (int i = propertyOffset; i < accessors.size(); i++) {
                o = accessors.get(i).getValue(o);
                if (o == null) {
                    return null;
                }
            }
            return o;
        } catch (Exception e) {
            throw new RuntimeException("getting property: " + toString(), e);
        }
    }

    public void set(Object bean, Object value) {
        set(0, bean, value);
    }

    public void set(int propertyOffset, Object bean, Object value) {
        try {
            Object o = bean;
            for (int i = propertyOffset; i < accessors.size() - 1; i++) {
                Object o2 = accessors.get(i).getValue(o);
                if (o2 == null) {
                    o2 = accessors.get(i).getPropertyType().newInstance();
                    accessors.get(i).setValue(o, o2);
                }
                o = o2;
            }
            accessors.get(accessors.size() - 1).setValue(o, value);
        } catch (Exception e) {
            throw new RuntimeException("setting property: " + toString(), e);
        }
    }

    @Override
    public String toString() {
        return "Property [fullName=" + fullName + ", accessors=" + accessors + ", annos=" + annos + "]";
    }

}