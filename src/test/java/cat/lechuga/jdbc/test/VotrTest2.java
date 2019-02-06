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

import cat.lechuga.EntityManager;
import cat.lechuga.EntityManagerFactory;
import cat.lechuga.GenericDao;
import cat.lechuga.jdbc.test.VotrTest.Comment;
import cat.lechuga.jdbc.test.VotrTest.Option;
import cat.lechuga.jdbc.test.VotrTest.OptionId;
import cat.lechuga.jdbc.test.VotrTest.User;
import cat.lechuga.jdbc.test.VotrTest.Votr;
import cat.lechuga.jdbc.test.VotrTest.VotrInfo;

public class VotrTest2 {

    final DataAccesFacade facade;

    public VotrTest2() {
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

        service.creaOptions(v.getVotrHash(), Arrays.asList(o1, o2), creator.getUserHash());
        service.creaOptions(v.getVotrHash(), Arrays.asList(o1, o2), creator.getUserHash());

        User user2 = new User();
        user2.setEmail("mem@votr.com");

        service.convidaUser(v.getVotrHash(), user2);

        service.creaOptions(v.getVotrHash(), Arrays.asList(o1, o2), creator.getUserHash());

        service.userUpdateAlias(v.getVotrHash(), creator.getUserHash(), "mhc");
        service.userVota(v.getVotrHash(), creator.getUserHash(), o1.getId().getOrder());

        System.out.println(service.getVotrInfo(v.getVotrHash(), user2.getUserHash()));
    }

    public static class VotrDao extends GenericDao<Votr, Integer> {

        public VotrDao(EntityManager em) {
            super(em);
        }

        public Votr loadByHash(String hash) {
            return buildQuery() //
                    .addAlias("v", Votr.class) //
                    .append("select {v.*} from {v.#} where {v.votrHash=?}", hash) //
                    .getExecutor(Votr.class) //
                    .loadUnique();
        }
    }

    public static class UserDao extends GenericDao<User, Long> {

        public UserDao(EntityManager em) {
            super(em);
        }

        public User loadByHash(int votrId, String hashUser) {
            return buildQuery() //
                    .addAlias("u", User.class) //
                    .append("select {u.*} from {u.#} ") //
                    .append("where {u.userHash=?} and {u.votrId=?}", hashUser, votrId) //
                    .getExecutor(User.class) //
                    .loadUnique();
        }

        public List<User> loadByVotr(int votrId) {
            return buildQuery() //
                    .addAlias("u", User.class) //
                    .append("select {u.*} from {u.#} ") //
                    .append("where {u.votrId=?}", votrId) //
                    .append("order by {u.email} asc") //
                    .getExecutor(User.class) //
                    .load();
        }

        public List<User> loadByVotrAndOption(int votrId, long votedOptionOrder) {
            return buildQuery() //
                    .addAlias("u", User.class) //
                    .append("select {u.*} from {u.#} ") //
                    .append("where {u.votrId=?} ", votrId) //
                    .append("and {u.votedOptionOrder=?} ", votedOptionOrder) //
                    .append("order by {u.email} asc") //
                    .getExecutor(User.class) //
                    .load();
        }
    }

    public static class OptionDao extends GenericDao<Option, OptionId> {

        public OptionDao(EntityManager em) {
            super(em);
        }

        public List<Option> loadByVotr(int votrId) {
            return buildQuery() //
                    .addAlias("o", Option.class) //
                    .append("select {o.*} from {o.#} ") //
                    .append("where {o.id.votrId=?}", votrId) //
                    .append("order by {o.id.order} asc") //
                    .getExecutor(Option.class) //
                    .load();
        }
    }

    public static class CommentDao extends GenericDao<Comment, Long> {

        public CommentDao(EntityManager em) {
            super(em);
        }

        public List<Comment> loadByVotr(int votrId) {
            return buildQuery() //
                    .addAlias("c", Comment.class) //
                    .append("select {c.*} from {c.#} ") //
                    .append("where {c.votrId=?}", votrId) //
                    .append("order by {c.commentDate} asc, {c.commentId} asc") //
                    .getExecutor(Comment.class) //
                    .load() //
            ;
        }
    }

    public static interface VotrService {

        /**
         * Crea votació base amb usuari creador. Inserta missatge de creació,
         */
        @TransactionalMethod
        void createVotr(Votr v, User creator);

        @TransactionalMethod
        void convidaUser(String hashVotr, User user);

        @TransactionalMethod
        void expulsaUser(String hashVotr, String hashUser);

        /**
         * desvota a tots els usuaris. borra les opcions que ja existeixin. Inserta les
         * opcions, enumerant-les. comenta.
         */
        @TransactionalMethod
        void creaOptions(String hashVotr, List<Option> option, String hashUserModifier);

