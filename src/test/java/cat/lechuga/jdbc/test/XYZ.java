package cat.lechuga.jdbc.test;

import java.math.BigDecimal;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;
import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.JdbcDataAccesFacade;
import org.lenteja.jdbc.script.SqlScriptExecutor;

import cat.lechuga.EntityManager;
import cat.lechuga.EntityManagerFactory;
import cat.lechuga.anno.Enumerated;
import cat.lechuga.anno.Id;
import cat.lechuga.anno.Table;
import cat.lechuga.tsmql.MetaGenerator;

public class XYZ {

    final DataAccesFacade facade;

    public XYZ() {
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
            sql.runFromClasspath("sql/xyz.sql");
            facade.commit();
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }
    }

    @Test
    public void testName() throws Exception {

        EntityManagerFactory emf = new EntityManagerFactory();
        EntityManager em = emf.buildEntityManager(facade, X.class);

        System.out.println(MetaGenerator.generateMetaColumns(em.getEntityMeta(X.class), 0));

        em.begin();
        try {

            {
                X x = new X(100, (byte) 1, (byte) 2, (short) 3, (short) 4, 5, 6, 7L, 8L, 9.1f, 9.2f, 10.1, 10.2, true,
                        false, "alo", ESex.FEMALE, new byte[] { 11, 12, 13 }, new BigDecimal("14.15"));

                em.store(x);

                x = em.loadById(X.class, 100);
            }

            {
                X x = new X(100, (byte) 1, null, (short) 3, null, 5, null, 7L, null, 9.1f, null, 10.1, null, true, null,
                        "alo", null, null, null);

                em.store(x);

                x = em.loadById(X.class, 100);
            }
            em.commit();
        } catch (Exception e) {
            em.rollback();
            throw new RuntimeException(e);
        }
    }

    @Table("xyz")
    public static class X {

        @Id
        public Integer id;

        public byte byte1;
        public Byte byte2;

        public short short1;
        public Short short2;

        public int int1;
        public Integer int2;

        public long long1;
        public Long long2;

        public float float1;
        public Float float2;

        public double double1;
        public Double double2;

        public boolean boolean1;
        public Boolean boolean2;

        public String string1;

        @Enumerated
        public ESex sex;

        public byte[] bytes1;

        public BigDecimal bigDecimal;

        public X() {
            super();
        }

        public X(Integer id, byte byte1, Byte byte2, short short1, Short short2, int int1, Integer int2, long long1,
                Long long2, float float1, Float float2, double double1, Double double2, boolean boolean1,
                Boolean boolean2, String string1, ESex sex, byte[] bytes1, BigDecimal bigDecimal) {
            super();
            this.id = id;
            this.byte1 = byte1;
            this.byte2 = byte2;
            this.short1 = short1;
            this.short2 = short2;
            this.int1 = int1;
            this.int2 = int2;
            this.long1 = long1;
            this.long2 = long2;
            this.float1 = float1;
            this.float2 = float2;
            this.double1 = double1;
            this.double2 = double2;
            this.boolean1 = boolean1;
            this.boolean2 = boolean2;
            this.string1 = string1;
            this.sex = sex;
            this.bytes1 = bytes1;
            this.bigDecimal = bigDecimal;
        }

    }

}
