package org.lenteja.test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;
import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.JdbcDataAccesFacade;
import org.lenteja.jdbc.script.SqlScriptExecutor;
import org.lenteja.mapper.Column;
import org.lenteja.mapper.EntityManager;
import org.lenteja.mapper.Table;
import org.lenteja.mapper.query.Order;
import org.lenteja.mapper.query.Relational;

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
            sql.runFromClasspath("films.sql");
            facade.commit();
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }
    }

    @Test
    public void test() throws Exception {

        facade.begin();
        try {

            EntityManager entityManager = new EntityManager(facade);
            ValoresTable vt = new ValoresTable();

            entityManager.storeAll(vt, //
                    Arrays.asList( //
                            new Valores("1", "on"), //
                            new Valores("2", "tw"), //
                            new Valores("3", "thre") //
                    ) //
            );
            entityManager.storeAll(vt, //
                    Arrays.asList( //
                            new Valores("1", "one"), //
                            new Valores("2", "two"), //
                            new Valores("3", "three") //
                    ) //
            );

            List<Valores> all = entityManager.query(vt, Relational.all(), Order.by(Order.asc(vt.key)));
            assertEquals("[Valores [key=1, val=one], Valores [key=2, val=two], Valores [key=3, val=three]]",
                    all.toString());

            facade.commit();
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }
    }

    public static class ValoresTable extends Table<Valores> {

        public final Column<Valores, String> key = addPkColumn(String.class, "key");
        public final Column<Valores, String> val = addColumn(String.class, "val");

        public ValoresTable(String alias) {
            super(Valores.class, "valores", alias);
        }

        public ValoresTable() {
            this(null);
        }
    }

    public static class Valores {

        String key;
        String val;

        public Valores() {
            super();
        }

        public Valores(String key, String val) {
            super();
            this.key = key;
            this.val = val;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getVal() {
            return val;
        }

        public void setVal(String val) {
            this.val = val;
        }

        @Override
        public String toString() {
            return "Valores [key=" + key + ", val=" + val + "]";
        }

    }
}
