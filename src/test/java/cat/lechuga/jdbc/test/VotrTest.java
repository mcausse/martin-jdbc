package cat.lechuga.jdbc.test;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;
import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.JdbcDataAccesFacade;
import org.lenteja.jdbc.script.SqlScriptExecutor;
import org.lenteja.jdbc.txproxy.TransactionalMethod;
import org.lenteja.jdbc.txproxy.TransactionalServiceProxyfier;

import cat.lechuga.EntityListener;
import cat.lechuga.EntityManager;
import cat.lechuga.EntityManagerFactory;
import cat.lechuga.GenericDao;
import cat.lechuga.anno.Column;
import cat.lechuga.anno.EntityListeners;
import cat.lechuga.anno.Generated;
import cat.lechuga.anno.Id;
import cat.lechuga.anno.Table;
import cat.lechuga.anno.Transient;
import cat.lechuga.generator.impl.HsqldbSequence;
import cat.lechuga.jdbc.MetaGeneratorTest.Comment_;
import cat.lechuga.jdbc.MetaGeneratorTest.Option_;
import cat.lechuga.jdbc.MetaGeneratorTest.User_;
import cat.lechuga.jdbc.MetaGeneratorTest.Votr_;
import cat.lechuga.mql.Orders;
import cat.lechuga.mql.Orders.Order;
import cat.lechuga.mql.QueryBuilder;
import cat.lechuga.reflect.anno.Embeddable;
import cat.lechuga.tsmql.Restrictions;
import cat.lechuga.tsmql.TOrders;
import cat.lechuga.tsmql.TOrders.TOrder;
import cat.lechuga.tsmql.TypeSafeQueryBuilder;

public class VotrTest {

    final DataAccesFacade facade;

    public VotrTest() {
        final JDBCDataSource ds = new JDBCDataSource();
        ds.setUrl("jdbc:hsqldb:mem:a");
        ds.setUser("sa");
        ds.setPassword("");
        this.facade = new JdbcDataAccesFacade(ds);
    }

    @Before
    public void before() {
        facade.begin();
        try {
            SqlScriptExecutor sql = new SqlScriptExecutor(facade);
            sql.runFromClasspath("sql/votr.sql");
            facade.commit();
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }
    }

    // TODO utilitzar type-safe queries
    @Test
    public void testName() throws Exception {

        VotrService service = TransactionalServiceProxyfier.proxyfy(facade, new VotrServiceImpl(facade),
                VotrService.class);

        Votr v = new Votr();
        v.setTitle("best poem");
        v.setDesc("Votr for best poem");

        User creator = new User();
        creator.setEmail("mhc@votr.com");

        service.createVotr(v, creator);

        Option o1 = new Option();
        o1.setTitle("aeneid");
        o1.setDesc("the aeneid");

        Option o2 = new Option();
        o2.setTitle("odissey");
        o2.setDesc("the odissey");

        service.createOptions(v.getVotrHash(), Arrays.asList(o1, o2), creator.getUserHash());
        service.createOptions(v.getVotrHash(), Arrays.asList(o1, o2), creator.getUserHash());

        User user2 = new User();
        user2.setEmail("mem@votr.com");

        service.createUsers(v.getVotrHash(), Arrays.asList(creator, user2));

        service.createOptions(v.getVotrHash(), Arrays.asList(o1, o2), creator.getUserHash());

        service.updateUser(v.getVotrHash(), creator.getUserHash(), null, "mhc");
        service.updateUser(v.getVotrHash(), creator.getUserHash(), o1.getId().getOrder(), null);

        service.updateUser(v.getVotrHash(), creator.getUserHash(), o1.getId().getOrder(), "mhc");
        service.updateUser(v.getVotrHash(), user2.getUserHash(), o1.getId().getOrder(), "mem");

        System.out.println(service.getVotrInfo(v.getVotrHash(), user2.getUserHash()));

    }

    protected String generaHash(String input) {
        return Integer.toHexString(input.hashCode());
    }

    public static interface VotrService {

