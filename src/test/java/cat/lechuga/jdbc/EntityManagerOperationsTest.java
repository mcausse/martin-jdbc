package cat.lechuga.jdbc;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Test;

import cat.lechuga.EntityManagerFactory;
import cat.lechuga.EntityManagerOperations;
import cat.lechuga.EntityMeta;
import cat.lechuga.jdbc.test.ESex;
import cat.lechuga.jdbc.test.Exp;
import cat.lechuga.jdbc.test.ExpId;
import cat.lechuga.jdbc.test.Pizza;

public class EntityManagerOperationsTest {

    @Test
    public void testExpExpId() throws Exception {

        EntityManagerFactory emf = new EntityManagerFactory();
        EntityMeta<Exp> entityMeta = emf.buildEntityMeta(Exp.class);
        EntityManagerOperations emo = new EntityManagerOperations();

        ExpId id = new ExpId();
        Exp exp = new Exp();

        exp.setId(id);
        exp.getId().setIdEns(8L);
        exp.getId().anyExp = 1982;
        exp.getId().setNumExp(123L);
        exp.setAlive(true);
        exp.setFecIni(new Date(0L));
        exp.setName("jou");
        exp.sex = ESex.FEMALE;

        assertEquals(
                "update exps set name=?,fecha_ini=?,sex=?,alive=? where id_ens=? and any_exp=? and num_exp=? -- [jou(String), 01-01-1970(String), FEMALE(String), true(Boolean), 8(Long), 1982(Integer), 123(Long)]",
                emo.update(entityMeta, exp).toString());
        assertEquals(
                "select id_ens,any_exp,num_exp,name,fecha_ini,sex,alive from exps where id_ens=? and any_exp=? and num_exp=? -- [8(Long), 1982(Integer), 123(Long)]",
                emo.loadById(entityMeta, id).toString());
        assertEquals(
                "insert into exps (id_ens,any_exp,num_exp,name,fecha_ini,sex,alive) values (?,?,?,?,?,?,?) -- [8(Long), 1982(Integer), 123(Long), jou(String), 01-01-1970(String), FEMALE(String), true(Boolean)]",
                emo.insert(entityMeta, exp).toString());
        assertEquals(
                "delete from exps where id_ens=? and any_exp=? and num_exp=? -- [8(Long), 1982(Integer), 123(Long)]",
                emo.delete(entityMeta, exp).toString());
        assertEquals(
                "select count(*) from exps where id_ens=? and any_exp=? and num_exp=? -- [8(Long), 1982(Integer), 123(Long)]",
                emo.existsById(entityMeta, id).toString());
        assertEquals(
                "select count(*) from exps where id_ens=? and any_exp=? and num_exp=? -- [8(Long), 1982(Integer), 123(Long)]",
                emo.exists(entityMeta, exp).toString());
    }

    @Test
    public void testPizza() throws Exception {

        EntityManagerFactory emf = new EntityManagerFactory();
        EntityMeta<Pizza> entityMeta = emf.buildEntityMeta(Pizza.class);
        EntityManagerOperations emo = new EntityManagerOperations();

        Pizza pizza = new Pizza();
        pizza.idPizza = 42L;
        pizza.name = "romana";
        pizza.price = new BigDecimal("12.34");

        assertEquals(
                "update pizza set name=?,price=? where id_pizza=? -- [romana(String), 12.34(BigDecimal), 42(Long)]",
                emo.update(entityMeta, pizza).toString());
        assertEquals("select id_pizza,name,price from pizza where id_pizza=? -- [42(Long)]",
                emo.loadById(entityMeta, 42L).toString());
        assertEquals(
                "insert into pizza (id_pizza,name,price) values (?,?,?) -- [42(Long), romana(String), 12.34(BigDecimal)]",
                emo.insert(entityMeta, pizza).toString());
        assertEquals("delete from pizza where id_pizza=? -- [42(Long)]", emo.delete(entityMeta, pizza).toString());
        assertEquals("select count(*) from pizza where id_pizza=? -- [42(Long)]",
                emo.existsById(entityMeta, 42L).toString());
        assertEquals("select count(*) from pizza where id_pizza=? -- [42(Long)]",
                emo.exists(entityMeta, pizza).toString());
    }
}
