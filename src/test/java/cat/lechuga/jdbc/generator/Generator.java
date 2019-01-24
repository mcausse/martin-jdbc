package cat.lechuga.jdbc.generator;

import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.mapper.autogen.ScalarHandler;

public abstract class Generator {

    final boolean beforeInsert;

    ScalarHandler<?> scalarHandler;

    public Generator(boolean beforeInsert) {
        super();
        this.beforeInsert = beforeInsert;
    }

    public ScalarHandler<?> getScalarHandler() {
        return scalarHandler;
    }

    public void setScalarHandler(ScalarHandler<?> scalarHandler) {
        this.scalarHandler = scalarHandler;
    }

    public Object generate(DataAccesFacade facade) {
        return facade.loadUnique(getQuery(), scalarHandler);
    }

    public boolean isBeforeInsert() {
        return beforeInsert;
    }

    protected abstract IQueryObject getQuery();

}