        /**
         * Crea votació base amb usuari creador. Inserta missatge de creació,
         */
        void createVotr(Votr v, User creator);

        /**
         * els usuaris desvoten i les opcions es re-creen. Inserta missatge d'opcions
         * reecreades i que tothom desvota.
         */
        void createOptions(String hashVotr, List<Option> options, String hashUserModifier);

        /**
         * esborra comments i re-crea usuaris. Inserta missatge d'usuaris convidats.
         */
        void createUsers(String hashVotr, List<User> us);

        /**
         * Actualitza usuari amb orderOpcioVotada i/o alias, segons s'informi. Inserta
         * missatge(s) dels canvis.
         */
        void updateUser(String hashVotr, String hashUser, Long orderOpcioVotada, String alias);

        VotrInfo getVotrInfo(String hashVotr, String hashUser);

        /**
         * Un usuari inserta un comentari
         */
        void createComment(String hashVotr, String hashUser, String message);
    }

    // TODO utilitzar type-safe queries

    public static class VotrServiceImpl implements VotrService {

        final DataAccesFacade facade;

        final Votr_ v = new Votr_();
        final User_ u = new User_();
        final Option_ o = new Option_();
        final Comment_ c = new Comment_();

        final GenericDao<Votr, Integer> votrDao;
        final GenericDao<User, Long> userDao;
        final GenericDao<Option, OptionId> optionDao;
        final GenericDao<Comment, Long> commentDao;

        public VotrServiceImpl(DataAccesFacade facade) {
            super();
            this.facade = facade;

            EntityManagerFactory emf = new EntityManagerFactory();
            EntityManager em = emf.buildEntityManager(facade, Votr.class, User.class, Option.class, Comment.class);

            this.votrDao = new GenericDao<>(em, Votr.class);
            this.userDao = new GenericDao<>(em, User.class);
            this.optionDao = new GenericDao<>(em, Option.class);
            this.commentDao = new GenericDao<>(em, Comment.class);
        }

        protected String generaHash(String input) {
            return Integer.toHexString(input.hashCode());
        }

        @TransactionalMethod
        @Override
        public void createVotr(Votr v, User creator) {
            v.setId(null);
            v.setCreacio(new Date(0L));
            v.setVotrHash(generaHash(v.getTitle() + v.getDesc() + v.getCreacio().getTime()));
            votrDao.store(v);

            creator.setUserId(null);
            creator.setVotedDate(null);
            creator.setVotedOptionOrder(null);
            creator.setVotrId(v.getId());
            creator.setUserHash(generaHash(creator.getEmail()));
            userDao.store(creator);

            createComment("alo, votr created.", v.getId(), creator.getUserId());
        }

        @TransactionalMethod
        @Override
        public void createOptions(String hashVotr, List<Option> options, String hashUserModifier) {

            Votr votr;
            {
                Votr example = new Votr();
                example.setVotrHash(hashVotr);
                votr = votrDao.loadUniqueByExample(example);
            }

            User userModifier;
            {
                User example = new User();
                example.setUserHash(hashUserModifier);
                example.setVotrId(votr.getId());
                userModifier = userDao.loadUniqueByExample(example);
            }

            // 1) tots els usuaris desvoten
            List<User> us;
            {
                User example = new User();
                example.setVotrId(votr.getId());
                us = userDao.loadByExample(example, Orders.by(Order.asc("userId")));
            }
            for (User u : us) {
                u.setVotedOptionOrder(null);
                u.setVotedDate(null);
                userDao.store(u);
            }

            // 2) s'esborren totes les opcions antigues
            {
                Option example = new Option();
                example.setId(new OptionId());
                example.getId().setVotrId(votr.getId());
                List<Option> opts = optionDao.loadByExample(example);
                for (Option o : opts) {
                    optionDao.delete(o);
                }
            }

            // 3) es creen les noves opcions
            long order = 0;
            for (Option o : options) {
                o.setId(new OptionId());
                o.getId().setVotrId(votr.getId());
                o.getId().setOrder(order++);
                optionDao.store(o);
            }

            createComment("s'ha recreat les opcions, tothom desvota", votr.getId(), userModifier.getUserId());
        }

