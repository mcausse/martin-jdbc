package cat.lechuga.jdbc.test;

import java.math.BigDecimal;

import cat.lechuga.anno.Generated;
import cat.lechuga.anno.Id;
import cat.lechuga.anno.Table;
import cat.lechuga.generator.impl.HsqldbIdentity;

@Table("pizzas")
public class Pizza {

    @Id
    @Generated(value = HsqldbIdentity.class)
    public Long idPizza;

    public String name;

    public BigDecimal price;

    @Override
    public String toString() {
        return idPizza + ":" + name + ":" + price;
    }

}
