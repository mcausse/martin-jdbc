package cat.lechuga.generator.impl;

import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;

import cat.lechuga.generator.Generator;

public class HsqldbSequence<T> extends Generator {

    final String sequenceName;

    public HsqldbSequence(String sequenceName) {
        super(true);
        this.sequenceName = sequenceName;
    }

    @Override
    protected IQueryObject getQuery() {
        QueryObject q = new QueryObject();
        q.append("call next value for " + sequenceName);
        return q;
    }
}
