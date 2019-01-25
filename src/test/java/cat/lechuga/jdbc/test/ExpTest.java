package cat.lechuga.jdbc.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;
import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.JdbcDataAccesFacade;
import org.lenteja.jdbc.script.SqlScriptExecutor;

import cat.lechuga.jdbc.EntityManager;
import cat.lechuga.jdbc.EntityManagerFactory;
import cat.lechuga.jdbc.mql.QueryBuilder;

public class ExpTest {

    final DataAccesFacade facade;

    public ExpTest() {
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
            sql.runFromClasspath("sql/exp.sql");
            facade.commit();
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }
    }

    @Test
    public void testName() throws Exception {

        EntityManagerFactory emf = new EntityManagerFactory(facade);

        EntityManager<Exp, ExpId> expEm = emf.buildEntityManager(Exp.class);

        facade.begin();
        try {

            ExpId id1 = new ExpId();
            Exp exp1 = new Exp();
            exp1.setId(id1);

            id1.setIdEns(8L);
            id1.anyExp = 2019;
            id1.setNumExp(null);
            exp1.setFecIni(new Date(0L));
            exp1.setName("jou");
            exp1.setAlive(true);
            exp1.sex = ESex.MALE;

            assertEquals(
                    "Exp [id=ExpId [idEns=8, anyExp=2019, numExp=null], name=jou, fecIni=19700101, sex=MALE, alive=true]",
                    exp1.toString());

            expEm.insert(exp1);

            assertEquals(
                    "Exp [id=ExpId [idEns=8, anyExp=2019, numExp=10], name=jou, fecIni=19700101, sex=MALE, alive=true]",
                    exp1.toString());

            exp1.sex = ESex.FEMALE;
            expEm.update(exp1);
            exp1 = expEm.loadById(exp1.getId());

            assertEquals(
                    "Exp [id=ExpId [idEns=8, anyExp=2019, numExp=10], name=jou, fecIni=19700101, sex=FEMALE, alive=true]",
                    exp1.toString());

            {
                QueryBuilder qb = new QueryBuilder(facade);
                qb.addAlias("e", expEm);
                qb.append("select {e.*} ");
                qb.append("from {e.#} ");
                qb.append("where {e.id.anyExp=?} and {e.sex in (?,?)}", exp1.getId().anyExp, ESex.FEMALE, ESex.MALE);
                exp1 = qb.getExecutor(expEm).loadUnique();
            }

            assertTrue(expEm.exists(exp1));
            assertTrue(expEm.existsById(id1));

            expEm.refresh(exp1);
            assertEquals(
                    "Exp [id=ExpId [idEns=8, anyExp=2019, numExp=10], name=jou, fecIni=19700101, sex=FEMALE, alive=true]",
                    exp1.toString());

            expEm.delete(exp1);

            assertFalse(expEm.exists(exp1));
            assertFalse(expEm.existsById(id1));
            List<Exp> exps = expEm.loadAll();
            assertTrue(exps.isEmpty());

            facade.commit();
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }
    }

}
