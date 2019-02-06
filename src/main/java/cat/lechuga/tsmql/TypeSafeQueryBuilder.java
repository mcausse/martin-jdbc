package cat.lechuga.tsmql;

import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;

import cat.lechuga.EntityManager;
import cat.lechuga.Mapable;
import cat.lechuga.mql.Executor;
import cat.lechuga.mql.QueryBuilder;

public class TypeSafeQueryBuilder {

    private final EntityManager em;
    private final QueryObject qo; // mql query
    private final QueryBuilder qb;

    public TypeSafeQueryBuilder(EntityManager em) {
        super();
        this.em = em;
        this.qo = new QueryObject();
        this.qb = new QueryBuilder(em);
    }

    public TypeSafeQueryBuilder addAlias(MetaTable<?> table) {
        qb.addAlias(table.getAlias(), table.getEntityClass());
        return this;
    }

    // public TypeSafeQueryBuilder<T> selectFrom(MetaColumn<?,T> column) {
    // append("select {} from {}", column, column.getTable());
    // return this;
    // }
    // public TypeSafeQueryBuilder<T> selectFrom(MetaTable<T> table) {
    // append("select {} from {}", table.all(), table);
    // return this;
    // }
    // public TypeSafeQueryBuilder<T> joinOn(MetaTable<T> table, Criterion on) {
    // append(" join {} on {}", table, on);
    // return this;
    // }
    // public TypeSafeQueryBuilder<T> where(Criterion criterion) {
    // append(" where {}", criterion);
    // return this;
    // }
    // public TypeSafeQueryBuilder<T> and(Criterion criterion) {
    // append(" and {}", criterion);
    // return this;
    // }
    // public TypeSafeQueryBuilder<T> orderBy(TOrders orders) {
    // append(" order by {}", orders);
    // return this;
    // }

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
                qb.append(exp);
            }

            IQueryObject arg = args[argIndex++];
            qo.append(arg);
            qb.append(arg.getQuery(), arg.getArgs());

            p = p2 + "{}".length();
        }

        String exp = queryFragment.substring(p);
        qo.append(exp);
        qb.append(exp);

        return this;
    }

    public IQueryObject getMqlQueryObject() {
        return qo;
    }

    public IQueryObject getQueryObject() {
        return qb.getQueryObject();
    }

    public <T> Executor<T> getExecutor(Class<T> entityClass) {
        return getExecutor(em.getEntityMeta(entityClass));
    }

    public <T> Executor<T> getExecutor(Mapable<T> mapable) {
        return qb.getExecutor(mapable);
    }

    @Override
    public String toString() {
        return qb.toString();
    }

}