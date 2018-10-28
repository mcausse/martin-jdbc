package org.lenteja.jdbc.query;

import java.util.ArrayList;
import java.util.List;

public class QueryObject implements IQueryObject {

    final StringBuilder query;
    final List<Object> params;

    public QueryObject() {
        super();
        this.query = new StringBuilder();
        this.params = new ArrayList<Object>();
    }

    public QueryObject(String sqlFragment) {
        this();
        this.query.append(sqlFragment);
    }

    public void append(String sqlFragment) {
        this.query.append(sqlFragment);
    }

    public void append(IQueryObject qo) {
        append(qo.getQuery());
        addArgs(qo.getArgsList());
    }

    public void addArg(Object paramValue) {
        this.params.add(paramValue);
    }

    public void addArgs(List<Object> paramValues) {
        this.params.addAll(paramValues);
    }

    @Override
    public String getQuery() {
        return query.toString();
    }

    @Override
    public Object[] getArgs() {
        return params.toArray();
    }

    @Override
    public List<Object> getArgsList() {
        return params;
    }

    @Override
    public String toString() {
        return QueryObjectUtils.toString(this);
    }

}
