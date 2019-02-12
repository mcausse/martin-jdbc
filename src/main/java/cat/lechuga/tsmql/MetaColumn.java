package cat.lechuga.tsmql;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;

public class MetaColumn<E, T> implements IQueryObject {

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

    public MetaTable<E> getTable() {
        return table;
    }

    //////////////////////////////////////////////

    protected IQueryObject binaryOp(String op, T value) {
        QueryObject q = new QueryObject();
        q.append("{");
        q.append(table.getAlias());
        q.append(".");
        q.append(propertyName);
        q.append(op);
        q.append("?}");
        q.addArg(value);
        return q;
    }

    public IQueryObject eq(T value) {
        return binaryOp("=", value);
    }

    public IQueryObject ne(T value) {
        return binaryOp("<>", value);
    }

    public IQueryObject le(T value) {
        return binaryOp("<=", value);
    }

    public IQueryObject ge(T value) {
        return binaryOp(">=", value);
    }

    public IQueryObject lt(T value) {
        return binaryOp("<", value);
    }

    public IQueryObject gt(T value) {
        return binaryOp(">", value);
    }

    //////////////////////////////////////////////

    protected IQueryObject binaryOp(String op, MetaColumn<?, T> column) {
        QueryObject q = new QueryObject();
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

    public IQueryObject eq(MetaColumn<?, T> column) {
        return binaryOp("=", column);
    }

    public IQueryObject ne(MetaColumn<?, T> column) {
        return binaryOp("<>", column);
    }

    public IQueryObject le(MetaColumn<?, T> column) {
        return binaryOp("<=", column);
    }

    public IQueryObject ge(MetaColumn<?, T> column) {
        return binaryOp(">=", column);
    }

    public IQueryObject lt(MetaColumn<?, T> column) {
        return binaryOp("<", column);
    }

    public IQueryObject gt(MetaColumn<?, T> column) {
        return binaryOp(">", column);
    }

    //////////////////////////////////////////////

    protected IQueryObject unaryOp(String prefix, String postfix) {
        QueryObject q = new QueryObject();
        q.append(prefix);
        q.append("{");
        q.append(table.getAlias());
        q.append(".");
        q.append(propertyName);
        q.append("}");
        q.append(postfix);
        return q;
    }

    public IQueryObject isNull() {
        return unaryOp("", " is null");
    }

    public IQueryObject isNotNull() {
        return unaryOp("", " is not null");
    }

    public IQueryObject isTrue() {
        return unaryOp("", "=TRUE");
    }

    public IQueryObject isFalse() {
        return unaryOp("", "=FALSE");
    }

    //////////////////////////////////////////////

    protected IQueryObject unaryOpAs(String prefix, String postfix) {
        QueryObject q = new QueryObject();
        q.append(prefix);
        q.append("{");
        q.append(table.getAlias());
        q.append(".");
        q.append(propertyName);
        q.append("}");
        q.append(postfix);
        q.append(" as ");
        q.append("{");
        q.append(table.getAlias());
        q.append(".");
        q.append(propertyName);
        q.append("}");
        return q;
    }

    public IQueryObject min() {
        return unaryOpAs("min(", ")");
    }

    public IQueryObject max() {
        return unaryOpAs("max(", ")");
    }

    public IQueryObject sum() {
        return unaryOpAs("sum(", ")");
    }
    // TODO ...

    //////////////////////////////////////////////

    public IQueryObject in(List<T> values) {
        QueryObject q = new QueryObject();
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
    public IQueryObject in(T... values) {
        return in(Arrays.asList(values));
    }

    public IQueryObject notIn(List<T> values) {
        return Restrictions.not(in(values));
    }

    @SuppressWarnings("unchecked")
    public IQueryObject notIn(T... values) {
        return Restrictions.not(in(Arrays.asList(values)));
    }

    //////////////////////////////////////////////

    public IQueryObject between(T value1, T value2) {
        QueryObject q = new QueryObject();
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

    public IQueryObject like(ELike elike, String value) {
        QueryObject q = new QueryObject();
        q.append("{");
        q.append(table.getAlias());
        q.append(".");
        q.append(propertyName);
        q.append(" like ?}");
        q.addArg(elike.process(value));
        return q;
    }

    // XXX birgueria en MQL (o millor dit, en Martincho-QL)
    public IQueryObject ilike(ELike elike, String value) {
        QueryObject q = new QueryObject();
        q.append("upper({");
        q.append(table.getAlias());
        q.append(".");
        q.append(propertyName);
        q.append(") like upper(?)}");
        q.addArg(elike.process(value));
        return q;
    }

    ///////////////////////////////////////////////////

    @Override
    public String getQuery() {
        return "{" + getAlias() + "." + getPropertyName() + "}";
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
        return "MetaColumn [propertyName=" + propertyName + "]";
    }

}