package cat.lechuga.tsmql;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;

import cat.lechuga.EntityManager;
import cat.lechuga.Mapable;
import cat.lechuga.mql.Executor;
import cat.lechuga.mql.QueryBuilder;

public class TypeSafeQueryBuilder {

    private final EntityManager em;
    private final QueryObject qo; // mql query
    private final Map<String, Class<?>> aliases;

    public TypeSafeQueryBuilder(EntityManager em) {
        super();
        this.em = em;
        this.qo = new QueryObject();
        this.aliases = new LinkedHashMap<>();
    }

    public TypeSafeQueryBuilder addAlias(MetaTable<?> table) {
        aliases.put(table.getAlias(), table.getEntityClass());
        return this;
    }

    public TypeSafeQueryBuilder append(String queryFragment, IQueryObject... args) {

        int argIndex = 0;
        int p = 0;
        while (true) {

            int p2 = queryFragment.indexOf("{}", p);
            if (p2 < 0) {
                break;
            }

            qo.append(queryFragment.substring(p, p2));
            qo.append(args[argIndex++]);

            p = p2 + "{}".length();
        }

        qo.append(queryFragment.substring(p));

        return this;
    }

    public IQueryObject getMqlQueryObject() {
        return qo;
    }

    public <T> Executor<T> getExecutor(Class<T> entityClass) {
        return getExecutor(em.getEntityMeta(entityClass));
    }

    public <T> Executor<T> getExecutor(Mapable<T> mapable) {
        QueryBuilder qb = new QueryBuilder(em);
        for (Entry<String, Class<?>> alias : aliases.entrySet()) {
            qb.addAlias(alias.getKey(), alias.getValue());
        }
        qb.append(qo.getQuery(), qo.getArgs());
        return qb.getExecutor(mapable);
    }

    @Override
    public String toString() {
        return qo.toString();
    }

}