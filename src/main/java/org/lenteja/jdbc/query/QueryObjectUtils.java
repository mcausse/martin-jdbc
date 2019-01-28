package org.lenteja.jdbc.query;

public interface QueryObjectUtils {

    public static String toString(IQueryObject q) {
        return toString(q.getQuery(), q.getArgs());
    }

    public static String toString(String query, Object[] args) {

        final StringBuilder r = new StringBuilder();
        r.append(query);
        r.append(" -- [");
        int c = 0;
        for (final Object o : args) {
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
