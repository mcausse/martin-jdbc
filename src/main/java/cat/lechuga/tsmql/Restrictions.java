package cat.lechuga.tsmql;

import java.util.Arrays;
import java.util.List;

public class Restrictions {

    protected static Criterion composition(String op, List<? extends Criterion> qs) {
        Criterion r = new Criterion();
        for (int i = 0; i < qs.size(); i++) {
            if (i > 0) {
                r.append(op);
            }
            r.append(qs.get(i));
        }
        return r;
    }

    // public static IQueryObject subQueryAliasedAsTable(IQueryObject subquery,
    // String alias) {
    // QueryObject r = new QueryObject();
    // r.append("(");
    // r.append(subquery);
    // r.append(") ");
    // r.append(alias);
    // return r;
    // }

    public static Criterion and(List<? extends Criterion> qs) {
        return composition(" and ", qs);
    }

    public static Criterion or(List<? extends Criterion> qs) {
        return composition(" or ", qs);
    }

    public static Criterion list(List<? extends Criterion> qs) {
        return composition(", ", qs);
    }

    public static Criterion and(Criterion... qs) {
        return and(Arrays.asList(qs));
    }

    public static Criterion or(Criterion... qs) {
        return or(Arrays.asList(qs));
    }

    public static Criterion list(Criterion... qs) {
        return list(Arrays.asList(qs));
    }

    public static Criterion not(Criterion q) {
        Criterion r = new Criterion();
        r.append("not(");
        r.append(q);
        r.append(")");
        return r;
    }

    // public static Object orderBy(List<Order<?>> orders) {
    // Criterion r = new Criterion();
    // for (Order<?> o : orders) {
    // r.append("not(");
    // o.getPropName()
    // r.append("not(");
    // r.append(q);
    // r.append(")");
    // }
    // return r;
    // }

    // public static Criterion all() {
    // return new Criterion("1=1");
    // }

}
