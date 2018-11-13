package org.lenteja.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.mapper.Column;
import org.lenteja.mapper.Table;
import org.lenteja.mapper.query.Order;

/**
 * <pre>
    begin() select() from<A>(A) {join<B>(B) on<A,B>()} {where()} [groupBy() [having()]] [orderBy()]
 * </pre>
 * 
 * L'algoritme pinta recursiu. Per cada producció "n" (token), essent el primer
 * token sempre "begin()":
 * 
 * <pre>
    interface A {
        nexts(A) a();
    }

    interface nexts(n) implements next(n)[0]..next(n)[k] {
        nexts(next(n)[0]) next(n)[0]()
        nexts(next1) next1()
        ...
        nexts(nextK) nextK()
    }
 * </pre>
 * 
 * @author mhoms
 *
 */
public class DslGen {

    public static void main(String[] args) {

        Prod select = new Prod("select", "select(Column<?,?>... columns)", "selectAll()", "selectStar()");
        Prod from = new Prod("from", "from(Table<?> table)", "from(Select selectQuery)");
        Prod join = new Prod("join", "join(Table<?> table)", "join(Select selectQuery)");
        Prod on = new Prod("on", "on(IQueryObject... restrictions)");
        Prod where = new Prod("where", "where(IQueryObject... restrictions)");
        Prod groupBy = new Prod("groupBy", "groupBy(Column<?,?>... columns)");
        Prod having = new Prod("having", "having(IQueryObject... restrictions)");
        Prod orderBy = new Prod("orderBy", "orderBy(List<Order<?>> orders)");
        Prod END = new Prod("execute", "Object execute()");

        // Prod update = new Prod("update", "update(Table<?> table)");
        // Prod set = new Prod("set", "set(IQueryObject... restrictions)");
        // Prod updateWhere = new Prod("where2", "where(IQueryObject... restrictions)");

        List<Prod> roots = new ArrayList<>();
        roots.add(select);
        // roots.add(update);

        // update.addNext(set);
        // set.addNext(updateWhere);
        // set.setPrefix("Update");
        // updateWhere.addNext(END);
        // updateWhere.setPrefix("Update");

        select.addNext(from);
        from.addNext(join);
        from.addNext(where);
        from.addNext(groupBy);
        from.addNext(orderBy);
        from.addNext(END);
        join.addNext(on);
        on.addNext(where);
        on.addNext(groupBy);
        on.addNext(orderBy);
        on.addNext(END);
        where.addNext(groupBy);
        where.addNext(orderBy);
        where.addNext(END);
        groupBy.addNext(having);
        groupBy.addNext(orderBy);
        groupBy.addNext(END);
        having.addNext(orderBy);
        having.addNext(END);
        orderBy.addNext(END);

        Set<Prod> explored = new HashSet<>();
        Set<String> generatedClassNames = new HashSet<>();
        Set<String> methodDefs = new HashSet<>();
        for (Prod root : roots) {
            System.out.println(root.toString(explored, generatedClassNames, methodDefs));
        }

        StringBuilder r = new StringBuilder();
        r.append("public class Impl implements ");
        {
            StringJoiner j = new StringJoiner(",");
            for (String p : generatedClassNames) {
                j.add(p);
            }
            r.append(j);
        }
        r.append("{\n");
        for (String methodDef : methodDefs) {
            r.append("\t@Override\n");
            r.append(methodDef);
            r.append("{\n");
            r.append("\t\treturn this;\n");
            r.append("\t}\n");
            r.append("\n");
        }
        r.append("}\n");

        System.out.println(r);

        //

        // new DslGen().new Impl().selectAll().from(new DogTable()).execute();

    }

    public static class Prod {

        final String name;
        final String[] paramVersions;
        final List<Prod> nexts;

        String prefix;

