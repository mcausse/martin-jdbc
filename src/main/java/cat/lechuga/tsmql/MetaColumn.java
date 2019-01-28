package cat.lechuga.tsmql;

import java.util.Arrays;
import java.util.List;

import org.lenteja.jdbc.query.IQueryObject;

public class MetaColumn<E, T> {

    final MetaTable<E> table;
    final String propertyName;

    public MetaColumn(MetaTable<E> table, String propertyName) {
        super();
        this.propertyName = propertyName;
        this.table = table;
    }

    public String getAlias() {
        return table.getAlias();
    }

    public String getPropertyName() {
        return propertyName;
    }

    //////////////////////////////////////////////

    protected Criterion binaryOp(String op, T value) {
        Criterion q = new Criterion();
        q.append("{");
        q.append(table.getAlias());
        q.append(".");
        q.append(propertyName);
        q.append(op);
        q.append("?}");
        q.addArg(value);
        return q;
    }

    public Criterion eq(T value) {
        return binaryOp("=", value);
    }

    public Criterion ne(T value) {
        return binaryOp("<>", value);
    }

    public Criterion le(T value) {
        return binaryOp("<=", value);
    }

    public Criterion ge(T value) {
        return binaryOp(">=", value);
    }

    public Criterion lt(T value) {
        return binaryOp("<", value);
    }

    public IQueryObject gt(T value) {
        return binaryOp(">", value);
    }

    //////////////////////////////////////////////

    protected Criterion binaryOp(String op, MetaColumn<?, T> column) {
        Criterion q = new Criterion();
        q.append("{");
        q.append(table.getAlias());
        q.append(".");
        q.append(propertyName);
        q.append("}");
        q.append(op);
        q.append("{");
        q.append(column.table.getAlias());
        q.append(".");
        q.append(column.propertyName);
        q.append("}");
        return q;
    }

    public Criterion eq(MetaColumn<?, T> column) {
        return binaryOp("=", column);
    }

    public Criterion ne(MetaColumn<?, T> column) {
        return binaryOp("<>", column);
    }

    public Criterion le(MetaColumn<?, T> column) {
        return binaryOp("<=", column);
    }

    public Criterion ge(MetaColumn<?, T> column) {
        return binaryOp(">=", column);
    }

    public Criterion lt(MetaColumn<?, T> column) {
        return binaryOp("<", column);
    }

    public Criterion gt(MetaColumn<?, T> column) {
        return binaryOp(">", column);
    }

    //////////////////////////////////////////////

    protected Criterion unaryOp(String prefix, String postfix) {
        Criterion q = new Criterion();
        q.append(prefix);
        q.append("{");
        q.append(table.getAlias());
        q.append(".");
        q.append(propertyName);
        q.append("}");
        q.append(postfix);
        return q;
    }

    public Criterion isNull() {
        return unaryOp("", " is null");
    }

    public Criterion isNotNull() {
        return unaryOp("", " is not null");
    }

    public Criterion isTrue() {
        return unaryOp("", "=TRUE");
    }

    public Criterion isFalse() {
        return unaryOp("", "=FALSE");
    }

    //////////////////////////////////////////////

    public Criterion in(List<T> values) {
        Criterion q = new Criterion();
        q.append("{");
        q.append(table.getAlias());
        q.append(".");
        q.append(propertyName);
        q.append(" in (");
        int c = 0;
        for (T value : values) {
            if (c > 0) {
                q.append(",");
            }
            c++;
            q.append("?");
            q.addArg(value);
        }
        q.append(")}");
        return q;
    }

    @SuppressWarnings("unchecked")
    public Criterion in(T... values) {
        return in(Arrays.asList(values));
    }

    public Criterion notIn(List<T> values) {
        return Restrictions.not(in(values));
    }

    @SuppressWarnings("unchecked")
    public Criterion notIn(T... values) {
        return Restrictions.not(in(Arrays.asList(values)));
    }

    //////////////////////////////////////////////

    public Criterion between(T value1, T value2) {
        Criterion q = new Criterion();
        q.append("{");
        q.append(table.getAlias());
        q.append(".");
        q.append(propertyName);
        q.append(" between ? and ?}");
        q.addArg(value1);
        q.addArg(value2);
        return q;
    }
    //////////////////////////////////////////////

    public Criterion like(ELike elike, String value) {
        Criterion q = new Criterion();
        q.append("{");
        q.append(table.getAlias());
        q.append(".");
        q.append(propertyName);
        q.append(" like ?}");
        q.addArg(elike.process(value));
        return q;
    }

    // TODO com carai fer això en MQL (o millor dit, en Martincho-QL)
    // public Criterion ilike(ELike elike, String value) {
    // Criterion q = new Criterion();
    // q.append("upper({");
    // q.append(table.getAlias());
    // q.append(".");
    // q.append(propertyName);
    // q.append("}) like upper(?)}");
    // q.addArg(elike.process(value));
    // return q;
    // }

}