package cat.lechuga.jdbc.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;
import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.JdbcDataAccesFacade;
import org.lenteja.jdbc.script.SqlScriptExecutor;

import cat.lechuga.EntityManager;
import cat.lechuga.EntityManagerFactory;
import cat.lechuga.repository.Repository;
import cat.lechuga.tsmql.ELike;
import cat.lechuga.tsmql.MetaColumn;
import cat.lechuga.tsmql.MetaGenerator;
import cat.lechuga.tsmql.MetaTable;
import cat.lechuga.tsmql.Restrictions;
import cat.lechuga.tsmql.TOrders;
import cat.lechuga.tsmql.TOrders.TOrder;

public class XYZRepositoryTest {

    final DataAccesFacade facade;

    public XYZRepositoryTest() {
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

    // TODO fer @ElementCollection

    @Test
    public void testName() throws Exception {

        EntityManagerFactory emf = new EntityManagerFactory();
        System.out.println(MetaGenerator.generateMetaColumns(emf.buildEntityMeta(Pizza.class), 0));
        EntityManager em = emf.buildEntityManager(facade, Pizza.class);

        Pizza_ p = new Pizza_();
        Repository<Pizza, Long, Pizza_> repoPizza = new Repository<>(em, p);

        em.begin();
        try {

            Pizza romana = new Pizza();
            romana.name = "romana";
            romana.price = new BigDecimal("11.5");

            Pizza napolitana = new Pizza();
            napolitana.name = "napolitana";
            napolitana.price = new BigDecimal("13.5");

            assertEquals("null:romana:11.5", romana.toString());
            assertEquals("null:napolitana:13.5", napolitana.toString());

            assertFalse(repoPizza.exists(romana));
            assertFalse(repoPizza.existsById(napolitana.idPizza));

            repoPizza.saveAll(Arrays.asList(romana, napolitana));

            assertEquals("10:romana:11.5", romana.toString());
            assertEquals("11:napolitana:13.5", napolitana.toString());

            assertTrue(repoPizza.exists(romana));
            assertTrue(repoPizza.existsById(napolitana.idPizza));

            romana = repoPizza.findById(romana.idPizza).orElseThrow(() -> new RuntimeException());

            assertEquals("10:romana:11.50", romana.toString());

            romana = repoPizza.findUniqueBy(k -> k.name.ilike(ELike.CONTAINS, "oma"))
                    .orElseThrow(() -> new RuntimeException());

            assertEquals("10:romana:11.50", romana.toString());

            List<Pizza> all = repoPizza.findBy(k -> Restrictions.and( //
                    k.idPizza.gt(0L), //
                    k.name.ilike(ELike.CONTAINS, "o") //
            ), TOrders.by(TOrder.asc(p.idPizza)));

            assertEquals("[10:romana:11.50, 11:napolitana:13.50]", all.toString());

            repoPizza.delete(romana);
            repoPizza.deleteById(napolitana.idPizza);

            assertFalse(repoPizza.exists(romana));
            assertFalse(repoPizza.existsById(napolitana.idPizza));

            assertEquals("[]", repoPizza.findAll().toString());

            em.commit();
        } catch (Exception e) {
            em.rollback();
            throw new RuntimeException(e);
        }
    }

}
