package org.lenteja.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;
import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.JdbcDataAccesFacade;
import org.lenteja.jdbc.script.SqlScriptExecutor;
import org.lenteja.mapper.Column;
import org.lenteja.mapper.GenericDao;
import org.lenteja.mapper.Table;
import org.lenteja.mapper.TableGenerator;
import org.lenteja.mapper.autogen.impl.HsqldbIdentity;
import org.lenteja.mapper.collabs.EntitiesLazyList;
import org.lenteja.mapper.collabs.JoinColumn;
import org.lenteja.mapper.collabs.ManyToOne;
import org.lenteja.mapper.collabs.OneToMany;
import org.lenteja.mapper.collabs.OneToMany.StoreOrphansStrategy;
import org.lenteja.mapper.handler.StringDateHandler;
import org.lenteja.mapper.query.Order;
import org.lenteja.mapper.query.Restrictions;

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
            sql.runFromClasspath("exp.sql");
            facade.commit();
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }
    }

    @Test
    public void testCodeGen() throws Exception {

        TableGenerator g = new TableGenerator(facade);
        System.out.println(g.generate("Exp", "exps"));
        System.out.println(g.generate("Tex", "tex"));
    }

    @Test
    public void testName() throws Exception {

        ExpDao expDao = new ExpDao(facade);
        TexDao texDao = new TexDao(facade);

        facade.begin();
        try {

            ExpId expId = new ExpId(8200L, 2018, null);
            Exp exp = new Exp(expId, "exp1", "20181111");
            expDao.store(exp);

            Tex fase1 = new Tex(null, expId, "fase1");
            Tex fase2 = new Tex(null, expId, "fase2");
            texDao.storeAll(Arrays.asList(fase1, fase2));

            {
                ExpId expId2 = new ExpId(8200L, 2018, null);
                Exp exp2 = new Exp(expId2, "exp2", "20181111");
                expDao.store(exp2);

                Tex fase12 = new Tex(null, expId2, "fase12");
                Tex fase22 = new Tex(null, expId2, "fase22");
                texDao.storeAll(Arrays.asList(fase12, fase22));

            }

            assertEquals(
                    "[Tex [idTex=100, expId=ExpId [idEns=8200, anyExp=2018, numExp=10], faseName=fase1], "
                            + "Tex [idTex=101, expId=ExpId [idEns=8200, anyExp=2018, numExp=10], faseName=fase2]]",
                    expDao.getFases(exp).toString());
            assertEquals("Exp [id=ExpId [idEns=8200, anyExp=2018, numExp=10], name=exp1, fecIni=20181111]",
                    texDao.getExpedient(fase1).toString());

            // XXX test de OneToMany lazy !!!
            {
                List<Tex> fases = expDao.getFasesLaziely(exp);
                assertFalse(((EntitiesLazyList<?>) fases).isInitializated());
                assertEquals(
                        "[Tex [idTex=100, expId=ExpId [idEns=8200, anyExp=2018, numExp=10], faseName=fase1], "
                                + "Tex [idTex=101, expId=ExpId [idEns=8200, anyExp=2018, numExp=10], faseName=fase2]]",
                        fases.toString());
                assertTrue(((EntitiesLazyList<?>) fases).isInitializated());
            }

            // XXX test de OneToMany store !!!!!!!!!!!!
            {
                List<Tex> fases = expDao.getFasesLaziely(exp);
                fases.add(new Tex(null, null, "fase jou"));
                fases.remove(0);

                int orphansRemoved = expDao.oneToMany.storeChilds(facade, exp, fases, StoreOrphansStrategy.NULL);

                assertEquals(1, orphansRemoved);

                assertEquals("[Tex [idTex=101, expId=ExpId [idEns=8200, anyExp=2018, numExp=10], faseName=fase2], "
                        + "Tex [idTex=104, expId=ExpId [idEns=8200, anyExp=2018, numExp=10], faseName=fase jou]]",
                        expDao.getFases(exp).toString());

            }

            facade.commit();
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }
    }

    @Test
    public void testOneToManyOrphansDELETE() throws Exception {

        ExpDao expDao = new ExpDao(facade);
        TexDao texDao = new TexDao(facade);

        facade.begin();
        try {

            ExpId expId = new ExpId(8200L, 2018, null);
            Exp exp = new Exp(expId, "exp1", "20181111");
            expDao.store(exp);

            Tex fase1 = new Tex(null, null, "fase1");
            Tex fase2 = new Tex(null, null, "fase2");
            expDao.oneToMany.storeChilds(facade, exp, Arrays.asList(fase1, fase2));

            {
                ExpId expId2 = new ExpId(8200L, 2018, null);
                Exp exp2 = new Exp(expId2, "exp2", "20181111");
                expDao.store(exp2);

                Tex fase12 = new Tex(null, null, "fase12");
                Tex fase22 = new Tex(null, null, "fase22");
                expDao.oneToMany.storeChilds(facade, exp2, Arrays.asList(fase12, fase22));
            }

            // XXX test de OneToMany store !!!!!!!!!!!!
            {
                List<Tex> fases = expDao.getFasesLaziely(exp);
                fases.add(new Tex(null, null, "fase jou"));
                fases.remove(0);

                int orphansRemoved = expDao.oneToMany.storeChilds(facade, exp, fases, StoreOrphansStrategy.DELETE);

                assertEquals(1, orphansRemoved);

                assertEquals("[Tex [idTex=101, expId=ExpId [idEns=8200, anyExp=2018, numExp=10], faseName=fase2], "
                        + "Tex [idTex=104, expId=ExpId [idEns=8200, anyExp=2018, numExp=10], faseName=fase jou]]",
                        expDao.getFases(exp).toString());

                assertEquals( //
                        "[Tex [idTex=101, expId=ExpId [idEns=8200, anyExp=2018, numExp=10], faseName=fase2], " //
                                + "Tex [idTex=102, expId=ExpId [idEns=8200, anyExp=2018, numExp=11], faseName=fase12], " //
                                + "Tex [idTex=103, expId=ExpId [idEns=8200, anyExp=2018, numExp=11], faseName=fase22], " //
                                + "Tex [idTex=104, expId=ExpId [idEns=8200, anyExp=2018, numExp=10], faseName=fase jou]]", //
                        texDao.query(Restrictions.all(), Order.by(Order.asc(TexDao.TABLE.idTex))).toString());

                int removed = expDao.oneToMany.deleteChilds(facade, exp);
                assertEquals(2, removed);

                assertEquals( //
                        "[Tex [idTex=102, expId=ExpId [idEns=8200, anyExp=2018, numExp=11], faseName=fase12], " //
                                + "Tex [idTex=103, expId=ExpId [idEns=8200, anyExp=2018, numExp=11], faseName=fase22]]" //
                        , texDao.query(Restrictions.all(), Order.by(Order.asc(TexDao.TABLE.idTex))).toString());

            }

            facade.commit();
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }
    }

    @Test
    public void testOneToManyOrphansNULL() throws Exception {

        ExpDao expDao = new ExpDao(facade);
        TexDao texDao = new TexDao(facade);

        facade.begin();
        try {

            ExpId expId = new ExpId(8200L, 2018, null);
            Exp exp = new Exp(expId, "exp1", "20181111");
            expDao.store(exp);
            expDao.refresh(exp); // <= per exemple

            Tex fase1 = new Tex(null, null, "fase1");
            Tex fase2 = new Tex(null, null, "fase2");
            expDao.oneToMany.storeChilds(facade, exp, Arrays.asList(fase1, fase2));

            {
                ExpId expId2 = new ExpId(8200L, 2018, null);
                Exp exp2 = new Exp(expId2, "exp2", "20181111");
                expDao.store(exp2);

                Tex fase12 = new Tex(null, null, "fase12");
                Tex fase22 = new Tex(null, null, "fase22");
                expDao.oneToMany.storeChilds(facade, exp2, Arrays.asList(fase12, fase22));
            }

            // XXX test de OneToMany store !!!!!!!!!!!!
            {
                List<Tex> fases = expDao.getFasesLaziely(exp);
                Tex novaFase = new Tex(null, null, "fase jou");
                fases.add(novaFase);
                fases.remove(0);

                assertEquals("Tex [idTex=null, expId=null, faseName=fase jou]", novaFase.toString());

                int orphansRemoved = expDao.oneToMany.storeChilds(facade, exp, fases, StoreOrphansStrategy.NULL);

                assertEquals("Tex [idTex=104, expId=ExpId [idEns=8200, anyExp=2018, numExp=10], faseName=fase jou]",
                        novaFase.toString());
                texDao.refresh(novaFase);
                assertEquals("Tex [idTex=104, expId=ExpId [idEns=8200, anyExp=2018, numExp=10], faseName=fase jou]",
                        novaFase.toString());

                assertEquals(1, orphansRemoved);

                assertEquals("[Tex [idTex=101, expId=ExpId [idEns=8200, anyExp=2018, numExp=10], faseName=fase2], "
                        + "Tex [idTex=104, expId=ExpId [idEns=8200, anyExp=2018, numExp=10], faseName=fase jou]]",
                        expDao.getFases(exp).toString());

                assertEquals( //
                        "[Tex [idTex=100, expId=ExpId [idEns=null, anyExp=null, numExp=null], faseName=fase1], " //
                                + "Tex [idTex=101, expId=ExpId [idEns=8200, anyExp=2018, numExp=10], faseName=fase2], " //
                                + "Tex [idTex=102, expId=ExpId [idEns=8200, anyExp=2018, numExp=11], faseName=fase12], " //
                                + "Tex [idTex=103, expId=ExpId [idEns=8200, anyExp=2018, numExp=11], faseName=fase22], " //
                                + "Tex [idTex=104, expId=ExpId [idEns=8200, anyExp=2018, numExp=10], faseName=fase jou]]" //
                        , texDao.query(Restrictions.all(), Order.by(Order.asc(TexDao.TABLE.idTex))).toString());
            }

            facade.commit();
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }
    }

    @Test
    public void testManyToOne() throws Exception {

        TexDao texDao = new TexDao(facade);

        facade.begin();
        try {

            ExpId expId = new ExpId(8200L, 2018, null);
            Exp exp = new Exp(expId, "exp1", "20181111");

            Tex fase1 = new Tex(null, null, "fase1");
            Tex fase2 = new Tex(null, null, "fase2");
            texDao.manyToOne.storeChildAndParent(facade, fase1, exp);
            texDao.manyToOne.storeChildAndParent(facade, fase2, exp, false /* ja esta guardat */);

            ExpId expId2 = new ExpId(8200L, 2018, null);
            Exp exp2 = new Exp(expId2, "exp2", "20181111");

            Tex fase12 = new Tex(null, null, "fase12");
            Tex fase22 = new Tex(null, null, "fase22");
            texDao.manyToOne.storeChildAndParent(facade, fase12, exp2);
            texDao.manyToOne.storeChildAndParent(facade, fase22, exp2, false /* ja esta guardat */);

            //////////

            Exp e = texDao.manyToOne.fetch(facade, fase1);
            assertEquals("Exp [id=ExpId [idEns=8200, anyExp=2018, numExp=10], name=exp1, fecIni=20181111]",
                    e.toString());

            texDao.manyToOne.storeChildAndParent(facade, fase1, null);

            e = texDao.manyToOne.fetch(facade, fase1);
            assertNull(e);

            texDao.refresh(fase1);

            e = texDao.manyToOne.fetch(facade, fase1);
            assertNull(e);

            // assigna a l'altre expedient

            texDao.manyToOne.storeChildAndParent(facade, fase1, exp2);

            texDao.refresh(fase1);

            e = texDao.manyToOne.fetch(facade, fase1);
            assertEquals("Exp [id=ExpId [idEns=8200, anyExp=2018, numExp=11], name=exp2, fecIni=20181111]",
                    e.toString());

            facade.commit();
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }
    }

    public static class ExpDao extends GenericDao<Exp, ExpId> {

        public static final ExpTable TABLE = new ExpTable("exp");

        final OneToMany<Exp, Tex> oneToMany = new OneToMany<>(TABLE, TexDao.TABLE,
                new JoinColumn<>(TABLE.idEns, TexDao.TABLE.idEns), //
                new JoinColumn<>(TABLE.anyExp, TexDao.TABLE.anyExp), //
                new JoinColumn<>(TABLE.numExp, TexDao.TABLE.numExp) //
        );

        public ExpDao(DataAccesFacade facade) {
            super(facade, TABLE);
        }

        public List<Tex> getFases(Exp exp) {
            return oneToMany.fetch(getFacade(), exp, //
                    Order.by( //
                            Order.asc(TexDao.TABLE.idEns), //
                            Order.asc(TexDao.TABLE.anyExp), //
                            Order.asc(TexDao.TABLE.numExp) //
                    ) //
            );
        }

        public List<Tex> getFasesLaziely(Exp exp) {
            return oneToMany.fetchLazy(getFacade(), exp, //
                    Order.by( //
                            Order.asc(TexDao.TABLE.idEns), //
                            Order.asc(TexDao.TABLE.anyExp), //
                            Order.asc(TexDao.TABLE.numExp) //
                    ) //
            );
        }

    }

    public static class TexDao extends GenericDao<Tex, Long> {

        public static final TexTable TABLE = new TexTable("tex");

        final ManyToOne<Tex, Exp> manyToOne = new ManyToOne<>(TABLE, ExpDao.TABLE,
                new JoinColumn<>(TABLE.idEns, ExpDao.TABLE.idEns), //
                new JoinColumn<>(TABLE.anyExp, ExpDao.TABLE.anyExp), //
                new JoinColumn<>(TABLE.numExp, ExpDao.TABLE.numExp) //
        );

        public TexDao(DataAccesFacade facade) {
            super(facade, TABLE);
        }

        public Exp getExpedient(Tex fase) {
            return manyToOne.fetch(getFacade(), fase);
        }
    }

    public static class ExpTable extends Table<Exp> {

        public final Column<Exp, Long> idEns = addPkColumn(Long.class, "id.idEns", "ID_ENS");
        public final Column<Exp, Integer> anyExp = addPkColumn(Integer.class, "id.anyExp", "ANY_EXP");
        public final Column<Exp, Long> numExp = addPkColumn(Long.class, "id.numExp", "NUM_EXP");
        public final Column<Exp, String> name = addColumn(String.class, "name", "NAME");
        public final Column<Exp, String> fecIni = addColumn(String.class, "fecIni", "FECHA_INI",
                new StringDateHandler());

        public ExpTable(String alias) {
            super("EXPS", alias);
            addAutoGenerated(new HsqldbIdentity<>(numExp));
        }

        public ExpTable(String tableName, String alias) {
            this(null);
        }

    }

    public static class TexTable extends Table<Tex> {

        public final Column<Tex, Long> idTex = addPkColumn(Long.class, "idTex", "ID_TEX");
        public final Column<Tex, Long> idEns = addColumn(Long.class, "expId.idEns", "ID_ENS");
        public final Column<Tex, Integer> anyExp = addColumn(Integer.class, "expId.anyExp", "ANY_EXP");
        public final Column<Tex, Long> numExp = addColumn(Long.class, "expId.numExp", "NUM_EXP");
        public final Column<Tex, String> faseName = addColumn(String.class, "faseName", "FASE_NAME");

        public TexTable(String alias) {
            super("TEX", alias);
            addAutoGenerated(new HsqldbIdentity<>(idTex));
        }

        public TexTable(String tableName, String alias) {
            this(null);
        }

    }

    public static class ExpId {

        Long idEns;
        Integer anyExp;
        Long numExp;

        public ExpId() {
            super();
        }

        public ExpId(Long idEns, Integer anyExp, Long numExp) {
            super();
            this.idEns = idEns;
            this.anyExp = anyExp;
            this.numExp = numExp;
        }

        public Long getIdEns() {
            return idEns;
        }

        public void setIdEns(Long idEns) {
            this.idEns = idEns;
        }

        public Integer getAnyExp() {
            return anyExp;
        }

        public void setAnyExp(Integer anyExp) {
            this.anyExp = anyExp;
        }

        public Long getNumExp() {
            return numExp;
        }

        public void setNumExp(Long numExp) {
            this.numExp = numExp;
        }

        @Override
        public String toString() {
            return "ExpId [idEns=" + idEns + ", anyExp=" + anyExp + ", numExp=" + numExp + "]";
        }
    }

    public static class Exp {

        ExpId id;
        String name;
        String fecIni;

        public Exp() {
            super();
        }

        public Exp(ExpId id, String name, String fecIni) {
            super();
            this.id = id;
            this.name = name;
            this.fecIni = fecIni;
        }

        public ExpId getId() {
            return id;
        }

        public void setId(ExpId id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFecIni() {
            return fecIni;
        }

        public void setFecIni(String fecIni) {
            this.fecIni = fecIni;
        }

        @Override
        public String toString() {
            return "Exp [id=" + id + ", name=" + name + ", fecIni=" + fecIni + "]";
        }
    }

    public static class Tex {

        Long idTex;
        ExpId expId;
        String faseName;

        public Tex() {
            super();
        }

        public Tex(Long idTex, ExpId expId, String faseName) {
            super();
            this.idTex = idTex;
            this.expId = expId;
            this.faseName = faseName;
        }

        public Long getIdTex() {
            return idTex;
        }

        public void setIdTex(Long idTex) {
            this.idTex = idTex;
        }

        public ExpId getExpId() {
            return expId;
        }

        public void setExpId(ExpId expId) {
            this.expId = expId;
        }

        public String getFaseName() {
            return faseName;
        }

        public void setFaseName(String faseName) {
            this.faseName = faseName;
        }

        @Override
        public String toString() {
            return "Tex [idTex=" + idTex + ", expId=" + expId + ", faseName=" + faseName + "]";
        }

    }

}
