package cat.lechuga.jdbc.test;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

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
import cat.lechuga.tsmql.MetaGenerator;

public class XYZTest {

    final DataAccesFacade facade;

    public XYZTest() {
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
        EntityManager em = emf.buildEntityManager(facade, Xyz.class);

        System.out.println(MetaGenerator.generateMetaColumns(em.getEntityMeta(Xyz.class), 0));

        em.begin();
        try {

            {
                Xyz x = new Xyz(100, (byte) 1, (byte) 2, (short) 3, (short) 4, 5, 6, 7L, 8L, 9.1f, 9.2f, 10.1, 10.2,
                        true, false, "alo", ESex.FEMALE, new byte[] { 11, 12, 13 }, new BigDecimal("14.15"),
                        new Date(0L));

                assertEquals(
                        "X [id=100, byte1=1, byte2=2, short1=3, short2=4, int1=5, int2=6, long1=7, long2=8, float1=9.1, float2=9.2, double1=10.1, double2=10.2, boolean1=true, boolean2=false, string1=alo, sex=FEMALE, bytes1=[11, 12, 13], bigDecimal=14.15, date1=01/01/1970 01:00:00]",
                        x.toString());

                em.store(x);

                assertEquals(
                        "X [id=100, byte1=1, byte2=2, short1=3, short2=4, int1=5, int2=6, long1=7, long2=8, float1=9.1, float2=9.2, double1=10.1, double2=10.2, boolean1=true, boolean2=false, string1=alo, sex=FEMALE, bytes1=[11, 12, 13], bigDecimal=14.15, date1=01/01/1970 01:00:00]",
                        x.toString());

                x = em.loadById(Xyz.class, 100);

                assertEquals(
                        "X [id=100, byte1=1, byte2=2, short1=3, short2=4, int1=5, int2=6, long1=7, long2=8, float1=9.1, float2=9.2, double1=10.1, double2=10.2, boolean1=true, boolean2=false, string1=alo, sex=FEMALE, bytes1=[11, 12, 13], bigDecimal=14.15, date1=01/01/1970 01:00:00]",
                        x.toString());
            }

            {
                Xyz x = new Xyz(100, (byte) 1, null, (short) 3, null, 5, null, 7L, null, 9.1f, null, 10.1, null, true,
                        null, "alo", null, null, null, null);

                assertEquals(
                        "X [id=100, byte1=1, byte2=null, short1=3, short2=null, int1=5, int2=null, long1=7, long2=null, float1=9.1, float2=null, double1=10.1, double2=null, boolean1=true, boolean2=null, string1=alo, sex=null, bytes1=null, bigDecimal=null, date1=null]",
                        x.toString());

                em.store(x);

                assertEquals(
                        "X [id=100, byte1=1, byte2=null, short1=3, short2=null, int1=5, int2=null, long1=7, long2=null, float1=9.1, float2=null, double1=10.1, double2=null, boolean1=true, boolean2=null, string1=alo, sex=null, bytes1=null, bigDecimal=null, date1=null]",
                        x.toString());

                x = em.loadById(Xyz.class, 100);

                assertEquals(
                        "X [id=100, byte1=1, byte2=null, short1=3, short2=null, int1=5, int2=null, long1=7, long2=null, float1=9.1, float2=null, double1=10.1, double2=null, boolean1=true, boolean2=null, string1=alo, sex=null, bytes1=null, bigDecimal=null, date1=null]",
                        x.toString());
            }
            em.commit();
        } catch (Exception e) {
            em.rollback();
            throw new RuntimeException(e);
        }
    }

    public static class Xyz {

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

        public Date date1;

        public Xyz() {
            super();
        }

        public Xyz(Integer id, byte byte1, Byte byte2, short short1, Short short2, int int1, Integer int2, long long1,
                Long long2, float float1, Float float2, double double1, Double double2, boolean boolean1,
                Boolean boolean2, String string1, ESex sex, byte[] bytes1, BigDecimal bigDecimal, Date date1) {
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
            this.date1 = date1;
        }

        @Override
        public String toString() {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String datef = date1 == null ? null : sdf.format(date1);
            return "X [id=" + id + ", byte1=" + byte1 + ", byte2=" + byte2 + ", short1=" + short1 + ", short2=" + short2
                    + ", int1=" + int1 + ", int2=" + int2 + ", long1=" + long1 + ", long2=" + long2 + ", float1="
                    + float1 + ", float2=" + float2 + ", double1=" + double1 + ", double2=" + double2 + ", boolean1="
                    + boolean1 + ", boolean2=" + boolean2 + ", string1=" + string1 + ", sex=" + sex + ", bytes1="
                    + Arrays.toString(bytes1) + ", bigDecimal=" + bigDecimal + ", date1=" + datef + "]";
        }

    }

}
