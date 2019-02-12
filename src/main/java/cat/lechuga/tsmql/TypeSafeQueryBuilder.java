package cat.lechuga.tsmql;

import java.util.Arrays;
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
    // private final QueryBuilder qb;
    private final Map<String, Class<?>> aliases;

    public TypeSafeQueryBuilder(EntityManager em) {
        super();
        this.em = em;
        this.qo = new QueryObject();
        // this.qb = new QueryBuilder(em);
        this.aliases = new LinkedHashMap<>();
    }

    // TODO protected?
    public TypeSafeQueryBuilder addAlias(MetaTable<?> table) {
        // qb.addAlias(table.getAlias(), table.getEntityClass());
        aliases.put(table.getAlias(), table.getEntityClass());
        return this;
    }

    public TypeSafeQueryBuilder select(MetaColumn<?, ?>... columns) {
        append("select {}", Restrictions.composition(", ", Arrays.asList(columns)));
        return this;
    }

    public TypeSafeQueryBuilder from(MetaTable<?> table) {
        addAlias(table);
        append(" from {}", table.all(), table);
        return this;
    }

    public TypeSafeQueryBuilder selectFrom(MetaColumn<?, ?> column) {
        append("select {} from {}", column, column.getTable());
        return this;
    }

    public TypeSafeQueryBuilder selectFrom(MetaTable<?> table) {
        addAlias(table);
        append("select {} from {}", table.all(), table);
        return this;
    }

    public TypeSafeQueryBuilder joinOn(MetaTable<?> table, IQueryObject on) {
        addAlias(table);
        append(" join {} on {}", table, on);
        return this;
    }

    public TypeSafeQueryBuilder where(IQueryObject criterion) {
        append(" where {}", criterion);
        return this;
    }

    public TypeSafeQueryBuilder and(IQueryObject criterion) {
        append(" and {}", criterion);
        return this;
    }

    public TypeSafeQueryBuilder orderBy(TOrders orders) {
        append(" order by {}", orders);
        return this;
    }

    public TypeSafeQueryBuilder groupBy(MetaColumn<?, ?>... columns) {
        append(" group by {}", Restrictions.composition(", ", Arrays.asList(columns)));
        return this;
    }

    public TypeSafeQueryBuilder having(IQueryObject criterion) {
        append(" having {}", criterion);
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

            {
                String exp = queryFragment.substring(p, p2);
                qo.append(exp);
                // qb.append(exp);
            }

            IQueryObject arg = args[argIndex++];
            qo.append(arg);
            // qb.append(arg.getQuery(), arg.getArgs());

            p = p2 + "{}".length();
        }

        String exp = queryFragment.substring(p);
        qo.append(exp);
        // qb.append(exp);

        return this;
    }

    public IQueryObject getMqlQueryObject() {
        return qo;
    }

    // public IQueryObject getQueryObject() {
    // return qb.getQueryObject();
    // }

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