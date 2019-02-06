package cat.lechuga.tsmql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;

public abstract class MetaTable<E> implements IQueryObject {

    final Class<E> entityClass;
    final String alias;
    final List<MetaColumn<E, ?>> cols = new ArrayList<>();

    public MetaTable(Class<E> entityClass, String alias) {
        super();
        this.entityClass = entityClass;
        this.alias = alias;
    }

    public <T> MetaColumn<E, T> addColumn(String propertyName) {
        MetaColumn<E, T> c = new MetaColumn<>(this, propertyName);
        this.cols.add(c);
        return c;
    }

    public Class<E> getEntityClass() {
        return entityClass;
    }

    public String getAlias() {
        return alias;
    }

    //////////////////////////////////////////////

    public IQueryObject star() {
        QueryObject c = new QueryObject();
        c.append("{");
        c.append(alias);
        c.append(".*");
        c.append("}");
        return c;
    }

    public IQueryObject all() {
        QueryObject c = new QueryObject();

        StringJoiner j = new StringJoiner(", ");
        for (MetaColumn<E, ?> p : cols) {
            j.add("{" + alias + "." + p.getPropertyName() + "}");
        }
        c.append(j.toString());
        return c;
    }

    //////////////////////////////////////////////

    @Override
    public String getQuery() {
        return "{" + getAlias() + ".#}";
    }

    @Override
    public Object[] getArgs() {
        return new Object[] {};
    }

    @Override
    public List<Object> getArgsList() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "MetaTable [entityClass=" + entityClass + ", alias=" + alias + ", cols=" + cols + "]";
    }

}
