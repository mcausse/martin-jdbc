package cat.lechuga.tsmql;

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

}