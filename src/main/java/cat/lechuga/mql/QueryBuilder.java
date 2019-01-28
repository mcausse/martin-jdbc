package cat.lechuga.mql;

import java.util.LinkedHashMap;
import java.util.Map;

import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;

import cat.lechuga.EntityMeta;
import cat.lechuga.EntityMetable;
import cat.lechuga.Facaded;
import cat.lechuga.FacadedMapable;
import cat.lechuga.Mapable;

public class QueryBuilder {

    private final QueryFormatter queryFormatter;

    private final Map<String, EntityMeta<?>> aliases;
    private final QueryObject qo;

    public QueryBuilder(QueryFormatter queryFormatter) {
        super();
        this.queryFormatter = queryFormatter;
        this.aliases = new LinkedHashMap<>();
        this.qo = new QueryObject();
    }

    public QueryBuilder() {
        this(new DefaultQueryFormatter());
    }

    // =========================================================
    // =========================================================
    // =========================================================

    public QueryBuilder addAlias(String alias, EntityMetable<?> metable) {
        aliases.put(alias, metable.getEntityMeta());
        return this;
    }

    public QueryBuilder addAlias(String alias, EntityMeta<?> meta) {
        aliases.put(alias, meta);
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

    public <T> Executor<T> getExecutor(FacadedMapable<T> facadedMapable) {
        return new Executor<>(facadedMapable.getFacade(), qo, facadedMapable);
    }

    public <T> Executor<T> getExecutor(DataAccesFacade facade, Mapable<T> mapable) {
        return new Executor<>(facade, qo, mapable);
    }

    public <T> Executor<T> getExecutor(Facaded facaded, Mapable<T> mapable) {
        return new Executor<>(facaded.getFacade(), qo, mapable);
    }

    @Override
    public String toString() {
        return qo.toString();
    }

}