        /**
         * @param alias si null, esborra alias
         */
        @TransactionalMethod
        void userUpdateAlias(String hashVotr, String hashUser, String alias);

        /**
         * @param optionOrderVotat si null, desvota.
         */
        @TransactionalMethod
        void userVota(String hashVotr, String hashUser, Long optionOrderVotat);

        @TransactionalMethod
        void userComenta(String hashVotr, String hashUser, String comment);

        @TransactionalMethod(readOnly = true)
        VotrInfo getVotrInfo(String hashVotr, String hashUser);

    }

    public static class VotrServiceImpl implements VotrService {

        final VotrDao votrDao;
        final UserDao userDao;
        final OptionDao optionDao;
        final CommentDao commentDao;

        public VotrServiceImpl(DataAccesFacade facade) {
            super();
            EntityManager em = new EntityManagerFactory().buildEntityManager(facade, Votr.class, User.class,
                    Option.class, Comment.class);
            this.votrDao = new VotrDao(em);
            this.userDao = new UserDao(em);
            this.optionDao = new OptionDao(em);
            this.commentDao = new CommentDao(em);
        }

        protected String generaHash(String input) {
            return Integer.toHexString(input.hashCode());
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

        @Override
        public void convidaUser(String hashVotr, User u) {

            Votr votr = votrDao.loadByHash(hashVotr);

            u.setUserId(null);
            u.setVotedDate(null);
            u.setVotedOptionOrder(null);
            u.setVotrId(votr.getId());
            u.setUserHash(generaHash(u.getEmail() + u.getAlias() + votr.getId()));
            userDao.insert(u);

            createComment("he sigut convidat", votr.getId(), u.getUserId());
        }

        @Override
        public void expulsaUser(String hashVotr, String hashUser) {

            Votr votr = votrDao.loadByHash(hashVotr);
            User u = userDao.loadByHash(votr.getId(), hashUser);
            userDao.delete(u);

            createComment("he sigut expulsat", votr.getId(), u.getUserId());
        }

        @Override
        public void creaOptions(String hashVotr, List<Option> options, String hashUserModifier) {

            Votr votr = votrDao.loadByHash(hashVotr);

            /*
             * devota tots els users
             */
            List<User> us = userDao.loadByVotr(votr.getId());
            for (User u : us) {
                u.setVotedOptionOrder(null);
            }
            userDao.storeAll(us);

            /*
             * s'esborren les opcions que puguin existir
             */
            List<Option> os = optionDao.loadByVotr(votr.getId());
            optionDao.deleteAll(os);

            long order = 0;
            for (Option o : options) {
                o.setId(new OptionId());
                o.getId().setVotrId(votr.getId());
                o.getId().setOrder(order++);
                optionDao.store(o);
            }

            User userModifier = userDao.loadByHash(votr.getId(), hashUserModifier);
            createComment("s'han editat les opcions, tothom desvota", votr.getId(), userModifier.getUserId());
        }

        @Override
        public void userUpdateAlias(String hashVotr, String hashUser, String alias) {

            Votr votr = votrDao.loadByHash(hashVotr);
            User user = userDao.loadByHash(votr.getId(), hashUser);
            user.setAlias(alias);
            userDao.update(user);
        }

        @Override
        public void userVota(String hashVotr, String hashUser, Long optionOrderVotat) {

            Votr votr = votrDao.loadByHash(hashVotr);
            User user = userDao.loadByHash(votr.getId(), hashUser);
            if (optionOrderVotat != null) {
                // valida que existeixi
                optionDao.loadById(new OptionId(votr.getId(), optionOrderVotat));
            }
            user.setVotedOptionOrder(optionOrderVotat);
            userDao.update(user);
        }

        @Override
        public void userComenta(String hashVotr, String hashUser, String comment) {
            Votr votr = votrDao.loadByHash(hashVotr);
            User user = userDao.loadByHash(votr.getId(), hashUser);
            createComment(comment, votr.getId(), user.getUserId());
        }

        @Override
        public VotrInfo getVotrInfo(String hashVotr, String hashUser) {

            Votr votr = votrDao.loadByHash(hashVotr);
            User you = userDao.loadByHash(votr.getId(), hashUser);
            List<User> allUsers = userDao.loadByVotr(votr.getId());

            Map<Option, List<User>> optionsVots = new LinkedHashMap<>();
            List<Option> os = optionDao.loadByVotr(votr.getId());
            for (Option o : os) {
                List<User> us = userDao.loadByVotrAndOption(votr.getId(), o.getId().getOrder());
                optionsVots.put(o, us);
            }

            List<Comment> comments = commentDao.loadByVotr(votr.getId());

            return new VotrInfo(votr, you, allUsers, optionsVots, comments);
        }

    }

}
