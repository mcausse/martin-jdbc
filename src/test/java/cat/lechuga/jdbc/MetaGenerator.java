package cat.lechuga.jdbc;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;

import org.junit.Test;
import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;

import cat.lechuga.EntityManagerFactory;
import cat.lechuga.EntityMeta;
import cat.lechuga.Mapable;
import cat.lechuga.PropertyMeta;
import cat.lechuga.jdbc.test.VotrTest.Comment;
import cat.lechuga.jdbc.test.VotrTest.Option;
import cat.lechuga.jdbc.test.VotrTest.User;
import cat.lechuga.jdbc.test.VotrTest.Votr;
import cat.lechuga.mql.Executor;
import cat.lechuga.mql.QueryBuilder;

public class MetaGenerator {

    @Test
    public void testName() throws Exception {

        Class<?>[] cs = { Votr.class, User.class, Option.class, Comment.class };

        EntityManagerFactory emf = new EntityManagerFactory(null);

        for (Class<?> c : cs) {
            EntityMeta<?> meta = emf.buildEntityMeta(c);
            System.out.println(generateMetaColumns(meta));
        }

        Votr_ votr_ = new Votr_();
        User_ user_ = new User_();
        Option_ option_ = new Option_();
        Comment_ comment_ = new Comment_();

        {
            TypeSafeQueryBuilder q = new TypeSafeQueryBuilder(null);
            q.addAlias(votr_);
            q.append("select max({}) ", votr_.id);
            q.append("from {} ", votr_);
            q.append("where {} ", votr_.id.eq(103));
            assertEquals("select max(votr.votr_id) from votrs votr where votr.votr_id=?  -- [103(Integer)]",
                    q.toString());
        }

        {
            TypeSafeQueryBuilder q = new TypeSafeQueryBuilder(null);
            q.addAlias(option_);
            q.addAlias(user_);
            q.append("select {},count(*) ", option_.star());
            q.append("from {} join {} ", option_, user_);
            q.append("on {} and {} ", option_.votrId.eq(user_.votrId), option_.order.eq(user_.votedOptionOrder));
            q.append("group by {} ", option_.all());
            assertEquals( //
                    "select option.votr_id,option.norder,option.title,option.descr,count(*) " + //
                            "from options option join users user " + //
                            "on option.votr_id=user.votr_id and option.norder=user.option_norder " + //
                            "group by option.votr_id, option.norder, option.title, option.descr  -- []", //
                    q.toString());
        }
    }

    public static class TypeSafeQueryBuilder {

        private final EntityManagerFactory emf;
        private final QueryBuilder qb;

        public TypeSafeQueryBuilder(DataAccesFacade facade) {
            super();
            this.emf = new EntityManagerFactory(facade);
            this.qb = new QueryBuilder(facade);
        }

        public TypeSafeQueryBuilder addAlias(MetaTable<?> table) {
            EntityMeta<?> em = emf.buildEntityMeta(table.getEntityClass());
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
                qb.append(queryFragment.substring(p, p2));

                Object arg = args[argIndex++];
                if (arg instanceof Criterion) {
                    Criterion arg2 = (Criterion) arg;
                    qb.append(arg2.getQuery(), arg2.getArgs());
                } else if (arg instanceof MetaTable) {
                    MetaTable<?> arg2 = (MetaTable<?>) arg;
                    qb.append("{" + arg2.getAlias() + ".#}");
                } else if (arg instanceof MetaColumn) {
                    MetaColumn<?, ?> arg2 = (MetaColumn<?, ?>) arg;
                    qb.append("{" + arg2.getAlias() + "." + arg2.getPropertyName() + "}");
                } else {
                    throw new RuntimeException(String.valueOf(arg));
                }

                p = p2 + "{}".length();
            }

            qb.append(queryFragment.substring(p));

            return this;
        }

        public IQueryObject getQueryObject() {
            return qb.getQueryObject();
        }

        public <T> Executor<T> getExecutor(Mapable<T> mapable) {
            return qb.getExecutor(mapable);
        }

