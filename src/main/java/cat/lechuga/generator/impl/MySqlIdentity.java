package cat.lechuga.generator.impl;

import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;

import cat.lechuga.generator.Generator;

public class MySqlIdentity<T> extends Generator {

    public MySqlIdentity() {
        super(false);
    }

    @Override
    protected IQueryObject getQuery() {
        QueryObject q = new QueryObject();
        q.append("select last_insert_id()");
        return q;
    }
}
