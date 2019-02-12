package cat.lechuga.jdbc.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;
import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.JdbcDataAccesFacade;
import org.lenteja.jdbc.script.SqlScriptExecutor;

import cat.lechuga.EntityManager;
import cat.lechuga.EntityManagerFactory;
import cat.lechuga.EntityMeta;
import cat.lechuga.anno.Id;
import cat.lechuga.generator.ScalarMappers;
import cat.lechuga.reflect.anno.Embeddable;
import cat.lechuga.tsmql.MetaColumn;
import cat.lechuga.tsmql.MetaGenerator;
import cat.lechuga.tsmql.MetaTable;
import cat.lechuga.tsmql.TOrders;
import cat.lechuga.tsmql.TOrders.TOrder;

public class TablaValoresTest {

    final DataAccesFacade facade;

    public TablaValoresTest() {
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
            sql.runFromClasspath("sql/tablavalores.sql");
            facade.commit();
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }
    }

    @Test
    public void testGen() throws Exception {

        Class<?>[] cs = { TablaValores.class };

        EntityManagerFactory emf = new EntityManagerFactory();

        int uniqueIndex = 0;
        for (Class<?> c : cs) {
            EntityMeta<?> meta = emf.buildEntityMeta(c);
            System.out.println(MetaGenerator.generateMetaColumns(meta, uniqueIndex++));
        }
    }

    @Test
    public void testName() throws Exception {

        EntityManagerFactory emf = new EntityManagerFactory();
        EntityManager em = emf.buildEntityManager(facade, TablaValores.class);

        TablaValoresId id1 = new TablaValoresId();
        TablaValores v1 = new TablaValores();

        facade.begin();
        try {

            v1.id = id1;

            v1.id.name = "CP";
            v1.id.code = "08208";
            v1.value = "S";
            em.store(v1);

            v1.value = "SBD";
            assertEquals("TablaValores [id=TablaValoresId [name=CP, code=08208], value=SBD]", v1.toString());
            em.store(v1);
            assertEquals("TablaValores [id=TablaValoresId [name=CP, code=08208], value=SBD]", v1.toString());

            v1 = em.loadById(TablaValores.class, id1);
            assertEquals("TablaValores [id=TablaValoresId [name=CP, code=08208], value=SBD]", v1.toString());

            facade.commit();
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }

        facade.begin();
        try {

            assertTrue(em.exists(v1));
            assertTrue(em.existsById(TablaValores.class, id1));

            {
                TablaValores r = em.buildQuery() //
                        .addAlias("t", TablaValores.class) //
                        .append("select {t.*} from {t.#} ") //
                        .append("where {t.id.name=?} and {t.id.code=?} ", "CP", "08208") //
                        .append("order by {t.value asc} ") //
                        .getExecutor(TablaValores.class) //
                        .loadFirst() //
                ;
                assertEquals("TablaValores [id=TablaValoresId [name=CP, code=08208], value=SBD]", r.toString());
            }
            {
                TablaValores_ t = new TablaValores_();
                TablaValores r = em.buildTypeSafeQuery() //
                        .addAlias(t) //
                        .append("select {} from {} ", t.all(), t) //
                        .append("where {} and {} ", t.name.eq("CP"), t.code.eq("08208")) //
                        .append("order by {}", TOrders.by(TOrder.asc(t.value))) //
                        .getExecutor(TablaValores.class) //
                        .loadUnique() //
                ;
                assertEquals("TablaValores [id=TablaValoresId [name=CP, code=08208], value=SBD]", r.toString());
            }
            {
                long count = em.buildQuery() //
                        .addAlias("t", TablaValores.class) //
                        .append("select count(*) from {t.#} ") //
                        .getExecutor(ScalarMappers.LONG) //
                        .loadUnique() //
                ;
                assertEquals(1L, count);
            }
            {
                TablaValores_ t = new TablaValores_();
                long count = em.buildTypeSafeQuery() //
                        .addAlias(t) //
                        .append("select count(*) from {}", t) //
                        .getExecutor(ScalarMappers.LONG) //
                        .loadUnique() //
                ;
                assertEquals(1L, count);
            }

            em.delete(v1);
            assertEquals("[]", em.loadAll(TablaValores.class).toString());

            assertFalse(em.exists(v1));
            assertFalse(em.existsById(TablaValores.class, id1));

            facade.commit();
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }
    }

    @Embeddable
    public static class TablaValoresId {

        public String name;
        public String code;

        @Override
        public String toString() {
            return "TablaValoresId [name=" + name + ", code=" + code + "]";
        }
    }

    public static class TablaValores {

        @Id
        public TablaValoresId id;
        public String value;

        @Override
        public String toString() {
            return "TablaValores [id=" + id + ", value=" + value + "]";
        }
    }

    public static class TablaValores_ extends MetaTable<TablaValores> {

        public final MetaColumn<TablaValores, String> name = addColumn("id.name");
        public final MetaColumn<TablaValores, String> code = addColumn("id.code");
        public final MetaColumn<TablaValores, String> value = addColumn("value");

        public TablaValores_() {
            super(TablaValores.class, "tablav0");
        }

        public TablaValores_(String alias) {
            super(TablaValores.class, alias);
        }
    }

}
