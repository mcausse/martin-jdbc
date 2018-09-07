package org.lenteja.jdbc.query;

public class QueryObjectUtils {

    public static String toString(final IQueryObject q) {
        final StringBuilder r = new StringBuilder();
        r.append(q.getQuery());
        r.append(" -- [");
        int c = 0;
        for (final Object o : q.getArgs()) {
            if (c > 0) {
                r.append(", ");
            }
            r.append(o);
            if (o != null) {
                r.append("(");
                r.append(o.getClass().getSimpleName());
                r.append(")");
            }
            c++;
        }
        r.append("]");
        return r.toString();
    }

}
