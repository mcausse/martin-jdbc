package cat.lechuga.jdbc.test;

import java.math.BigDecimal;

import cat.lechuga.anno.Generated;
import cat.lechuga.anno.Id;
import cat.lechuga.generator.impl.HsqldbSequence;

public class Pizza {

    @Id
    @Generated(value = HsqldbSequence.class, args = { "seq_pizzas" })
    public Long idPizza;

    public String name;

    public BigDecimal price;

}
