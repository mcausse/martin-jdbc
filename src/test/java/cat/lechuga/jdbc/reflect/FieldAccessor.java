package cat.lechuga.jdbc.reflect;

import java.lang.reflect.Field;

public class FieldAccessor extends Accessor {

    private final Field field;

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
