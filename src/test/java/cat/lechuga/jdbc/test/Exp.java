package cat.lechuga.jdbc.test;

import java.text.SimpleDateFormat;
import java.util.Date;

import cat.lechuga.jdbc.anno.Column;
import cat.lechuga.jdbc.anno.Enumerated;
import cat.lechuga.jdbc.anno.Id;
import cat.lechuga.jdbc.anno.Table;

@Table("exps")
public class Exp {

    @Id
    ExpId id;

    String name;

    @Column("fecha_ini")
    Date fecIni;

    @Enumerated
    public ESex sex;

    boolean alive;

    public ExpId getId() {
        return id;
    }

    public void setId(ExpId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getFecIni() {
        return fecIni;
    }

    public void setFecIni(Date fecIni) {
        this.fecIni = fecIni;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return "Exp [id=" + id + ", name=" + name + ", fecIni=" + (fecIni == null ? "null" : sdf.format(fecIni))
                + ", sex=" + sex + ", alive=" + alive + "]";
    }

}