package org.lenteja.test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;
import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.JdbcDataAccesFacade;
import org.lenteja.jdbc.script.SqlScriptExecutor;
import org.lenteja.mapper.Column;
import org.lenteja.mapper.EntityManager;
import org.lenteja.mapper.Table;
import org.lenteja.mapper.autogen.impl.HsqldbIdentity;
import org.lenteja.mapper.collabs.JoinColumn;
import org.lenteja.mapper.collabs.ManyToMany;
import org.lenteja.mapper.collabs.ManyToOne;
import org.lenteja.mapper.collabs.OneToMany;
import org.lenteja.mapper.query.Order;

public class ManyToManyTest {

    final DataAccesFacade facade;

    public ManyToManyTest() {
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
            sql.runFromClasspath("manytomany.sql");
            facade.commit();
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }
    }

    public static class Pizza {

        Long idPizza;
        String name;

        public Pizza() {
            super();
        }

        public Pizza(String name) {
            super();
            this.name = name;
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

        @Override
        public String toString() {
            return "Pizza [" + idPizza + "/" + name + "]";
        }

    }

    public static class Ingredient {

        Integer idIngredient;
        String name;

        public Ingredient() {
            super();
        }

        public Ingredient(String name) {
            super();
            this.name = name;
        }

        public Integer getIdIngredient() {
            return idIngredient;
        }

        public void setIdIngredient(Integer idIngredient) {
            this.idIngredient = idIngredient;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Ingredient [" + idIngredient + "/" + name + "]";
        }

    }

    public static class PizzaIngredients {

        Long idPizza;
        Integer idIngredient;

        public PizzaIngredients() {
            super();
        }

        public PizzaIngredients(Long idPizza, Integer idIngredient) {
            super();
            this.idPizza = idPizza;
            this.idIngredient = idIngredient;
        }

        public Long getIdPizza() {
            return idPizza;
        }

        public void setIdPizza(Long idPizza) {
            this.idPizza = idPizza;
        }

        public Integer getIdIngredient() {
            return idIngredient;
        }

        public void setIdIngredient(Integer idIngredient) {
            this.idIngredient = idIngredient;
        }

        @Override
        public String toString() {
            return "PizzaIngredients [" + idPizza + "/" + idIngredient + "]";
        }

    }

    public static class Pizza_ extends Table<Pizza> {

        public final Column<Pizza, Long> idPizza = addPkColumn(Long.class, "idPizza", "id_pizza");
        public final Column<Pizza, String> name = addColumn(String.class, "name", "name");

        public Pizza_() {
            super("pizzas");
            addAutoGenerated(new HsqldbIdentity<>(idPizza));
        }
    }

    public static class Ingredient_ extends Table<Ingredient> {

        public final Column<Ingredient, Integer> idIngredient = addPkColumn(Integer.class, "idIngredient");
        public final Column<Ingredient, String> name = addColumn(String.class, "name");

        public Ingredient_() {
            super("ingredients");
            addAutoGenerated(new HsqldbIdentity<>(idIngredient));
        }

    }

    public static class PizzaIngredients_ extends Table<PizzaIngredients> {

        public final Column<PizzaIngredients, Long> idPizza = addPkColumn(Long.class, "idPizza", "id_pizza");
        public final Column<PizzaIngredients, Integer> idIngredient = addPkColumn(Integer.class, "idIngredient");

        public PizzaIngredients_(String alias) {
            super("pizza_ingredients");
        }

        public PizzaIngredients_() {
            this(null);
        }
    }

    @Test
    public void test() throws Exception {

        facade.begin();
        try {

            EntityManager entityManager = new EntityManager(facade);

            Pizza_ pizzas_ = new Pizza_();
            Ingredient_ ingredients_ = new Ingredient_();
            PizzaIngredients_ pizzaIngredients_ = new PizzaIngredients_("pi");

            Pizza margarita = new Pizza("margarita");
            Pizza romana = new Pizza("romana");
            entityManager.storeAll(pizzas_, Arrays.asList(margarita, romana));

            Ingredient tomatoe = new Ingredient("tomatoe");
            Ingredient cheese = new Ingredient("cheese");
            Ingredient olivas = new Ingredient("olivas");
            entityManager.storeAll(ingredients_, Arrays.asList(tomatoe, cheese, olivas));

            PizzaIngredients r1 = new PizzaIngredients(margarita.getIdPizza(), tomatoe.getIdIngredient());
            PizzaIngredients r2 = new PizzaIngredients(margarita.getIdPizza(), cheese.getIdIngredient());
            PizzaIngredients r3 = new PizzaIngredients(romana.getIdPizza(), tomatoe.getIdIngredient());
            PizzaIngredients r4 = new PizzaIngredients(romana.getIdPizza(), cheese.getIdIngredient());
            PizzaIngredients r5 = new PizzaIngredients(romana.getIdPizza(), olivas.getIdIngredient());
            entityManager.storeAll(pizzaIngredients_, Arrays.asList(r1, r2, r3, r4, r5));

            OneToMany<Pizza, PizzaIngredients> oneToMany = new OneToMany<>(pizzas_, pizzaIngredients_,
                    new JoinColumn<>(pizzas_.idPizza, pizzaIngredients_.idPizza));
            ManyToOne<PizzaIngredients, Ingredient> manyToOne = new ManyToOne<>(pizzaIngredients_, ingredients_,
                    new JoinColumn<>(pizzaIngredients_.idIngredient, ingredients_.idIngredient));

            ManyToMany<Pizza, PizzaIngredients, Ingredient> manyToMany = new ManyToMany<>(oneToMany, manyToOne);

            assertEquals("[Ingredient [100/tomatoe], Ingredient [101/cheese]]",
                    manyToMany.fetch(facade, margarita, Order.by(Order.asc(ingredients_.idIngredient))).toString());
            assertEquals("[Ingredient [100/tomatoe], Ingredient [101/cheese], Ingredient [102/olivas]]",
                    manyToMany.fetch(facade, romana, Order.by(Order.asc(ingredients_.idIngredient))).toString());

            assertEquals("{Pizza [100/margarita]=[Ingredient [100/tomatoe], Ingredient [101/cheese]], "
                    + "Pizza [101/romana]=[Ingredient [100/tomatoe], Ingredient [101/cheese], Ingredient [102/olivas]]}",
                    manyToMany.fetch(facade, Arrays.asList(margarita, romana),
                            Order.by(Order.asc(ingredients_.idIngredient))).toString());

            assertEquals("{Pizza [100/margarita]=[PizzaIngredients [100/100], PizzaIngredients [100/101]], "
                    + "Pizza [101/romana]=[PizzaIngredients [101/100], PizzaIngredients [101/101], PizzaIngredients [101/102]]}",
                    oneToMany.fetch(facade, Arrays.asList(margarita, romana),
                            Order.by(Order.asc(pizzaIngredients_.idIngredient))).toString());

            facade.commit();
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }
    }

}
