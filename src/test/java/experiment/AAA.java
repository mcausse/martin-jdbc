package experiment;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.lenteja.jdbc.txproxy.EPropagation;
import org.lenteja.test.ExpTest.ExpId;

public class AAA {

    public static void main(String[] args) {

        new AAA().explore(Exp.class);

    }

    public void explore(Class<?> beanClass) {
        explore(beanClass, "");
    }

    public void explore(Class<?> beanClass, String path) {

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

            Class<?> propType = pd.getPropertyType();
            if (propType.getAnnotation(Embeddable.class) != null) {
                explore(propType, path + "." + pd.getName());
            } else {
                System.out.println(path + "." + pd.getName());
            }
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Embeddable {
    }

    @Embeddable
    public static class ExpId {

        Long idEns;
        Integer anyExp;
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

    public static class Exp {

        ExpId id;
        String name;
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
