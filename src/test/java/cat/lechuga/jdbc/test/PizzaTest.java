package cat.lechuga.jdbc.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;
import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.JdbcDataAccesFacade;
import org.lenteja.jdbc.script.SqlScriptExecutor;

import cat.lechuga.EntityManager;
import cat.lechuga.EntityManagerFactory;
import cat.lechuga.generator.ScalarMappers;
import cat.lechuga.tsmql.MetaColumn;
import cat.lechuga.tsmql.MetaGenerator;
import cat.lechuga.tsmql.MetaTable;

public class PizzaTest {

    final DataAccesFacade facade;

    public PizzaTest() {
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
            sql.runFromClasspath("sql/pizza.sql");
            facade.commit();
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }
    }

    public static class Pizza_ extends MetaTable<Pizza> {

        public final MetaColumn<Pizza, Long> idPizza = addColumn("idPizza");
        public final MetaColumn<Pizza, String> name = addColumn("name");
        public final MetaColumn<Pizza, BigDecimal> price = addColumn("price");

        public Pizza_() {
            super(Pizza.class, "pizza");
        }

        public Pizza_(String alias) {
            super(Pizza.class, alias);
        }
    }

    @Test
    public void testName() throws Exception {

        EntityManagerFactory emf = new EntityManagerFactory();
        EntityManager em = emf.buildEntityManager(facade, Pizza.class);

        System.out.println(MetaGenerator.generateMetaColumns(em.getEntityMeta(Pizza.class), 0));

        em.begin();
        try {
            assertFalse(em.existsById(Pizza.class, 10));
            assertFalse(em.existsById(Pizza.class, 11));

            Pizza romana = new Pizza();
            romana.name = "romana";
            romana.price = new BigDecimal("11.5");
            assertEquals("null:romana:11.5", romana.toString());
            em.store(romana);
            assertEquals("10:romana:11.5", romana.toString());

            assertTrue(em.existsById(Pizza.class, 10));
            assertFalse(em.existsById(Pizza.class, 11));

            Pizza napolitana = new Pizza();
            napolitana.name = "napolitana";
            napolitana.price = new BigDecimal("13.5");
            assertEquals("null:napolitana:13.5", napolitana.toString());
            em.store(napolitana);
            assertEquals("11:napolitana:13.5", napolitana.toString());

            assertTrue(em.existsById(Pizza.class, 10));
            assertTrue(em.existsById(Pizza.class, 11));

            assertEquals("[10:romana:11.50, 11:napolitana:13.50]", em.loadAll(Pizza.class).toString());

            Pizza_ p = new Pizza_("p");
            double sum = em.buildTypeSafeQuery() //
                    .addAlias(p) //
                    .append("select sum({}) ", p.price) //
                    .append("from {}", p) //
                    .getExecutor(ScalarMappers.DOUBLE) //
                    .loadUnique() //
            ;
            assertEquals(25.0, sum, 0.00001);

            em.commit();
        } catch (Exception e) {
            em.rollback();
            throw new RuntimeException(e);
        }
    }

}