        @TransactionalMethod
        @Override
        public void createUsers(String hashVotr, List<User> us) {

            Votr votr;
            {
                Votr example = new Votr();
                example.setVotrHash(hashVotr);
                votr = votrDao.loadUniqueByExample(example);
            }
            {
                // List<Comment> comments = commentDao.loadBy(c.votrId.eq(votr.id));

                List<Comment> comments = commentDao.buildTypeSafeQuery() //
                        // .addAlias(c) //
                        .selectFrom(c) //
                        .where(c.votrId.eq(votr.id)) //
                        .getExecutor(Comment.class) //
                        .load();

                commentDao.deleteAll(comments);
            }
            // {
            // List<Option> options = optionDao.loadBy(o.votrId.eq(votr.id));
            // optionDao.deleteAll(options);
            // }
            {
                // List<User> currentUsers = userDao.loadBy(u.votrId.eq(votr.id));
                List<User> currentUsers = userDao.buildTypeSafeQuery() //
                        .selectFrom(u) //
                        .where(u.votrId.eq(votr.id)) //
                        .getExecutor(User.class) //
                        .load();

                userDao.deleteAll(currentUsers);
            }

            for (User u : us) {
                u.setUserId(null);
                u.setVotedDate(null);
                u.setVotedOptionOrder(null);
                u.setVotrId(votr.getId());
                u.setUserHash(generaHash(u.getEmail() + u.getAlias() + votr.getId()));
                userDao.insert(u);

                createComment("he sigut convidat", votr.getId(), u.getUserId());
            }

        }

        @TransactionalMethod
        @Override
        public void updateUser(String hashVotr, String hashUser, Long orderOpcioVotada, String alias) {

            Votr votr;
            {
                Votr example = new Votr();
                example.setVotrHash(hashVotr);
                votr = votrDao.loadUniqueByExample(example);
            }

            User u;
            {
                User example = new User();
                example.setUserHash(hashUser);
                example.setVotrId(votr.getId());
                u = userDao.loadUniqueByExample(example);
            }

            // valida opció
            if (orderOpcioVotada != null) {
                Option opcioVotada;
                {
                    OptionId optId = new OptionId();
                    optId.setVotrId(votr.getId());
                    optId.setOrder(orderOpcioVotada);
                    opcioVotada = optionDao.loadById(optId);
                }
                u.setVotedOptionOrder(orderOpcioVotada);
                u.setVotedDate(new Date(0L));

                if (u.getVotedOptionOrder() == null && orderOpcioVotada != null ||
                /**/u.getVotedOptionOrder() != null && orderOpcioVotada != null
                        && !u.getVotedOptionOrder().equals(orderOpcioVotada)) {
                    createComment("he votat: " + opcioVotada.getTitle(), votr.getId(), u.getUserId());
                }
            }

            if (alias != null) {
                if (u.getAlias() == null && alias != null
                        || u.getAlias() != null && alias != null && !u.getAlias().equals(alias)) {
                    createComment(u.getEmail() + " => " + alias, votr.getId(), u.getUserId());
                }
                u.setAlias(alias);
            }

            userDao.store(u);
        }

        @TransactionalMethod
        @Override
        public void createComment(String hashVotr, String hashUser, String message) {

            Votr votr;
            {
                Votr example = new Votr();
                example.setVotrHash(hashVotr);
                votr = votrDao.loadUniqueByExample(example);
            }

            User u;
            {
                User example = new User();
                example.setUserHash(hashUser);
                example.setVotrId(votr.getId());
                u = userDao.loadUniqueByExample(example);
            }

            createComment(message, votr.getId(), u.getUserId());
        }

        protected void createComment(String message, Integer votrId, Long userId) {
            Comment c = new Comment();
            c.setCommentId(null);
            c.setComment(message);
            c.setCommentDate(new Date(0L));
            c.setUserId(userId);
            c.setVotrId(votrId);
            commentDao.store(c);
        }

