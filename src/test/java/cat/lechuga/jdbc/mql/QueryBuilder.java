package cat.lechuga.jdbc.mql;

import java.util.LinkedHashMap;
import java.util.Map;

import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.query.QueryObject;
import org.lenteja.mapper.Mapable;

import cat.lechuga.jdbc.EntityManager;
import cat.lechuga.jdbc.EntityMeta;

public class QueryBuilder {

    private final DataAccesFacade facade;
    private final QueryFormatter queryFormatter;

    private final Map<String, EntityMeta<?>> aliases;
    private final QueryObject qo;

    public QueryBuilder(DataAccesFacade facade, QueryFormatter queryFormatter) {
        super();
        this.facade = facade;
        this.queryFormatter = queryFormatter;
        this.aliases = new LinkedHashMap<>();
        this.qo = new QueryObject();
    }

    public QueryBuilder(DataAccesFacade facade) {
        this(facade, new DefaultQueryFormatter());
    }

    public QueryBuilder addAlias(String alias, EntityMeta<?> meta) {
        aliases.put(alias, meta);
        return this;
    }

    public QueryBuilder addAlias(String alias, EntityManager<?, ?> em) {
        addAlias(alias, em.getEntityMeta());
        return this;
    }

    public QueryBuilder append(String queryFragment, Object... args) {
        this.qo.append(queryFormatter.format(aliases, queryFragment, args));
        return this;
    }

    public QueryObject getQueryObject() {
        return qo;
    }

    public <T> Executor<T> getExecutor(Mapable<T> mapable) {
        return new Executor<>(facade, qo, mapable);
    }
}