        @Override
        public String toString() {
            return qb.toString();
        }

    }

    public static class Criterion extends QueryObject {
    }

    protected String generateMetaColumns(EntityMeta<?> meta) {

        StringBuilder s = new StringBuilder();

        s.append("public static class " + meta.getEntityClass().getSimpleName() + "_ extends MetaTable<");
        s.append(meta.getEntityClass().getSimpleName());
        s.append("> {\n\n");
        for (PropertyMeta p : meta.getAllProps()) {
            s.append("\tpublic final MetaColumn<");
            s.append(meta.getEntityClass().getSimpleName());
            s.append(", ");
            s.append(p.getProp().getType().getSimpleName());
            s.append(">");
            s.append(p.getProp().getLastName() + " = ");
            s.append("addColumn(\"" + p.getProp().getFullName() + "\");\n");
        }

        s.append("\n");
        s.append("\tpublic " + meta.getEntityClass().getSimpleName() + "_() {\n");
        s.append("\t\tsuper(" + meta.getEntityClass().getSimpleName() + ".class, ");
        s.append("\"" + meta.getEntityClass().getSimpleName().toLowerCase() + "\");\n");

        s.append("\t}\n");

        s.append("\n");
        s.append("\tpublic " + meta.getEntityClass().getSimpleName() + "_(String alias) {\n");
        s.append("\t\tsuper(" + meta.getEntityClass().getSimpleName() + ".class, alias);\n");
        s.append("\t}\n");

        s.append("}\n");
        return s.toString();
    }

    public static abstract class MetaTable<E> {

        final Class<E> entityClass;
        final String alias;
        final List<MetaColumn<E, ?>> cols = new ArrayList<>();

        public MetaTable(Class<E> entityClass, String alias) {
            super();
            this.entityClass = entityClass;
            this.alias = alias;
        }

        public <T> MetaColumn<E, T> addColumn(String propertyName) {
            MetaColumn<E, T> c = new MetaColumn<>(this, propertyName);
            this.cols.add(c);
            return c;
        }

        public Class<E> getEntityClass() {
            return entityClass;
        }

        public String getAlias() {
            return alias;
        }

        public List<MetaColumn<E, ?>> getCols() {
            return cols;
        }

        //////////////////////////////////////////////

        public Criterion star() {
            Criterion c = new Criterion();
            c.append("{");
            c.append(alias);
            c.append(".*");
            c.append("}");
            return c;
        }

        public Criterion all() {
            Criterion c = new Criterion();

            StringJoiner j = new StringJoiner(", ");
            for (MetaColumn<E, ?> p : cols) {
                j.add("{" + alias + "." + p.getPropertyName() + "}");
            }
            c.append(j.toString());
            return c;
        }

        //////////////////////////////////////////////

    }

    public static class MetaColumn<E, T> {

        final MetaTable<E> table;
        final String propertyName;

        public MetaColumn(MetaTable<E> table, String propertyName) {
            super();
            this.propertyName = propertyName;
            this.table = table;
        }

        public String getAlias() {
            return table.getAlias();
        }

        public String getPropertyName() {
            return propertyName;
        }

        //////////////////////////////////////////////

        protected Criterion binaryOp(String op, T value) {
            Criterion q = new Criterion();
            q.append("{");
            q.append(table.getAlias());
            q.append(".");
            q.append(propertyName);
            q.append(op);
            q.append("?}");
            q.addArg(value);
            return q;
        }

        public Criterion eq(T value) {
            return binaryOp("=", value);
        }

        public Criterion ne(T value) {
            return binaryOp("<>", value);
        }

        public Criterion le(T value) {
            return binaryOp("<=", value);
        }

        public Criterion ge(T value) {
            return binaryOp(">=", value);
        }

        public Criterion lt(T value) {
            return binaryOp("<", value);
        }

        public IQueryObject gt(T value) {
            return binaryOp(">", value);
        }

        //////////////////////////////////////////////

        protected Criterion binaryOp(String op, MetaColumn<?, T> column) {
            Criterion q = new Criterion();
            q.append("{");
            q.append(table.getAlias());
            q.append(".");
            q.append(propertyName);
            q.append("}");
            q.append(op);
            q.append("{");
            q.append(column.table.getAlias());
            q.append(".");
            q.append(column.propertyName);
            q.append("}");
            return q;
        }

        public Criterion eq(MetaColumn<?, T> column) {
            return binaryOp("=", column);
        }

        public Criterion ne(MetaColumn<?, T> column) {
            return binaryOp("<>", column);
        }

        public Criterion le(MetaColumn<?, T> column) {
            return binaryOp("<=", column);
        }

        public Criterion ge(MetaColumn<?, T> column) {
            return binaryOp(">=", column);
        }

        public Criterion lt(MetaColumn<?, T> column) {
            return binaryOp("<", column);
        }

        public Criterion gt(MetaColumn<?, T> column) {
            return binaryOp(">", column);
        }

    }

    public static class Votr_ extends MetaTable<Votr> {

        public final MetaColumn<Votr, Integer> id = addColumn("id");
        public final MetaColumn<Votr, String> votrHash = addColumn("votrHash");
        public final MetaColumn<Votr, String> title = addColumn("title");
        public final MetaColumn<Votr, String> desc = addColumn("desc");
        public final MetaColumn<Votr, Date> creacio = addColumn("creacio");

        public Votr_() {
            super(Votr.class, "votr");
        }

        public Votr_(String alias) {
            super(Votr.class, alias);
        }
    }

    public static class User_ extends MetaTable<User> {

        public final MetaColumn<User, Long> userId = addColumn("userId");
        public final MetaColumn<User, String> userHash = addColumn("userHash");
        public final MetaColumn<User, String> email = addColumn("email");
        public final MetaColumn<User, String> alias = addColumn("alias");
        public final MetaColumn<User, Integer> votrId = addColumn("votrId");
        public final MetaColumn<User, Long> votedOptionOrder = addColumn("votedOptionOrder");
        public final MetaColumn<User, Date> votedDate = addColumn("votedDate");

        public User_() {
            super(User.class, "user");
        }

        public User_(String alias) {
            super(User.class, alias);
        }
    }

    public static class Option_ extends MetaTable<Option> {

        public final MetaColumn<Option, Integer> votrId = addColumn("id.votrId");
        public final MetaColumn<Option, Long> order = addColumn("id.order");
        public final MetaColumn<Option, String> title = addColumn("title");
        public final MetaColumn<Option, String> desc = addColumn("desc");

        public Option_() {
            super(Option.class, "option");
        }

        public Option_(String alias) {
            super(Option.class, alias);
        }
    }

    public static class Comment_ extends MetaTable<Comment> {

        public final MetaColumn<Comment, Long> commentId = addColumn("commentId");
        public final MetaColumn<Comment, Date> commentDate = addColumn("commentDate");
        public final MetaColumn<Comment, String> comment = addColumn("comment");
        public final MetaColumn<Comment, Integer> votrId = addColumn("votrId");
        public final MetaColumn<Comment, Long> userId = addColumn("userId");

        public Comment_() {
            super(Comment.class, "comment");
        }

        public Comment_(String alias) {
            super(Comment.class, alias);
        }
    }

}
