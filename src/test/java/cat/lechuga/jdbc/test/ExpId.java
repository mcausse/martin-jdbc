package cat.lechuga.jdbc.test;

import cat.lechuga.jdbc.anno.Column;
import cat.lechuga.jdbc.anno.Generated;
import cat.lechuga.jdbc.generator.impl.HsqldbIdentity;
import cat.lechuga.jdbc.reflect.anno.Embeddable;

@Embeddable
public class ExpId {

    Long idEns;

    public Integer anyExp;

    @Generated(HsqldbIdentity.class)
    @Column("num_exp")
    Long numExp;

    public Long getIdEns() {
        return idEns;
    }

    public void setIdEns(Long idEns) {
        this.idEns = idEns;
    }

    public Long getNumExp() {
        return numExp;
    }

    public void setNumExp(Long numExp) {
        this.numExp = numExp;
    }

    @Override
    public String toString() {
        return "ExpId [idEns=" + idEns + ", anyExp=" + anyExp + ", numExp=" + numExp + "]";
    }
}
