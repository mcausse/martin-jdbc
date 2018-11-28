package org.lenteja.test;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;
import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.JdbcDataAccesFacade;
import org.lenteja.jdbc.extractor.EntityMapResultSetExtractor;
import org.lenteja.jdbc.script.SqlScriptExecutor;
import org.lenteja.jdbc.txproxy.TransactionalMethod;
import org.lenteja.jdbc.txproxy.TransactionalServiceProxyfier;
import org.lenteja.mapper.Column;
import org.lenteja.mapper.EntityManager;
import org.lenteja.mapper.GenericDao;
import org.lenteja.mapper.Table;
import org.lenteja.mapper.TableGenerator;
import org.lenteja.mapper.autogen.impl.HsqldbIdentity;
import org.lenteja.mapper.autogen.impl.HsqldbSequence;
import org.lenteja.mapper.handler.EnumColumnHandler;
import org.lenteja.mapper.query.Order;
import org.lenteja.mapper.query.Restrictions;
import org.lenteja.test.TicketsTest.ServiceImpl.TicketDetail;

public class TicketsTest {

    final DataAccesFacade facade;

    public TicketsTest() {
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
            sql.runFromClasspath("tickets.sql");
            facade.commit();
        } catch (Exception e) {
            facade.rollback();
            throw e;
        }
    }

    @Test
    public void testName() throws Exception {
        TableGenerator g = new TableGenerator(facade);
        System.out.println(g.generate("Ticket", "tickets"));
        System.out.println(g.generate("User", "users"));
        System.out.println(g.generate("TicketUser", "ticket_user"));
        System.out.println(g.generate("TicketAction", "actions"));

        Service s = TransactionalServiceProxyfier.proxyfy(facade, new ServiceImpl(facade), Service.class);

        User mhc = new User();
        mhc.email = "mhc@votr.com";
        s.storeUser(mhc);

        User eib = new User();
        eib.email = "eib@votr.com";
        s.storeUser(eib);

        Ticket ticket1 = new Ticket();
        ticket1.title = "falla tot";
        s.createNewTicket(ticket1, mhc);

        s.inviteUser(ticket1, eib);

        Action action = new Action();
        action.message = "encara passa?";
        s.createNewAction(ticket1, eib, action);

        System.out.println(s.obtenirTicket(ticket1.idTicket));

        assertEquals("TicketDetail [ticket=Ticket [idTicket=10, title=falla tot, created=1970-01-01 01:00:00.0, "
                + "idUserCreated=100], userCreated=User [idUser=100, email=mhc@votr.com], "
                + "users={100=User [idUser=100, email=mhc@votr.com], 101=User [idUser=101, email=eib@votr.com]}, "
                + "actions=["
                + "Action [idAction=10, ticketUserId=TicketUserId [idTicket=10, idUser=100], state=CREAT, message=ticket created, date=1970-01-01 01:00:00.0], "
                + "Action [idAction=11, ticketUserId=TicketUserId [idTicket=10, idUser=101], state=null, message=encara passa?, date=1970-01-01 01:00:00.0]]"
                + ", state=CREAT]", s.obtenirTicket(ticket1.idTicket).toString());

    }

    public static interface Service {

        TicketDetail obtenirTicket(int idTicket);

        void inviteUser(Ticket ticket, User user);

        void createNewAction(Ticket ticket, User user, Action action);

        void createNewTicket(Ticket ticket, User creator);

        void storeUser(User user);

    }

    public static class ServiceImpl implements Service {

        final DataAccesFacade facade;
        final TicketDao ticketDao;
        final UserDao userDao;
        final ActionDao actionDao;

        public ServiceImpl(DataAccesFacade facade) {
            super();
            this.facade = facade;
            this.ticketDao = new TicketDao(facade);
            this.userDao = new UserDao(facade);
            this.actionDao = new ActionDao(facade);
        }

        @TransactionalMethod
        @Override
        public void storeUser(User user) {
            userDao.store(user);
        }

        @TransactionalMethod
        @Override
        public void createNewTicket(Ticket ticket, User creator) {
            ticket.idUserCreated = creator.idUser;
            ticket.created = new Date(0L);
            ticketDao.insert(ticket);

            inviteUser(ticket, creator);
            Action action = new Action();
            action.message = "ticket created";
            action.state = ETicketState.CREAT;
            createNewAction(ticket, creator, action);
        }

        @TransactionalMethod
        @Override
        public void inviteUser(Ticket ticket, User user) {
            TicketUser tue = new TicketUser();
            tue.ticketUserId = new TicketUserId();
            tue.ticketUserId.idTicket = ticket.idTicket;
            tue.ticketUserId.idUser = user.idUser;
            tue.joined = new Date(0L);

            TicketUserTable tu = new TicketUserTable();
            EntityManager em = new EntityManager(facade);
            em.store(tu, tue);
        }

        @TransactionalMethod
        @Override
        public void createNewAction(Ticket ticket, User user, Action action) {
            action.ticketUserId = new TicketUserId();
            action.ticketUserId.idTicket = ticket.idTicket;
            action.ticketUserId.idUser = user.idUser;
            action.date = new Date(0L);
            actionDao.insert(action);
        }

        @TransactionalMethod(readOnly = true)
        @Override
        public TicketDetail obtenirTicket(int idTicket) {

            Ticket ticket = ticketDao.loadById(idTicket);
            User userCreated = userDao.loadById(ticket.idUserCreated);

            UserTable u = UserDao.TABLE;
            TicketUserTable tu = new TicketUserTable();
            TicketTable t = TicketDao.TABLE;
            ActionTable a = ActionDao.TABLE;

            Map<Integer, User> users = userDao.queryFor() //
                    .append("select {} from {} ", u.all(), u) //
                    .append("join {} on {} ", tu, u.idUser.eq(tu.idUser)) //
                    .append("join {} on {} ", tu, t.idTicket.eq(tu.idTicket)) //
                    .append("where {} ", t.idTicket.eq(idTicket)) //
                    .append("order by {} ", Order.asc(tu.joined)) //
                    .getExecutor(facade).extract(new EntityMapResultSetExtractor<Integer, User>(u, uu -> uu.idUser));

            List<Action> actions = actionDao.queryFor() //
                    .append("select {} from {} ", a.all(), a) //
                    .append("where {} ", t.idTicket.eq(idTicket)) //
                    .append("order by {} ", Order.asc(a.moment)) //
                    .getExecutor(facade).load();

            ETicketState state = actionDao.scalarQueryFor(a.state) //
                    .append("select {} from {} ", a.state, a) //
                    .append("where {} ", Restrictions.and(a.idTick.eq(idTicket), a.state.isNotNull())) //
                    .append("order by {} ", Order.desc(a.moment)) //
                    .getExecutor(facade).loadFirst();

            return new TicketDetail(ticket, userCreated, users, actions, state);
        }

        public static class TicketDetail {

            public final Ticket ticket;
            public final User userCreated;
            public final Map<Integer, User> users;
            public final List<Action> actions;
            public final ETicketState state;

            public TicketDetail(Ticket ticket, User userCreated, Map<Integer, User> users, List<Action> actions,
                    ETicketState state) {
                super();
                this.ticket = ticket;
                this.userCreated = userCreated;
                this.users = users;
                this.actions = actions;
                this.state = state;
            }

            @Override
            public String toString() {
                return "TicketDetail [ticket=" + ticket + ", userCreated=" + userCreated + ", users=" + users
                        + ", actions=" + actions + ", state=" + state + "]";
            }

        }
    }

    // TODO i fer un generador Table => entity+dao

    public static class TicketDao extends GenericDao<Ticket, Integer> {

        public static final TicketTable TABLE = new TicketTable();

        public TicketDao(DataAccesFacade facade) {
            super(facade, TABLE);
        }

    }

    public static class UserDao extends GenericDao<User, Integer> {

        public static final UserTable TABLE = new UserTable();

        public UserDao(DataAccesFacade facade) {
            super(facade, TABLE);
        }

    }

    public static class ActionDao extends GenericDao<Action, Integer> {

        public static final ActionTable TABLE = new ActionTable();

        public ActionDao(DataAccesFacade facade) {
            super(facade, TABLE);
        }

    }

    public static class TicketTable extends Table<Ticket> {

        public final Column<Ticket, Integer> idTicket = addPkColumn(Integer.class, "idTicket", "ID_TICK");
        public final Column<Ticket, String> title = addColumn(String.class, "title", "TITLE");
        public final Column<Ticket, Date> created = addColumn(Date.class, "created", "CREATED");
        public final Column<Ticket, Integer> idUserCreated = addColumn(Integer.class, "idUserCreated",
                "ID_USER_CREATED");

        public TicketTable(String alias) {
            super("TICKETS", alias);
            addAutoGenerated(new HsqldbIdentity<>(idTicket));
        }

        public TicketTable() {
            this(null);
        }
    }

    public static class UserTable extends Table<User> {

        public final Column<User, Integer> idUser = addPkColumn(Integer.class, "idUser", "ID_USER");
        public final Column<User, String> email = addColumn(String.class, "email", "EMAIL");

        public UserTable(String alias) {
            super("USERS", alias);
            addAutoGenerated(new HsqldbIdentity<>(idUser));
        }

        public UserTable() {
            this(null);
        }
    }

    public static class TicketUserTable extends Table<TicketUser> {

        public final Column<TicketUser, Integer> idTicket = addPkColumn(Integer.class, "ticketUserId.idTicket",
                "ID_TICK");
        public final Column<TicketUser, Integer> idUser = addPkColumn(Integer.class, "ticketUserId.idUser", "ID_USER");
        public final Column<TicketUser, Date> joined = addColumn(Date.class, "joined", "JOINED_MOMENT");

        public TicketUserTable(String alias) {
            super("TICKET_USER", alias);
        }

        public TicketUserTable() {
            this(null);
        }
    }

    public static class ActionTable extends Table<Action> {

        public final Column<Action, Integer> idAction = addPkColumn(Integer.class, "idAction", "ID_ACTION");
        public final Column<Action, Integer> idTick = addPkColumn(Integer.class, "ticketUserId.idTicket", "ID_TICK");
        public final Column<Action, Integer> idUser = addPkColumn(Integer.class, "ticketUserId.idUser", "ID_USER");
        public final Column<Action, ETicketState> state = addColumn(ETicketState.class, "state", "STATE",
                new EnumColumnHandler<>(ETicketState.class));
        public final Column<Action, Date> moment = addColumn(Date.class, "date", "MOMENT");
        public final Column<Action, String> message = addColumn(String.class, "message", "MESSAGE");

        public ActionTable(String alias) {
            super("ACTIONS", alias);
            addAutoGenerated(new HsqldbSequence<>(idAction, "seq_actions"));
        }

        public ActionTable() {
            this(null);
        }
    }

    public static class Ticket {
        public Integer idTicket;
        public String title;
        public Date created;
        public Integer idUserCreated;

        @Override
        public String toString() {
            return "Ticket [idTicket=" + idTicket + ", title=" + title + ", created=" + created + ", idUserCreated="
                    + idUserCreated + "]";
        }
    }

    public static class User {
        public Integer idUser;
        public String email;

        @Override
        public String toString() {
            return "User [idUser=" + idUser + ", email=" + email + "]";
        }
    }

    public static class TicketUserId {
        public Integer idTicket;
        public Integer idUser;

        @Override
        public String toString() {
            return "TicketUserId [idTicket=" + idTicket + ", idUser=" + idUser + "]";
        }
    }

    public static class TicketUser {
        public TicketUserId ticketUserId;
        public Date joined;

        @Override
        public String toString() {
            return "TicketUser [ticketUserId=" + ticketUserId + ", joined=" + joined + "]";
        }
    }

    public static class Action {
        public Integer idAction;
        public TicketUserId ticketUserId;
        public ETicketState state;
        public String message;
        public Date date;

        @Override
        public String toString() {
            return "Action [idAction=" + idAction + ", ticketUserId=" + ticketUserId + ", state=" + state + ", message="
                    + message + ", date=" + date + "]";
        }
    }

    public static enum ETicketState {
        CREAT, EN_CURS, RESOLT, REOBERT
    }

}
