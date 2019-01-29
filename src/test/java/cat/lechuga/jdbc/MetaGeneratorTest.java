package cat.lechuga.jdbc;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

import cat.lechuga.EntityManagerFactory;
import cat.lechuga.EntityMeta;
import cat.lechuga.jdbc.test.VotrTest.Comment;
import cat.lechuga.jdbc.test.VotrTest.Option;
import cat.lechuga.jdbc.test.VotrTest.User;
import cat.lechuga.jdbc.test.VotrTest.Votr;
import cat.lechuga.tsmql.ELike;
import cat.lechuga.tsmql.MetaColumn;
import cat.lechuga.tsmql.MetaGenerator;
import cat.lechuga.tsmql.MetaTable;
import cat.lechuga.tsmql.Restrictions;
import cat.lechuga.tsmql.TypeSafeQueryBuilder;

public class MetaGeneratorTest {

    @Test
    public void testName() throws Exception {

        Class<?>[] cs = { Votr.class, User.class, Option.class, Comment.class };

        EntityManagerFactory emf = new EntityManagerFactory();

        int uniqueIndex = 0;
        for (Class<?> c : cs) {
            EntityMeta<?> meta = emf.buildEntityMeta(c);
            System.out.println(MetaGenerator.generateMetaColumns(meta, uniqueIndex++));
        }

        Votr_ votr_ = new Votr_("v");
        User_ user_ = new User_();
        Option_ option_ = new Option_();
        // Comment_ comment_ = new Comment_();

        {
            TypeSafeQueryBuilder q = new TypeSafeQueryBuilder();
            q.addAlias(votr_);
            q.append("select max({}) ", votr_.id);
            q.append("from {} ", votr_);
            q.append("where {} ", votr_.id.eq(103));
            assertEquals("select max({v.id}) from {v.#} where {v.id=?}  -- [103(Integer)]",
                    q.getMqlQueryObject().toString());
            assertEquals("select max(v.votr_id) from votrs v where v.votr_id=?  -- [103(Integer)]", q.toString());
        }

        {
            TypeSafeQueryBuilder q = new TypeSafeQueryBuilder();
            q.addAlias(option_);
            q.addAlias(user_);
            q.append("select {},count(*) ", option_.star());
            q.append("from {} join {} ", option_, user_);
            q.append("on {} and {} ", option_.votrId.eq(user_.votrId), option_.order.eq(user_.votedOptionOrder));
            q.append("group by {} ", option_.all());
            assertEquals( //
                    "select {option2.*},count(*) " + //
                            "from {option2.#} join {user.#} " + //
                            "on {option2.id.votrId}={user.votrId} and {option2.id.order}={user.votedOptionOrder} " + //
                            "group by {option2.id.votrId}, {option2.id.order}, {option2.title}, {option2.desc}  -- []", //
                    q.getMqlQueryObject().toString());
            assertEquals( //
                    "select option2.votr_id,option2.norder,option2.title,option2.descr,count(*) " + //
                            "from options option2 join users user " + //
                            "on option2.votr_id=user.votr_id and option2.norder=user.option_norder " + //
                            "group by option2.votr_id, option2.norder, option2.title, option2.descr  -- []", //
                    q.toString());
        }

        {
            TypeSafeQueryBuilder q = new TypeSafeQueryBuilder();
            q.addAlias(votr_);
            q.append("select max({}) ", votr_.id);
            q.append("from {} ", votr_);
            q.append("where {} ", Restrictions.and( //
                    votr_.title.ilike(ELike.CONTAINS, "o"), //
                    votr_.id.isNotNull(), //
                    votr_.id.ge(5), //
                    votr_.id.le(50), //
                    votr_.id.between(5, 50) //
            ));

            assertEquals(
                    "select max({v.id}) from {v.#} where upper({v.title) like upper(?)} and {v.id} is not null and {v.id>=?} and {v.id<=?} and {v.id between ? and ?}  -- [%o%(String), 5(Integer), 50(Integer), 5(Integer), 50(Integer)]",
                    q.getMqlQueryObject().toString());
            assertEquals(
                    "select max(v.votr_id) from votrs v where upper(v.title) like upper(?) and v.votr_id is not null and v.votr_id>=? and v.votr_id<=? and v.votr_id between ? and ?  -- [%o%(String), 5(Integer), 50(Integer), 5(Integer), 50(Integer)]",
                    q.toString());
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
            super(Option.class, "option2");
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
            super(Comment.class, "commen3");
        }

        public Comment_(String alias) {
            super(Comment.class, alias);
        }
    }

}
