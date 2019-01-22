package org.lenteja.test;

import java.util.Date;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.JdbcDataAccesFacade;
import org.lenteja.jdbc.extractor.EntityMapResultSetExtractor2;
import org.lenteja.jdbc.script.SqlScriptExecutor;
import org.lenteja.jdbc.txproxy.TransactionalMethod;
import org.lenteja.jdbc.txproxy.TransactionalServiceProxyfier;
import org.lenteja.mapper.Column;
import org.lenteja.mapper.GenericDao;
import org.lenteja.mapper.Table;
import org.lenteja.mapper.TableGenerator;
import org.lenteja.mapper.autogen.ScalarMappers;
import org.lenteja.mapper.autogen.impl.HsqldbSequence;
import org.lenteja.mapper.query.Restrictions;
import org.lenteja.test.VotrTest.ServiceImpl.VistaUsuariDto;

@Ignore // TODO
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
            sql.runFromClasspath("votr.sql");
            facade.commit();
        } catch (Exception e) {
            facade.rollback();
            throw e;
        }
    }

    @Test
    public void testName() throws Exception {

        // drop table votrs if exists;
        // drop table users if exists;
        // drop table options if exists;
        // drop table comments if exists;

        TableGenerator g = new TableGenerator(facade);
        System.out.println(g.generate("Votr", "votrs"));
        System.out.println(g.generate("User", "users"));
        System.out.println(g.generate("Option", "options"));
        System.out.println(g.generate("Comment", "comments"));

        Service s = TransactionalServiceProxyfier.proxyfy(facade, new ServiceImpl(facade), Service.class);

        User u1 = new User();
        u1.email = "mhc@votr.org";

        Votr v1 = new Votr();
        v1.title = "best poem";
        v1.descr = "best poem of all the times";
        s.creaVotacio(v1, u1);

        s.actualitzaUsuari(v1.votrHash, u1.userHash, "mhc", null);

        User u2 = new User();
        u2.email = "mem@votr.org";
        s.convidaUsuari(v1.votrHash, u2);

        Option eneida = new Option();
        eneida.title = "aeneid";
        eneida.descr = "la eneida";
        Option iliada = new Option();
        iliada.title = "iliada";
        iliada.descr = "la iliada";
        Option odisea = new Option();
        odisea.title = "odisea";
        odisea.descr = "la odisea";
        s.creaOrModificaOpcio(v1.votrHash, eneida);
        s.creaOrModificaOpcio(v1.votrHash, iliada);
        s.creaOrModificaOpcio(v1.votrHash, odisea);
        s.eliminaOpcio(v1.votrHash, iliada.optionId);

    }

    public class VotrDao extends GenericDao<Votr, Integer> {
        public VotrDao(DataAccesFacade facade) {
            super(facade, new VotrTable());
        }
    }

    public class UserDao extends GenericDao<User, Long> {
        public UserDao(DataAccesFacade facade) {
            super(facade, new UserTable());
        }
    }

    public class OptionDao extends GenericDao<Option, Long> {
        public OptionDao(DataAccesFacade facade) {
            super(facade, new OptionTable());
        }
    }

    public class CommentDao extends GenericDao<Comment, Long> {
        public CommentDao(DataAccesFacade facade) {
            super(facade, new CommentTable());
        }
    }

    public interface Service {

        void creaVotacio(Votr votr, User user);

        VistaUsuariDto vistaUsuari(String hashVotr, String hashUser);

        void eliminaOpcio(String hashVotr, long idOpcio);

        void creaOrModificaOpcio(String hashVotr, Option option);

        void convidaUsuari(String hashVotr, User user);

        void actualitzaUsuari(String hashVotr, String hashUser, String alias, Long idOpcioVotada);
    }

    public class ServiceImpl implements Service {

        final DataAccesFacade facade;
        final VotrDao votrDao;
        final UserDao userDao;
        final OptionDao optionDao;
        final CommentDao commentDao;

        final VotrTable votr_ = new VotrTable();
        final UserTable user_ = new UserTable();
        final OptionTable option_ = new OptionTable();
        final CommentTable comment_ = new CommentTable();

        public ServiceImpl(DataAccesFacade facade) {
            super();
            this.facade = facade;
            this.votrDao = new VotrDao(facade);
            this.userDao = new UserDao(facade);
            this.optionDao = new OptionDao(facade);
            this.commentDao = new CommentDao(facade);
        }

        protected String generaHash(String input) {
            return Integer.toHexString(input.hashCode());
        }

        @Override
        @TransactionalMethod
        public void creaVotacio(Votr votr, User user) {

            votr.votrId = null;
            votr.votrHash = generaHash(votr.title + votr.descr);
            votr.creatDate = new Date();
            votrDao.insert(votr);

            convidaUsuari(votr.votrHash, user);

            Comment c = new Comment();
            c.commentId = null;
            c.commentDate = new Date();
            c.comment = "he creat aquesta votació, ara us convido.";
            c.userId = user.userId;
            c.votrId = votr.votrId;
            commentDao.insert(c);
        }

        @Override
        @TransactionalMethod
        public void creaOrModificaOpcio(String hashVotr, Option option) {

            Votr votr = votrDao.queryUnique(votr_.votrHash.eq(hashVotr));

            option.optionId = null;
            option.votrId = votr.votrId;
            optionDao.store(option);
        }

        @Override
        @TransactionalMethod
        public void eliminaOpcio(String hashVotr, long idOpcio) {

            Votr votr = votrDao.queryUnique(votr_.votrHash.eq(hashVotr));

            Option option = optionDao.queryUnique(Restrictions.and( //
                    option_.optionId.eq(idOpcio), //
                    option_.votrId.eq(votr.votrId)));

            optionDao.delete(option);
        }

        @Override
        @TransactionalMethod
        public void convidaUsuari(String hashVotr, User user) {

            Votr votr = votrDao.queryUnique(votr_.votrHash.eq(hashVotr));

            user.userId = null;
            user.userHash = generaHash(user.email);
            user.optionDate = null;
            user.optionId = null;
            user.votrId = votr.votrId;
            userDao.insert(user);
        }

        @Override
        @TransactionalMethod
        public void actualitzaUsuari(String hashVotr, String hashUser, String alias, Long idOpcioVotada) {

            Votr votr = votrDao.queryUnique(votr_.votrHash.eq(hashVotr));
            User user = userDao.queryUnique( //
                    Restrictions.and( //
                            user_.userHash.eq(hashUser), //
                            user_.votrId.eq(votr.votrId) //
                    ));
            user.alias = alias;
            user.optionId = idOpcioVotada;
            userDao.update(user);
        }

        @Override
        @TransactionalMethod(readOnly = true)
        public VistaUsuariDto vistaUsuari(String hashVotr, String hashUser) {

            Votr votr = votrDao.queryUnique(votr_.votrHash.eq(hashVotr));
            User user = userDao.queryUnique( //
                    Restrictions.and( //
                            user_.userHash.eq(hashUser), //
                            user_.votrId.eq(votr.votrId) //
                    ));

            // Map<Integer, User> users = userDao.queryFor() //
            // .append("select {} from {} ", u.all(), u) //
            // .append("join {} on {} ", tu, u.idUser.eq(tu.idUser)) //
            // .append("join {} on {} ", tu, t.idTicket.eq(tu.idTicket)) //
            // .append("where {} ", t.idTicket.eq(idTicket)) //
            // .append("order by {} ", Order.asc(tu.joined)) //
            // .getExecutor(facade).extract(new EntityMapResultSetExtractor<Integer,
            // User>(u, uu -> uu.idUser));

            votrDao.queryFor() //
                    .append("select * ") //
                    .append("from {}") //
                    .append("") //
                    .append("") //
                    .append("") //
                    .append("") //
                    .getExecutor(facade)
                    .extract(new EntityMapResultSetExtractor2<Integer, Option>(ScalarMappers.INTEGER, option_));

            ;

            // FIXME

            return null;
        }

        public class VistaUsuariDto {

            Votr votr;
            User user;

        }
    }

    public class VotrTable extends Table<Votr> {

        public final Column<Votr, Integer> votrId = addPkColumn(Integer.class, "votrId", "VOTR_ID");
        public final Column<Votr, String> votrHash = addColumn(String.class, "votrHash", "VOTR_HASH");
        public final Column<Votr, String> title = addColumn(String.class, "title", "TITLE");
        public final Column<Votr, String> descr = addColumn(String.class, "descr", "DESCR");
        public final Column<Votr, Date> creatDate = addColumn(Date.class, "creatDate", "CREAT_DATE");

        public VotrTable(String alias) {
            super("VOTRS", alias);
            addAutoGenerated(new HsqldbSequence<>(votrId, "seq_votrs"));
        }

        public VotrTable() {
            this(null);
        }
    }

    public static class Votr {

        /** PK */
        public Integer votrId;
        public String votrHash;
        public String title;
        public String descr;
        public Date creatDate;
    }

    public class UserTable extends Table<User> {

        public final Column<User, Long> userId = addPkColumn(Long.class, "userId", "USER_ID");
        public final Column<User, String> userHash = addColumn(String.class, "userHash", "USER_HASH");
        public final Column<User, String> email = addColumn(String.class, "email", "EMAIL");
        public final Column<User, String> alias = addColumn(String.class, "alias", "ALIAS");
        public final Column<User, Integer> votrId = addColumn(Integer.class, "votrId", "VOTR_ID");
        public final Column<User, Long> optionId = addColumn(Long.class, "optionId", "OPTION_ID");
        public final Column<User, Date> optionDate = addColumn(Date.class, "optionDate", "OPTION_DATE");

        public UserTable(String alias) {
            super("USERS", alias);
            addAutoGenerated(new HsqldbSequence<>(userId, "seq_users"));
        }

        public UserTable() {
            this(null);
        }
    }

    public static class User {

        /** PK */
        public Long userId;
        public String userHash;
        public String email;
        public String alias;
        public Integer votrId;
        public Long optionId;
        public Date optionDate;
    }

    public class OptionTable extends Table<Option> {

        public final Column<Option, Long> optionId = addPkColumn(Long.class, "optionId", "OPTION_ID");
        public final Column<Option, String> title = addColumn(String.class, "title", "TITLE");
        public final Column<Option, String> descr = addColumn(String.class, "descr", "DESCR");
        public final Column<Option, Integer> votrId = addColumn(Integer.class, "votrId", "VOTR_ID");

        public OptionTable(String alias) {
            super("OPTIONS", alias);
            addAutoGenerated(new HsqldbSequence<>(optionId, "seq_options"));
        }

        public OptionTable() {
            this(null);
        }
    }

    public static class Option {

        /** PK */
        public Long optionId;
        public String title;
        public String descr;
        public Integer votrId;
    }

    public class CommentTable extends Table<Comment> {

        public final Column<Comment, Long> commentId = addPkColumn(Long.class, "commentId", "COMMENT_ID");
        public final Column<Comment, Date> commentDate = addColumn(Date.class, "commentDate", "COMMENT_DATE");
        public final Column<Comment, String> comment = addColumn(String.class, "comment", "COMMENT");
        public final Column<Comment, Integer> votrId = addColumn(Integer.class, "votrId", "VOTR_ID");
        public final Column<Comment, Long> userId = addColumn(Long.class, "userId", "USER_ID");

        public CommentTable(String alias) {
            super("COMMENTS", alias);
            addAutoGenerated(new HsqldbSequence<>(commentId, "seq_comments"));
        }

        public CommentTable() {
            this(null);
        }
    }

    public static class Comment {

        /** PK */
        public Long commentId;
        public Date commentDate;
        public String comment;
        public Integer votrId;
        public Long userId;
    }

}
