package cat.lechuga.tsmql;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public abstract class MetaTable<E> {

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

    public List<MetaColumn<E, ?>> getCols() {
        return cols;
    }

    //////////////////////////////////////////////

    public Criterion star() {
        Criterion c = new Criterion();
        c.append("{");
        c.append(alias);
        c.append(".*");
        c.append("}");
        return c;
    }

    public Criterion all() {
        Criterion c = new Criterion();

        StringJoiner j = new StringJoiner(", ");
        for (MetaColumn<E, ?> p : cols) {
            j.add("{" + alias + "." + p.getPropertyName() + "}");
        }
        c.append(j.toString());
        return c;
    }

    //////////////////////////////////////////////

}
