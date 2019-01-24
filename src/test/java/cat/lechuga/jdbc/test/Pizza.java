package cat.lechuga.jdbc.test;

import java.math.BigDecimal;

import cat.lechuga.jdbc.anno.Generated;
import cat.lechuga.jdbc.anno.Id;
import cat.lechuga.jdbc.generator.impl.HsqldbSequence;

public class Pizza {

    @Id
    @Generated(value = HsqldbSequence.class, args = { "seq_pizzas" })
    public Long idPizza;

    public String name;

    public BigDecimal price;

}
