package org.lenteja.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import org.junit.Test;
import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;
import org.lenteja.mapper.Column;
import org.lenteja.mapper.Mapable;
import org.lenteja.mapper.Table;
import org.lenteja.mapper.query.Executor;
import org.lenteja.mapper.query.Order;
import org.lenteja.mapper.query.Restrictions;

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

    @Test
    public void testName() throws Exception {

        Prod select = new Prod("select", "select(Column<?,?>... columns)", "select(Table<?>...tables)", "selectStar()");
        Prod from = new Prod("from", "from(Table<?> table)", "from(Select selectQuery)");
        Prod join = new Prod("join", "join(Table<?> table)", "join(Select selectQuery, String alias)");
        Prod on = new Prod("on", "on(IQueryObject... restrictions)");
        Prod where = new Prod("where", "where(IQueryObject... restrictions)");
        Prod groupBy = new Prod("groupBy", "groupBy(Column<?,?>... columns)");
        Prod having = new Prod("having", "having(IQueryObject... restrictions)");
        Prod orderBy = new Prod("orderBy", "orderBy(Order<?>... orders)");
        Prod END = new Prod("execute", "<E>Executor<E> getExecutor(DataAccesFacade facade, Mapable<E> mapable)");

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
        select.setPrefix("S");
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

        generateDsl("Select", roots);

        //

        // new DslGen().new Impl().selectAll().from(new DogTable()).execute();

        DogTable dt = new DogTable("dogs_");
        PersonTable pt = new PersonTable("persons_");

        IQueryObject q = new DslGen().new Select() //
                .select(dt, pt) //
                .from(dt).join(pt).on(dt.idJefe.eq(pt.idPerson)) //
                .where(dt.alive.eq(true)) //
                .orderBy(Order.asc(dt.idDog)) //
                .getExecutor(null, dt) //
                .getQuery() //
        ;

        // TODO és factible/val la pena?

        assertEquals("select dogs_.id_dog,dogs_.name,dogs_.is_alive,dogs_.sex,dogs_.id_jefe,"
                + "persons_.id_person,persons_.dni,persons_.name,persons_.age,persons_.birth_date "
                + "from dogs dogs_ join persons persons_ on dogs_.id_jefe=persons_.id_person "
                + "where dogs_.is_alive=? order by dogs_.id_dog asc -- [true(Boolean)]", q.toString());
    }

    private static void generateDsl(String rootName, List<Prod> roots) {

        Set<Prod> explored = new HashSet<>();
        Set<String> generatedClassNames = new HashSet<>();
        Set<String> methodDefs = new HashSet<>();
        for (Prod root : roots) {
            System.out.println(root.toString(explored, generatedClassNames, methodDefs));
        }

        StringBuilder r = new StringBuilder();
        r.append("public class " + rootName + " implements ");
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

    public interface SSelect {
        public From select(Column<?, ?>... columns);

        public From select(Table<?>... tables);

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

        public On join(Select selectQuery, String alias);
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
        public Execute orderBy(Order<?>... orders);
    }

    public interface Execute {
        public <E> Executor<E> getExecutor(DataAccesFacade facade, Mapable<E> mapable);
    }

    public class Select
            implements OrderBy, GroupBy, Execute, Where_GroupBy_OrderBy_Execute, OrderBy_Execute, SSelect, Join, From,
            Having, Having_OrderBy_Execute, Join_Where_GroupBy_OrderBy_Execute, GroupBy_OrderBy_Execute, Where, On {

        final QueryObject qo = new QueryObject();

        public QueryObject getQuery() {
            return qo;
        }

        @Override
        public <E> Executor<E> getExecutor(DataAccesFacade facade, Mapable<E> mapable) {
            return new Executor<E>(facade, qo, mapable);
        }

        @Override
        public Execute orderBy(Order<?>... orders) {
            qo.append(" order by ");
            qo.append(Restrictions.list(orders));
            return this;
        }

        @Override
        public Join_Where_GroupBy_OrderBy_Execute from(Table<?> table) {
            qo.append(" from ");
            qo.append(table.getAliasedName());
            return this;
        }

        @Override
        public On join(Select selectQuery, String alias) {
            qo.append(" join ");
            qo.append(selectQuery.qo);
            qo.append(alias);
            return this;
        }

        @Override
        public Join_Where_GroupBy_OrderBy_Execute from(Select selectQuery) {
            return this;
        }

        @Override
        public From select(Column<?, ?>... columns) {
            qo.append("select ");
            StringJoiner j = new StringJoiner(",");
            for (Column<?, ?> c : columns) {
                j.add(c.getAliasedName());
            }
            qo.append(j.toString());
            return this;
        }

        @Override
        public From selectStar() {
            qo.append("select *");
            return this;
        }

        @Override
        public On join(Table<?> table) {
            qo.append(" join ");
            qo.append(table.getAliasedName());
            return this;
        }

        @Override
        public OrderBy_Execute having(IQueryObject... restrictions) {
            qo.append(" having ");
            StringJoiner j = new StringJoiner(" and ");
            for (IQueryObject r : restrictions) {
                j.add(r.getQuery());
                qo.addArgs(r.getArgsList());
            }
            qo.append(j.toString());
            return this;
        }

        @Override
        public From select(Table<?>... tables) {
            qo.append("select ");
            StringJoiner j = new StringJoiner(",");
            for (Table<?> t : tables) {
                for (Column<?, ?> c : t.getColumns()) {
                    j.add(c.getAliasedName());
                }
            }
            qo.append(j.toString());
            return this;
        }

        @Override
        public Having_OrderBy_Execute groupBy(Column<?, ?>... columns) {
            qo.append(" group by ");
            StringJoiner j = new StringJoiner(",");
            for (Column<?, ?> c : columns) {
                j.add(c.getAliasedName());
            }
            qo.append(j.toString());
            return this;
        }

        @Override
        public GroupBy_OrderBy_Execute where(IQueryObject... restrictions) {
            qo.append(" where ");
            StringJoiner j = new StringJoiner(" and ");
            for (IQueryObject r : restrictions) {
                j.add(r.getQuery());
                qo.addArgs(r.getArgsList());
            }
            qo.append(j.toString());
            return this;
        }

        @Override
        public Where_GroupBy_OrderBy_Execute on(IQueryObject... restrictions) {
            qo.append(" on ");
            StringJoiner j = new StringJoiner(" and ");
            for (IQueryObject r : restrictions) {
                j.add(r.getQuery());
                qo.addArgs(r.getArgsList());
            }
            qo.append(j.toString());
            return this;
        }

    }

}
