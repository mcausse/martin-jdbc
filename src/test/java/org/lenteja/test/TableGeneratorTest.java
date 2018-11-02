package org.lenteja.test;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;
import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.JdbcDataAccesFacade;
import org.lenteja.jdbc.script.SqlScriptExecutor;
import org.lenteja.mapper.TableGenerator;

public class TableGeneratorTest {

    final DataAccesFacade facade;

    public TableGeneratorTest() {
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
    public void testName() throws Exception {

        // final TableMeta meta;
        //
        // facade.begin();
        // try {
        // QueryObject q = new QueryObject("select * from pizzas");
        // meta = facade.extract(q, new MetaTableExtractor(facade));
        // } finally {
        // facade.rollback();
        // }

        TableGenerator g = new TableGenerator(facade);
        System.out.println(g.generate("Pizza", "pizzas"));
    }

}