        @TransactionalMethod(readOnly = true)
        @Override
        public VotrInfo getVotrInfo(String hashVotr, String hashUser) {

            Votr votr;
            {
                Votr example = new Votr();
                example.setVotrHash(hashVotr);
                votr = votrDao.loadUniqueByExample(example);
            }

            User you;
            {
                // User example = new User();
                // example.setUserHash(hashUser);
                // example.setVotrId(votr.getId());
                // you = userDao.loadUniqueByExample(example);

                you = userDao.loadUniqueBy(u, //
                        Restrictions.and( //
                                u.userHash.eq(hashUser), //
                                u.votrId.eq(votr.getId()) //
                        ) //
                );
            }

            List<User> allUsers;
            {
                // User example = new User();
                // example.setVotrId(votr.getId());
                // allUsers = userDao.loadByExample(example);

                allUsers = userDao.loadBy(u, //
                        u.votrId.eq(votr.getId()), //
                        TOrders.by(TOrder.asc(u.userId)));
            }

            Map<Option, List<User>> optionsVots = new LinkedHashMap<>();
            {
                List<Option> opcions;
                {
                    // OptionId optId = new OptionId();
                    // Option opt = new Option();
                    // opt.setId(optId);
                    // optId.setVotrId(votr.getId());
                    // opcions = optionDao.loadByExample(opt);

                    TypeSafeQueryBuilder q = optionDao.buildTypeSafeQuery();
                    q.addAlias(o);
                    q.append("select {} from {} ", o.all(), o);
                    q.append("where {} ", o.votrId.eq(votr.getId()));
                    q.append("order by {}", TOrders.by(TOrder.asc(o.order)));
                    opcions = q.getExecutor(Option.class).load();
                }
                for (Option o : opcions) {
                    // User example = new User();
                    // example.setVotrId(votr.getId());
                    // example.setVotedOptionOrder(o.getId().getOrder());
                    // optionsVots.put(o, userDao.loadByExample(example));

                    QueryBuilder q = userDao.buildQuery();
                    q.addAlias("u", User.class);
                    q.append("select {u.*} from {u.#} where {u.votrId=?} and {u.votedOptionOrder=?}", votr.getId(),
                            o.getId().getOrder());
                    optionsVots.put(o, q.getExecutor(User.class).load());
                }
            }

            List<Comment> comments;
            {
                // Comment example = new Comment();
                // example.setVotrId(votr.getId());
                // comments = commentDao.loadByExample(example,
                // Orders.by(Order.asc("commentDate"), Order.asc("commentId")));

                comments = commentDao.loadBy(c, c.votrId.eq(votr.getId()),
                        TOrders.by(TOrder.asc(c.commentDate), TOrder.asc(c.commentId)));
            }

            return new VotrInfo(votr, you, allUsers, optionsVots, comments);
        }

    }

    public static class VotrInfo {

        public final Votr votr;
        public final User you;
        public final List<User> allUsers;
        public final Map<Option, List<User>> optionsVots;
        public final List<Comment> comments;

        public VotrInfo(Votr votr, User you, List<User> allUsers, Map<Option, List<User>> optionsVots,
                List<Comment> comments) {
            super();
            this.votr = votr;
            this.you = you;
            this.allUsers = allUsers;
            this.optionsVots = optionsVots;
            this.comments = comments;
        }

        @Override
        public String toString() {
            return "VotrInfo [votr=" + votr + ", you=" + you + ", allUsers=" + allUsers + ", optionsVots=" + optionsVots
                    + ", comments=" + comments + "]";
        }

    }

    @Table("votrs")
    public static class Votr {

        @Id
        @Column("votr_id")
        @Generated(value = HsqldbSequence.class, args = { "seq_votrs" })
        Integer id;

        String votrHash;

        String title;

        @Column("descr")
        String desc;

        @Column("creat_date")
        Date creacio;

        public Votr() {
            super();
        }

