package cat.lechuga.jdbc.mql;

import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;

import cat.lechuga.jdbc.EntityMeta;
import cat.lechuga.jdbc.PropertyMeta;

public class DefaultQueryFormatter implements QueryFormatter {

    @Override
    public IQueryObject format(Map<String, EntityMeta<?>> aliases, String fragment, Object[] args) {

        QueryObject r = new QueryObject();

        String regexp = "\\{(\\w+)\\.((\\w|\\.|\\*|\\#)+)(.*?)\\}";

        Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(fragment);

        int currArgIndex = 0;

        final StringBuffer sb = new StringBuffer();
        while (m.find()) {

            // System.out.println("alias: " + m.group(1));
            // System.out.println("propy: " + m.group(2));
            // System.out.println("exprn: " + m.group(4));

            String alias = m.group(1);
            String propName = m.group(2);
            String expression = m.group(4);

            IQueryObject value = evaluateExpression(aliases, alias, propName, expression, args, currArgIndex);
            currArgIndex += value.getArgs().length;

            m.appendReplacement(sb, value.getQuery());
            r.addArgs(value.getArgsList());
        }
        m.appendTail(sb);

        if (currArgIndex != args.length) {
            throw new RuntimeException("unused argument at index " + currArgIndex);
        }

        r.append(sb.toString());
        return r;
    }

    private IQueryObject evaluateExpression(Map<String, EntityMeta<?>> aliases, String alias, String propName,
            String expression, Object[] args, int currArgIndex) {

        if (!aliases.containsKey(alias)) {
            throw new RuntimeException("alias not found: '" + alias + "', valid are: " + aliases.keySet());
        }
        EntityMeta<?> meta = aliases.get(alias);

        QueryObject r = new QueryObject();

        if (propName.equals("*")) {
            StringJoiner j = new StringJoiner(",");
            for (PropertyMeta p : meta.getAllProps()) {
                j.add(p.getColumnName());
            }
            r.append(j.toString());
        } else if (propName.equals("#")) {
            r.append(meta.getTableName());
            r.append(" ");
            r.append(alias);
        } else {

            PropertyMeta prop = null;
            for (PropertyMeta p : meta.getAllProps()) {
                if (p.getProp().getFullName().equals(propName)) {
                    prop = p;
                    break;
                }
            }
            if (prop == null) {
                throw new RuntimeException(
                        "property not defined: '" + meta.getEntityClass().getName() + "#" + propName);
            }

            r.append(alias);
            r.append(".");
            r.append(prop.getColumnName());
            r.append(expression);

            for (char c : expression.toCharArray()) {
                if (c == '?') {
                    if (args.length <= currArgIndex) {
                        throw new RuntimeException("expected one more argument");
                    }
                    Object arg = args[currArgIndex++];
                    arg = prop.getHandler().getJdbcValue(arg);
                    r.addArg(arg);
                }
            }
        }

        return r;
    }

}