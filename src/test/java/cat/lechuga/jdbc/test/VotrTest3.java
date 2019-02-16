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
import cat.lechuga.jdbc.MetaGeneratorTest.Comment_;
import cat.lechuga.jdbc.MetaGeneratorTest.Option_;
import cat.lechuga.jdbc.MetaGeneratorTest.User_;
import cat.lechuga.jdbc.MetaGeneratorTest.Votr_;
import cat.lechuga.jdbc.test.VotrTest.Comment;
import cat.lechuga.jdbc.test.VotrTest.Option;
import cat.lechuga.jdbc.test.VotrTest.OptionId;
import cat.lechuga.jdbc.test.VotrTest.User;
import cat.lechuga.jdbc.test.VotrTest.Votr;
import cat.lechuga.jdbc.test.VotrTest.VotrInfo;
import cat.lechuga.repository.Repository;
import cat.lechuga.repository.Specification;
import cat.lechuga.tsmql.Restrictions;
import cat.lechuga.tsmql.TOrders;
import cat.lechuga.tsmql.TOrders.TOrder;

public class VotrTest3 {

    final DataAccesFacade facade;

    public VotrTest3() {
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

    public static class VotrRespository extends Repository<Votr, Integer, Votr_> {

        public VotrRespository(EntityManager em) {
            super(em, new Votr_());
        }

        public Votr loadByHash(String hash) {
            return findUniqueBy(k -> k.votrHash.eq(hash)).orElseThrow(() -> new RuntimeException());
        }
    }

    public static class UserRepository extends Repository<User, Long, User_> {

        public UserRepository(EntityManager em) {
            super(em, new User_());
        }

        public User loadByHash(int votrId, String hashUser) {

            Specification<User_> s = u -> Restrictions.and( //
                    u.votrId.eq(votrId), //
                    u.userHash.eq(hashUser));
            return findUniqueBy(s).orElseThrow(() -> new RuntimeException());
        }

        public List<User> loadByVotr(int votrId) {
            return findBy(u -> u.votrId.eq(votrId));
        }

        public List<User> loadByVotrAndOption(int votrId, long votedOptionOrder) {

            return findBy( //
                    u -> Restrictions.and( //
                            u.votrId.eq(votrId), //
                            u.votedOptionOrder.eq(votedOptionOrder) //
                    ), //
                    u -> TOrders.by(TOrder.asc(u.email)));
        }
    }

    public static class OptionRepository extends Repository<Option, OptionId, Option_> {

        public OptionRepository(EntityManager em) {
            super(em, new Option_());
        }

        public List<Option> loadByVotr(int votrId) {
            return findBy(o -> o.votrId.eq(votrId), o -> TOrders.by(TOrder.asc(o.order)));
        }
    }

    public static class CommentRepository extends Repository<Comment, Long, Comment_> {

        public CommentRepository(EntityManager em) {
            super(em, new Comment_());
        }

        public List<Comment> loadByVotr(int votrId) {
            return findBy(c -> c.votrId.eq(votrId),
                    c -> TOrders.by(TOrder.asc(c.commentDate), TOrder.asc(c.commentId)));
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

        final VotrRespository votrRepository;
        final UserRepository userRepository;
        final OptionRepository optionRepository;
        final CommentRepository commentRepository;

        public VotrServiceImpl(DataAccesFacade facade) {
            super();
            EntityManager em = new EntityManagerFactory().buildEntityManager(facade, Votr.class, User.class,
                    Option.class, Comment.class);
            this.votrRepository = new VotrRespository(em);
            this.userRepository = new UserRepository(em);
            this.optionRepository = new OptionRepository(em);
            this.commentRepository = new CommentRepository(em);
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
            commentRepository.save(c);
        }

        @Override
        public void createVotr(Votr v, User creator) {
            v.setId(null);
            v.setCreacio(new Date(0L));
            v.setVotrHash(generaHash(v.getTitle() + v.getDesc() + v.getCreacio().getTime()));
            votrRepository.save(v);

            creator.setUserId(null);
            creator.setVotedDate(null);
            creator.setVotedOptionOrder(null);
            creator.setVotrId(v.getId());
            creator.setUserHash(generaHash(creator.getEmail()));
            userRepository.save(creator);

            createComment("alo, votr created.", v.getId(), creator.getUserId());
        }

        @Override
        public void convidaUser(String hashVotr, User u) {

            Votr votr = votrRepository.loadByHash(hashVotr);

            u.setUserId(null);
            u.setVotedDate(null);
            u.setVotedOptionOrder(null);
            u.setVotrId(votr.getId());
            u.setUserHash(generaHash(u.getEmail() + u.getAlias() + votr.getId()));
            userRepository.save(u);

            createComment("he sigut convidat", votr.getId(), u.getUserId());
        }

        @Override
        public void expulsaUser(String hashVotr, String hashUser) {

            Votr votr = votrRepository.loadByHash(hashVotr);
            User u = userRepository.loadByHash(votr.getId(), hashUser);
            userRepository.delete(u);

            createComment("he sigut expulsat", votr.getId(), u.getUserId());
        }

        @Override
        public void creaOptions(String hashVotr, List<Option> options, String hashUserModifier) {

            Votr votr = votrRepository.loadByHash(hashVotr);

            /*
             * devota tots els users
             */
            List<User> us = userRepository.loadByVotr(votr.getId());
            for (User u : us) {
                u.setVotedOptionOrder(null);
            }
            userRepository.saveAll(us);

            /*
             * s'esborren les opcions que puguin existir
             */
            List<Option> os = optionRepository.loadByVotr(votr.getId());
            optionRepository.deleteAll(os);

            long order = 0;
            for (Option o : options) {
                o.setId(new OptionId());
                o.getId().setVotrId(votr.getId());
                o.getId().setOrder(order++);
                optionRepository.save(o);
            }

            User userModifier = userRepository.loadByHash(votr.getId(), hashUserModifier);
            createComment("s'han editat les opcions, tothom desvota", votr.getId(), userModifier.getUserId());
        }

        @Override
        public void userUpdateAlias(String hashVotr, String hashUser, String alias) {

            Votr votr = votrRepository.loadByHash(hashVotr);
            User user = userRepository.loadByHash(votr.getId(), hashUser);
            user.setAlias(alias);
            userRepository.save(user);
        }

        @Override
        public void userVota(String hashVotr, String hashUser, Long optionOrderVotat) {

            Votr votr = votrRepository.loadByHash(hashVotr);
            User user = userRepository.loadByHash(votr.getId(), hashUser);
            if (optionOrderVotat != null) {
                // valida que existeixi
                optionRepository.findById(new OptionId(votr.getId(), optionOrderVotat));
            }
            user.setVotedOptionOrder(optionOrderVotat);
            userRepository.save(user);
        }

        @Override
        public void userComenta(String hashVotr, String hashUser, String comment) {
            Votr votr = votrRepository.loadByHash(hashVotr);
            User user = userRepository.loadByHash(votr.getId(), hashUser);
            createComment(comment, votr.getId(), user.getUserId());
        }

        @Override
        public VotrInfo getVotrInfo(String hashVotr, String hashUser) {

            Votr votr = votrRepository.loadByHash(hashVotr);
            User you = userRepository.loadByHash(votr.getId(), hashUser);
            List<User> allUsers = userRepository.loadByVotr(votr.getId());

            Map<Option, List<User>> optionsVots = new LinkedHashMap<>();
            List<Option> os = optionRepository.loadByVotr(votr.getId());
            for (Option o : os) {
                List<User> us = userRepository.loadByVotrAndOption(votr.getId(), o.getId().getOrder());
                optionsVots.put(o, us);
            }

            List<Comment> comments = commentRepository.loadByVotr(votr.getId());

            return new VotrInfo(votr, you, allUsers, optionsVots, comments);
        }

    }

}
