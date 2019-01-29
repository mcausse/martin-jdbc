package cat.lechuga.tsmql;

import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;

import cat.lechuga.EntityManagerFactory;
import cat.lechuga.EntityMeta;
import cat.lechuga.Facaded;
import cat.lechuga.Mapable;
import cat.lechuga.mql.Executor;
import cat.lechuga.mql.QueryBuilder;

public class TypeSafeQueryBuilder {

    private final QueryObject qo; // mql query
    private final QueryBuilder qb;

    public TypeSafeQueryBuilder() {
        super();
        this.qo = new QueryObject();
        this.qb = new QueryBuilder();
    }

    public TypeSafeQueryBuilder addAlias(MetaTable<?> table) {
        EntityMeta<?> em = new EntityManagerFactory().buildEntityMeta(table.getEntityClass());
        qb.addAlias(table.getAlias(), em);
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
            qo.append(queryFragment.substring(p, p2));
            qb.append(queryFragment.substring(p, p2));

            Object arg = args[argIndex++];
            if (arg instanceof Criterion) {
                Criterion arg2 = (Criterion) arg;
                qo.append(arg2);
                qb.append(arg2.getQuery(), arg2.getArgs());
            } else if (arg instanceof MetaTable) {
                MetaTable<?> arg2 = (MetaTable<?>) arg;
                qo.append("{" + arg2.getAlias() + ".#}");
                qb.append("{" + arg2.getAlias() + ".#}");
            } else if (arg instanceof MetaColumn) {
                MetaColumn<?, ?> arg2 = (MetaColumn<?, ?>) arg;
                qo.append("{" + arg2.getAlias() + "." + arg2.getPropertyName() + "}");
                qb.append("{" + arg2.getAlias() + "." + arg2.getPropertyName() + "}");
            } else {
                throw new RuntimeException(String.valueOf(arg));
            }

            p = p2 + "{}".length();
        }

        qo.append(queryFragment.substring(p));
        qb.append(queryFragment.substring(p));

        return this;
    }

    public IQueryObject getMqlQueryObject() {
        return qo;
    }

    public IQueryObject getQueryObject() {
        return qb.getQueryObject();
    }

    public <T, FM extends Facaded & Mapable<T>> Executor<T> getExecutor(FM facadedMapable) {
        return qb.getExecutor(facadedMapable);
    }

    public <T> Executor<T> getExecutor(DataAccesFacade facade, Mapable<T> mapable) {
        return qb.getExecutor(facade, mapable);
    }

    public <T> Executor<T> getExecutor(Facaded facaded, Mapable<T> mapable) {
        return qb.getExecutor(facaded.getFacade(), mapable);
    }

    @Override
    public String toString() {
        return qb.toString();
    }

}