        public Prod(String name, String... paramVersions) {
            super();
            this.name = name;
            this.paramVersions = paramVersions;
            this.nexts = new ArrayList<>();
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public void addNext(Prod p) {
            this.nexts.add(p);
        }

        public String getNameAsClassName() {
            if (prefix == null) {
                return Character.toUpperCase(name.charAt(0)) + name.substring(1);
            } else {
                return prefix + Character.toUpperCase(name.charAt(0)) + name.substring(1);
            }
        }

        public String getNextsAsClassName() {
            StringJoiner j = new StringJoiner("_");
            for (Prod next : nexts) {
                j.add(next.getNameAsClassName());
            }
            return j.toString();
        }

        public String getNextsInterfaceNames() {
            StringJoiner j = new StringJoiner(",");
            for (Prod next : nexts) {
                j.add(next.getNameAsClassName());
            }
            return j.toString();
        }

        public String toString(Set<Prod> explored, Set<String> generatedClassNames, Set<String> methodDefs) {

            StringBuilder r = new StringBuilder();
            r.append("public interface " + getNameAsClassName() + "{\n");
            for (String paramVersion : paramVersions) {
                String methodDef = "\tpublic " + getNextsAsClassName() + " " + paramVersion;
                r.append(methodDef + ";\n");
                methodDefs.add(methodDef);
            }
            r.append("}\n");
            r.append("\n");
            generatedClassNames.add(getNameAsClassName());

            //

            if (explored.contains(this)) {
                return "";
            }
            explored.add(this);

            //

            if (nexts.size() >= 2) {
                r.append("public interface " + getNextsAsClassName() + " extends " + getNextsInterfaceNames() + "{}\n");
                generatedClassNames.add(getNextsAsClassName());
            }

            for (Prod next : nexts) {
                r.append(next.toString(explored, generatedClassNames, methodDefs));
            }
            return r.toString();
        }
    }

    public interface Select {
        public From select(Column<?, ?>... columns);

        public From selectAll();

        public From selectStar();
    }

    public interface From {
        public Join_Where_GroupBy_OrderBy_Execute from(Table<?> table);

        public Join_Where_GroupBy_OrderBy_Execute from(Select selectQuery);
    }

    public interface Join_Where_GroupBy_OrderBy_Execute extends Join, Where, GroupBy, OrderBy, Execute {
    }

    public interface Join {
        public On join(Table<?> table);

        public On join(Select selectQuery);
    }

    public interface On {
        public Where_GroupBy_OrderBy_Execute on(IQueryObject... restrictions);
    }

    public interface Where_GroupBy_OrderBy_Execute extends Where, GroupBy, OrderBy, Execute {
    }

    public interface Where {
        public GroupBy_OrderBy_Execute where(IQueryObject... restrictions);
    }

    public interface GroupBy_OrderBy_Execute extends GroupBy, OrderBy, Execute {
    }

    public interface GroupBy {
        public Having_OrderBy_Execute groupBy(Column<?, ?>... columns);
    }

    public interface Having_OrderBy_Execute extends Having, OrderBy, Execute {
    }

    public interface Having {
        public OrderBy_Execute having(IQueryObject... restrictions);
    }

    public interface OrderBy_Execute extends OrderBy, Execute {
    }

    public interface OrderBy {
        public Execute orderBy(List<Order<?>> orders);
    }

    public interface Execute {
        public Object execute();
    }

    public class Impl
            implements OrderBy, GroupBy, Execute, Where_GroupBy_OrderBy_Execute, OrderBy_Execute, Join, From, Having,
            Having_OrderBy_Execute, Select, Join_Where_GroupBy_OrderBy_Execute, GroupBy_OrderBy_Execute, Where, On {

        @Override
        public Object execute() {
            return this;
        }

        @Override
        public Execute orderBy(List<Order<?>> orders) {
            return this;
        }

        @Override
        public Join_Where_GroupBy_OrderBy_Execute from(Table<?> table) {
            return this;
        }

        @Override
        public On join(Select selectQuery) {
            return this;
        }

        @Override
        public From select(Column<?, ?>... columns) {
            return this;
        }

        @Override
        public From selectStar() {
            return this;
        }

        @Override
        public On join(Table<?> table) {
            return this;
        }

        @Override
        public OrderBy_Execute having(IQueryObject... restrictions) {
            return this;
        }

        @Override
        public From selectAll() {
            return this;
        }

        @Override
        public Having_OrderBy_Execute groupBy(Column<?, ?>... columns) {
            return this;
        }

        @Override
        public GroupBy_OrderBy_Execute where(IQueryObject... restrictions) {
            return this;
        }

        @Override
        public Where_GroupBy_OrderBy_Execute on(IQueryObject... restrictions) {
            return this;
        }

        @Override
        public Join_Where_GroupBy_OrderBy_Execute from(Select selectQuery) {
            return this;
        }

    }

}
