package cat.lechuga.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import cat.lechuga.reflect.anno.Embeddable;

/**
 * <pre>
 *
 * XXX
 *
 *  1) rollo hql
 *  2) generar constants per a entitats, typed.
 *  3) amb aquestes constants de columna => fer criteria type-safe
 *
 * </pre>
 *
 * @author mhoms
 */
public class PropertyScanner {

    public Map<String, Property> propertyScanner(Class<?> beanClass) {
        List<Property> props = new ArrayList<>();
        List<String> fullName = new ArrayList<>();
        List<Accessor> accessors = new ArrayList<>();
        Set<Annotation> annos = new LinkedHashSet<>();
        propertyScanner(props, beanClass, beanClass, fullName, accessors, annos);

        Map<String, Property> r = new LinkedHashMap<>();
        for (Property p : props) {
            r.put(p.getFullName(), p);
        }
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
                    a = new PropertyAccessor(p, f.getName(), setter, getter);
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

}
