package org.lenteja.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;
import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.JdbcDataAccesFacade;
import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.script.SqlScriptExecutor;
import org.lenteja.mapper.Column;
import org.lenteja.mapper.Table;
import org.lenteja.mapper.handler.EnumColumnHandler;
import org.lenteja.mapper.query.ELike;
import org.lenteja.mapper.query.Operations;
import org.lenteja.mapper.query.Query;
import org.lenteja.mapper.query.Restrictions;

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
            sql.runFromClasspath("films.sql");
            facade.commit();
        } catch (Exception e) {
            facade.rollback();
            throw e;
        }
    }

    @Test
    public void testReal() throws Exception {

        Operations o = new Operations();
        facade.begin();
        try {

            Pizza_ p = new Pizza_();

            Pizza romana = new Pizza(100L, "romana", 12.5, EPizzaType.DELUX);
            facade.update(o.insert(p, romana));

            romana.setPrice(12.9);
            assertEquals("Pizza [idPizza=100, name=romana, price=12.9, type=DELUX]", romana.toString());
            facade.update(o.update(p, romana));

            Query<Pizza> q = o.query(p).append("select * from {} where {}", p, p.id.eq(100L));

            romana = q.getExecutor(facade).loadUnique();
            assertEquals("Pizza [idPizza=100, name=romana, price=12.9, type=DELUX]", romana.toString());

            assertEquals("[Pizza [idPizza=100, name=romana, price=12.9, type=DELUX]]",
                    q.getExecutor(facade).load().toString());

            p = new Pizza_();

            Query<Double> scalar = o.query(p.price).append("select sum({}) as {} from {}", p.price, p.price, p);
            assertEquals(romana.getPrice(), scalar.getExecutor(facade).loadUnique());

            facade.update(o.delete(p, romana));
            assertEquals("[]", q.getExecutor(facade).load().toString());

            assertNull(scalar.getExecutor(facade).loadUnique());

            facade.commit();
        } catch (Exception e) {
            facade.rollback();
            throw e;
        }
    }

    @Test
    public void testName() throws Exception {

        {
            Pizza_ p_ = new Pizza_();
            assertEquals("pizzas pizzas_", p_.getAliasedName());
            assertEquals("pizzas_.price", p_.price.getAliasedName());
        }
        {
            Pizza_ p_ = new Pizza_();
            assertEquals("pizzas pizzas_", p_.getAliasedName());
            assertEquals("pizzas_.price", p_.price.getAliasedName());

            IQueryObject q = Restrictions.and( //
                    p_.id.eq(100L), //
                    p_.name.ilike(ELike.CONTAINS, "alo"), //
                    p_.price.between(5.0, 18.5), //
                    p_.type.in(EPizzaType.REGULAR, EPizzaType.DELUX) //
            );
            assertEquals(
                    "pizzas_.id_pizza=? and upper(pizzas_.name) like upper(?) and pizzas_.price between ? and ? and pizzas_.kind in (?,?) -- [100(Long), %alo%(String), 5.0(Double), 18.5(Double), REGULAR(String), DELUX(String)]",
                    q.toString());
        }
        {

            Operations o = new Operations();
            Pizza romana = new Pizza(100L, "romana", 12.5, EPizzaType.DELUX);

            assertEquals(
                    "insert into pizzas (id_pizza, name, price, kind) values (?, ?, ?, ?) -- [100(Long), romana(String), 12.5(Double), DELUX(String)]",
                    o.insert(new Pizza_(), romana).toString());
            assertEquals(
                    "update pizzas set name=?, price=?, kind=? where id_pizza=? -- [romana(String), 12.5(Double), DELUX(String), 100(Long)]",
                    o.update(new Pizza_(), romana).toString());
            assertEquals("delete from pizzas where id_pizza=? -- [100(Long)]",
                    o.delete(new Pizza_(), romana).toString());
        }
        {
            Operations o = new Operations();
            Pizza_ p = new Pizza_();

            Query<Pizza> q = o.query(p);
            q.append("select sum({}) from {} ", p.price, p);
            q.append("where {} ", Restrictions.and( //
                    p.id.lt(100L), //
                    p.name.ilike(ELike.CONTAINS, "oma"), //
                    p.type.in(EPizzaType.REGULAR, EPizzaType.DELUX) //
            ));

            assertEquals(
                    "select sum(pizzas_.price) from pizzas pizzas_ where pizzas_.id_pizza<? and upper(pizzas_.name) like upper(?) and pizzas_.kind in (?,?) "
                            + " -- [100(Long), %oma%(String), REGULAR(String), DELUX(String)]", //
                    q.toString());
        }
    }

    public static class Pizza_ extends Table<Pizza> {

        public final Column<Pizza, Long> id = addPkColumn(Long.class, "idPizza", "id_pizza");
        public final Column<Pizza, String> name = addColumn(String.class, "name", "name");
        public final Column<Pizza, Double> price = addColumn(Double.class, "price", "price");
        public final Column<Pizza, EPizzaType> type = addColumn(EPizzaType.class, "type", "kind",
                new EnumColumnHandler<>(EPizzaType.class));

        public Pizza_() {
            super("pizzas");
        }
    }

    public static enum EPizzaType {
        REGULAR, DELUX;
    }

    public static class Pizza {

        Long idPizza;
        String name;
        Double price;
        EPizzaType type;

        public Pizza() {
            super();
        }

        public Pizza(Long idPizza, String name, Double price, EPizzaType type) {
            super();
            this.idPizza = idPizza;
            this.name = name;
            this.price = price;
            this.type = type;
        }

        public Long getIdPizza() {
            return idPizza;
        }

        public void setIdPizza(Long idPizza) {
            this.idPizza = idPizza;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }

        public EPizzaType getType() {
            return type;
        }

        public void setType(EPizzaType type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "Pizza [idPizza=" + idPizza + ", name=" + name + ", price=" + price + ", type=" + type + "]";
        }
    }

}