        public Votr(String votrHash, String title, String desc, Date creacio) {
            super();
            this.votrHash = votrHash;
            this.title = title;
            this.desc = desc;
            this.creacio = creacio;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getVotrHash() {
            return votrHash;
        }

        public void setVotrHash(String votrHash) {
            this.votrHash = votrHash;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public Date getCreacio() {
            return creacio;
        }

        public void setCreacio(Date creacio) {
            this.creacio = creacio;
        }

        @Override
        public String toString() {
            return "Votr [id=" + id + ", votrHash=" + votrHash + ", title=" + title + ", desc=" + desc + ", creacio="
                    + creacio + "]";
        }

    }

    public static class UserEntityListener extends EntityListener<User> {

        @Override
        public void afterLoad(User user) {
            if (user.getAlias() == null) {
                user.setCurrentName(user.getEmail());
            } else {
                user.setCurrentName(user.getAlias());
            }
        }
    }

    @EntityListeners(UserEntityListener.class)
    @Table("users")
    public static class User {

        @Id
        @Generated(value = HsqldbSequence.class, args = { "seq_users" })
        Long userId;

        String userHash;

        String email;

        String alias;

        Integer votrId;

        @Transient
        String currentName;

        @Column("option_norder")
        Long votedOptionOrder;

        @Column("option_date")
        Date votedDate;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getUserHash() {
            return userHash;
        }

        public void setUserHash(String userHash) {
            this.userHash = userHash;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        public Integer getVotrId() {
            return votrId;
        }

        public void setVotrId(Integer votrId) {
            this.votrId = votrId;
        }

        public Long getVotedOptionOrder() {
            return votedOptionOrder;
        }

        public void setVotedOptionOrder(Long votedOptionOrder) {
            this.votedOptionOrder = votedOptionOrder;
        }

        public Date getVotedDate() {
            return votedDate;
        }

        public void setVotedDate(Date votedDate) {
            this.votedDate = votedDate;
        }

        public String getCurrentName() {
            return currentName;
        }

        public void setCurrentName(String currentName) {
            this.currentName = currentName;
        }

        @Override
        public String toString() {
            return "User [userId=" + userId + ", userHash=" + userHash + ", votrId=" + votrId + ", currentName="
                    + currentName + ", votedOptionOrder=" + votedOptionOrder + ", votedDate=" + votedDate + "]";
        }

    }

    @Embeddable
    public static class OptionId {

        Integer votrId;

        @Column("norder")
        Long order;

        public OptionId() {
            super();
        }

        public OptionId(Integer votrId, Long order) {
            super();
            this.votrId = votrId;
            this.order = order;
        }

        public Integer getVotrId() {
            return votrId;
        }

        public void setVotrId(Integer votrId) {
            this.votrId = votrId;
        }

        public Long getOrder() {
            return order;
        }

        public void setOrder(Long order) {
            this.order = order;
        }

        @Override
        public String toString() {
            return "Option [votrId=" + votrId + ", order=" + order + "]";
        }
    }

    @Table("options")
    public static class Option {

        @Id
        OptionId id;

        String title;

        @Column("descr")
        String desc;

        public OptionId getId() {
            return id;
        }

        public void setId(OptionId id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        @Override
        public String toString() {
            return "Option [id=" + id + ", title=" + title + ", desc=" + desc + "]";
        }
    }

    @Table("comments")
    public static class Comment {

        @Id
        @Generated(value = HsqldbSequence.class, args = { "seq_comments" })
        Long commentId;

        Date commentDate;
        String comment;

        Integer votrId;
        Long userId;

        public Long getCommentId() {
            return commentId;
        }

        public void setCommentId(Long commentId) {
            this.commentId = commentId;
        }

        public Date getCommentDate() {
            return commentDate;
        }

        public void setCommentDate(Date commentDate) {
            this.commentDate = commentDate;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public Integer getVotrId() {
            return votrId;
        }

        public void setVotrId(Integer votrId) {
            this.votrId = votrId;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        @Override
        public String toString() {
            return "Comment [commentId=" + commentId + ", commentDate=" + commentDate + ", comment=" + comment
                    + ", votrId=" + votrId + ", userId=" + userId + "]";
        }
    }

}