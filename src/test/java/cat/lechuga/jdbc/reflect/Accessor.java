package cat.lechuga.jdbc.reflect;

public abstract class Accessor {

    private final Class<?> beanClass;
    private final Class<?> propertyType;

    public Accessor(Class<?> beanClass, Class<?> propertyType) {
        super();
        this.beanClass = beanClass;
        this.propertyType = propertyType;
    }

    protected void verifyTargetBeanClass(Object targetBean) {
        if (!beanClass.isAssignableFrom(targetBean.getClass())) {
            throw new RuntimeException(toString() + " expects an " + beanClass.getName() + " instance, but received: "
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
