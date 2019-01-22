package cat.lechuga.jdbc.test;

import java.math.BigDecimal;

import cat.lechuga.jdbc.anno.Id;

public class Pizza {

    @Id
    // @Generated(HsqldbIdentity.class) //TODO
    public Long idPizza;

    public String name;

    public BigDecimal price;

}
