package cat.lechuga.generator.impl;

import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;

import cat.lechuga.generator.Generator;

public class HsqldbIdentity<T> extends Generator {

    public HsqldbIdentity() {
        super(false);
    }

    @Override
    protected IQueryObject getQuery() {
        QueryObject q = new QueryObject();
        q.append("call identity()");
        return q;
    }
}