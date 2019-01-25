package cat.lechuga.reflect;

import java.lang.reflect.Method;

public class PropertyAccessor extends Accessor {

    private final String propertyName;
    private final Method setter;
    private final Method getter;

    public PropertyAccessor(Class<?> beanClass, String propertyName, Method setter, Method getter) {
        super(beanClass, getter.getReturnType());
        this.propertyName = propertyName;
        this.setter = setter;
        this.getter = getter;
    }

    @Override
    public String getPropertyName() {
        return propertyName;
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