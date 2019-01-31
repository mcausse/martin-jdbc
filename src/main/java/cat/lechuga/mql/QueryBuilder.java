package cat.lechuga.mql;

import java.util.LinkedHashMap;
import java.util.Map;

import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;

import cat.lechuga.EntityManager;
import cat.lechuga.EntityMeta;
import cat.lechuga.Mapable;

public class QueryBuilder {

    private final QueryFormatter queryFormatter;

    private final EntityManager em;

    private final Map<String, EntityMeta<?>> aliases;
    private final QueryObject qo;

    public QueryBuilder(EntityManager em, QueryFormatter queryFormatter) {
        super();
        this.em = em;
        this.queryFormatter = queryFormatter;
        this.aliases = new LinkedHashMap<>();
        this.qo = new QueryObject();
    }

    public QueryBuilder(EntityManager em) {
        this(em, new DefaultQueryFormatter());
    }

    public QueryBuilder addAlias(String alias, Class<?> entityClass) {
        aliases.put(alias, em.getEntityMeta(entityClass));
        return this;
    }

    // =========================================================
    // =========================================================
    // =========================================================

    public QueryBuilder append(String queryFragment, Object... args) {
        this.qo.append(queryFormatter.format(aliases, queryFragment, args));
        return this;
    }

    public IQueryObject getQueryObject() {
        return qo;
    }

    public <T> Executor<T> getExecutor(Class<T> entityClassResult) {
        EntityMeta<T> entityMeta = em.getEntityMeta(entityClassResult);
        return getExecutor(entityMeta);
    }

    public <T> Executor<T> getExecutor(Mapable<T> mapable) {
        return new Executor<>(em.getFacade(), qo, mapable);
    }

    @Override
    public String toString() {
        return qo.toString();
    }

}
