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

import cat.lechuga.BetterGenericDao;
import cat.lechuga.EntityManagerFactory;
import cat.lechuga.Order;
import cat.lechuga.anno.Column;
import cat.lechuga.anno.Generated;
import cat.lechuga.anno.Id;
import cat.lechuga.anno.Table;
import cat.lechuga.generator.impl.HsqldbSequence;
import cat.lechuga.reflect.anno.Embeddable;

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

        service.updateUser(v.getVotrHash(), creator.getUserHash(), o1.getId().getOrder(), "mhc");
        service.updateUser(v.getVotrHash(), user2.getUserHash(), o1.getId().getOrder(), "mem");

        System.out.println(service.getVotrInfo(v.getVotrHash(), user2.getUserHash()));

        // VotrInfo [votr=Votr [id=100, votrHash=4ceec84a, title=best poem, desc=Votr
        // for best poem, creacio=1970-01-01 01:00:00.0], you=User [userId=202,
        // userHash=fd702155, email=mem@votr.com, alias=mem, votrId=100,
        // votedOptionId=0, votedDate=1970-01-01 01:00:00.0], optionsVots={Option
        // [id=Option [votrId=100, order=0], title=aeneid, desc=the aeneid]=[User
        // [userId=201, userHash=24831ba8, email=mhc@votr.com, alias=mhc, votrId=100,
        // votedOptionId=0, votedDate=1970-01-01 01:00:00.0], User [userId=202,
        // userHash=fd702155, email=mem@votr.com, alias=mem, votrId=100,
        // votedOptionId=0, votedDate=1970-01-01 01:00:00.0]], Option [id=Option
        // [votrId=100, order=1], title=odissey, desc=the odissey]=[]},
        // comments=[Comment [commentId=400, commentDate=1970-01-01 01:00:00.0,
        // comment=alo, votr created., votrId=100, userId=200], Comment [commentId=401,
        // commentDate=1970-01-01 01:00:00.0, comment=s'ha recreat les opcions,
        // votrId=100, userId=200], Comment [commentId=402, commentDate=1970-01-01
        // 01:00:00.0, comment=s'ha recreat les opcions, votrId=100, userId=200],
        // Comment [commentId=403, commentDate=1970-01-01 01:00:00.0, comment=he sigut
        // convidat, votrId=100, userId=201], Comment [commentId=404,
        // commentDate=1970-01-01 01:00:00.0, comment=he sigut convidat, votrId=100,
        // userId=202], Comment [commentId=405, commentDate=1970-01-01 01:00:00.0,
        // comment=s'ha recreat les opcions, votrId=100, userId=201]]]
    }

    protected String generaHash(String input) {
        return Integer.toHexString(input.hashCode());
    }

    public static interface VotrService {

        void createVotr(Votr v, User creator);

        void createOptions(String hashVotr, List<Option> options, String hashUserModifier);

        void createUsers(String hashVotr, List<User> us);

        void updateUser(String hashVotr, String hashUser, Long orderOpcioVotada, String alias);

        VotrInfo getVotrInfo(String hashVotr, String hashUser);

        void createComment(String hashVotr, String hashUser, String message);
    }

    public static class VotrServiceImpl implements VotrService {

        final DataAccesFacade facade;
        final BetterGenericDao<Votr, Integer> votrDao;
        final BetterGenericDao<User, Long> userDao;
        final BetterGenericDao<Option, OptionId> optionDao;
        final BetterGenericDao<Comment, Long> commentDao;

        public VotrServiceImpl(DataAccesFacade facade) {
            super();
            this.facade = facade;
            EntityManagerFactory emf = new EntityManagerFactory();

            this.votrDao = new BetterGenericDao<>(emf.buildEntityManager(facade, Votr.class));
            this.userDao = new BetterGenericDao<>(emf.buildEntityManager(facade, User.class));
            this.optionDao = new BetterGenericDao<>(emf.buildEntityManager(facade, Option.class));
            this.commentDao = new BetterGenericDao<>(emf.buildEntityManager(facade, Comment.class));
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
                us = userDao.loadByExample(example, Order.by(Order.asc("userId")));
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

            createComment("s'ha recreat les opcions", votr.getId(), userModifier.getUserId());
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
            Option opcioVotada;
            {
                OptionId optId = new OptionId();
                optId.setVotrId(votr.getId());
                optId.setOrder(orderOpcioVotada);
                opcioVotada = optionDao.loadById(optId);
            }

            if (u.getVotedOptionOrder() == null && orderOpcioVotada != null ||
            /**/u.getVotedOptionOrder() != null && orderOpcioVotada != null
                    && !u.getVotedOptionOrder().equals(orderOpcioVotada)) {
                createComment("he votat: " + opcioVotada.getTitle(), votr.getId(), u.getUserId());
            }

            u.setVotedOptionOrder(orderOpcioVotada);
            u.setVotedDate(new Date(0L));
            u.setAlias(alias);
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
                User example = new User();
                example.setUserHash(hashUser);
                example.setVotrId(votr.getId());
                you = userDao.loadUniqueByExample(example);
            }

            List<User> allUsers;
            {
                User example = new User();
                example.setVotrId(votr.getId());
                allUsers = userDao.loadByExample(example);

                // TODO
                // // Votr_ votr_ = new Votr_("v");
                // User_ user_ = new User_();
                // // Option_ option_ = new Option_();
                //
                // allUsers = userDao.loadBy( //
                // user_.votrId.eq(votr.getId()), //
                // Order.by(Order.asc(user_.userId)));
            }

            Map<Option, List<User>> optionsVots = new LinkedHashMap<>();
            {
                List<Option> opcions;
                {
                    OptionId optId = new OptionId();
                    Option opt = new Option();
                    opt.setId(optId);
                    optId.setVotrId(votr.getId());
                    opcions = optionDao.loadByExample(opt);
                }
                for (Option o : opcions) {
                    User example = new User();
                    example.setVotrId(votr.getId());
                    example.setVotedOptionOrder(o.getId().getOrder());
                    optionsVots.put(o, userDao.loadByExample(example));
                }
            }

            List<Comment> comments;
            {
                Comment example = new Comment();
                example.setVotrId(votr.getId());
                comments = commentDao.loadByExample(example,
                        Order.by(Order.asc("commentDate"), Order.asc("commentId")));
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

    @Table("users")
    public static class User {

        @Id
        @Generated(value = HsqldbSequence.class, args = { "seq_users" })
        Long userId;

        String userHash;

        String email;

        String alias;

        Integer votrId;

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

        @Override
        public String toString() {
            return "User [userId=" + userId + ", userHash=" + userHash + ", email=" + email + ", alias=" + alias
                    + ", votrId=" + votrId + ", votedOptionId=" + votedOptionOrder + ", votedDate=" + votedDate + "]";
        }

    }

    @Embeddable
    public static class OptionId {

        Integer votrId;

        @Column("norder")
        Long order;

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