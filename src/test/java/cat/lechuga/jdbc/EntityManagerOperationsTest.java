package cat.lechuga.jdbc;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Test;

import cat.lechuga.jdbc.test.ESex;
import cat.lechuga.jdbc.test.Exp;
import cat.lechuga.jdbc.test.ExpId;
import cat.lechuga.jdbc.test.Pizza;

public class EntityManagerOperationsTest {

    @Test
    public void testExpExpId() throws Exception {

        EntityManagerFactory emf = new EntityManagerFactory();
        EntityMeta<Exp> entityMeta = emf.buildEntityMeta(Exp.class);
        EntityManagerOperations<Exp> emo = new EntityManagerOperations<>(entityMeta);

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

        System.out.println(emo.update(exp));
        assertEquals(
                "update exps set name=?,fecha_ini=?,sex=?,alive=? where id_ens=? and any_exp=? and num_exp=? -- [jou(String), Thu Jan 01 01:00:00 CET 1970(Date), FEMALE(String), true(Boolean), 8(Long), 1982(Integer), 123(Long)]",
                emo.update(exp).toString());

        System.out.println(emo.loadById(id));// TODO
        System.out.println(emo.insert(exp));// TODO
        System.out.println(emo.delete(exp));// TODO
        System.out.println(emo.deleteById(id));// TODO
        System.out.println(emo.existsById(id));// TODO
        System.out.println(emo.exists(exp));// TODO
    }

    @Test
    public void testPizza() throws Exception {

        EntityManagerFactory emf = new EntityManagerFactory();
        EntityMeta<Pizza> entityMeta = emf.buildEntityMeta(Pizza.class);
        EntityManagerOperations<Pizza> emo = new EntityManagerOperations<>(entityMeta);

        Pizza pizza = new Pizza();
        pizza.idPizza = 42L;
        pizza.name = "romana";
        pizza.price = new BigDecimal("12.34");

        System.out.println(emo.update(pizza));
        assertEquals(
                "update pizza set name=?,price=? where id_pizza=? -- [romana(String), 12.34(BigDecimal), 42(Long)]",
                emo.update(pizza).toString());

        System.out.println(emo.loadById(42L));// TODO
        System.out.println(emo.insert(pizza));// TODO
        System.out.println(emo.delete(pizza));// TODO
        System.out.println(emo.deleteById(42L));// TODO
        System.out.println(emo.existsById(42L));// TODO
        System.out.println(emo.exists(pizza));// TODO
    }
}
