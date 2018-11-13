package org.lenteja.mapper.query;

import java.util.Arrays;
import java.util.List;

import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;

public class Restrictions {

    protected static IQueryObject composition(String op, List<? extends IQueryObject> qs) {
        QueryObject r = new QueryObject();
        for (int i = 0; i < qs.size(); i++) {
            if (i > 0) {
                r.append(op);
            }
            r.append(qs.get(i));
        }
        return r;
    }

    public static IQueryObject and(List<? extends IQueryObject> qs) {
        return composition(" and ", qs);
    }

    public static IQueryObject or(List<? extends IQueryObject> qs) {
        return composition(" or ", qs);
    }

    public static IQueryObject list(List<? extends IQueryObject> qs) {
        return composition(", ", qs);
    }

    public static IQueryObject and(IQueryObject... qs) {
        return and(Arrays.asList(qs));
    }

    public static IQueryObject or(IQueryObject... qs) {
        return or(Arrays.asList(qs));
    }

    public static IQueryObject list(IQueryObject... qs) {
        return list(Arrays.asList(qs));
    }

    public static IQueryObject not(IQueryObject q) {
        QueryObject r = new QueryObject();
        r.append("not(");
        r.append(q);
        r.append(")");
        return r;
    }

    public static IQueryObject all() {
        return new QueryObject("1=1");
    }
}
