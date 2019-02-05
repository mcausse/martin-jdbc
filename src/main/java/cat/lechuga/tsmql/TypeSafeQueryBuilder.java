package cat.lechuga.tsmql;

import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;

import cat.lechuga.EntityManager;
import cat.lechuga.Mapable;
import cat.lechuga.mql.Executor;
import cat.lechuga.mql.QueryBuilder;
import cat.lechuga.tsmql.TOrders.TOrder;

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

    public TypeSafeQueryBuilder append(String queryFragment, Object... args) {

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

            Object arg = args[argIndex++];
            if (arg instanceof Criterion) {
                Criterion arg2 = (Criterion) arg;
                qo.append(arg2);
                qb.append(arg2.getQuery(), arg2.getArgs());
            } else if (arg instanceof MetaTable) {
                MetaTable<?> arg2 = (MetaTable<?>) arg;
                String exp = "{" + arg2.getAlias() + ".#}";
                qo.append(exp);
                qb.append(exp);
            } else if (arg instanceof MetaColumn) {
                MetaColumn<?, ?> arg2 = (MetaColumn<?, ?>) arg;
                String exp = "{" + arg2.getAlias() + "." + arg2.getPropertyName() + "}";
                qo.append(exp);
                qb.append(exp);
            } else if (arg instanceof TOrders) {
                TOrders<?> arg2 = (TOrders<?>) arg;
                int c = 0;
                for (TOrder<?> o : arg2.getOrders()) {
                    if (c > 0) {
                        qo.append(",");
                    }
                    c++;
                    MetaColumn<?, ?> metac = o.getMetaColumn();
                    String exp = "{" + metac.getAlias() + "." + metac.getPropertyName() + "} " + o.getOrder();
                    qo.append(exp);
                    qb.append(exp);
                }
            } else if (arg instanceof TOrder) {
                TOrder<?> o = (TOrder<?>) arg;
                MetaColumn<?, ?> metac = o.getMetaColumn();
                String exp = "{" + metac.getAlias() + "." + metac.getPropertyName() + "} " + o.getOrder();
                qo.append(exp);
                qb.append(exp);
            } else {
                throw new RuntimeException(String.valueOf(arg));
            }

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