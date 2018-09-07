package org.lenteja.mapper.query;

import java.util.ArrayList;
import java.util.List;

import org.lenteja.Aliasable;
import org.lenteja.Mapable;
import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;
import org.lenteja.jdbc.query.QueryObjectUtils;

public class Query<E> implements IQueryObject {

    final Mapable<E> mapable;

    final StringBuilder query;
    final List<Object> params;

    public Query(Mapable<E> mapable) {
        super();
        this.mapable = mapable;
        this.query = new StringBuilder();
        this.params = new ArrayList<>();
    }

    public Query<E> append(String queryFragment, Object... params) {

        int p = 0;
        int paramIndex = 0;
        while (true) {
            int pos = queryFragment.indexOf("{}", p);
            if (pos < 0) {
                break;
            }

            this.query.append(queryFragment.substring(p, pos));
            Object param = params[paramIndex++];
            IQueryObject paramResult = evaluate(param);
            this.query.append(paramResult.getQuery());
            this.params.addAll(paramResult.getArgsList());

            p = pos + "{}".length();
        }
        this.query.append(queryFragment.substring(p));

        return this;
    }

    protected IQueryObject evaluate(Object param) {
        if (param instanceof IQueryObject) {
            return (IQueryObject) param;
        } else if (param instanceof Aliasable) {
            Aliasable t = (Aliasable) param;
            QueryObject r = new QueryObject();
            r.append(t.getAliasedName());
            return r;
        } else {
            throw new RuntimeException(param.getClass().getName());
        }
    }

    public Executor<E> getExecutor(DataAccesFacade facade) {
        return new Executor<E>(facade, this, mapable);
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
