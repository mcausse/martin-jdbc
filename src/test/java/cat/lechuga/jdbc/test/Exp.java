package cat.lechuga.jdbc.test;

import java.text.SimpleDateFormat;
import java.util.Date;

import cat.lechuga.anno.Column;
import cat.lechuga.anno.Enumerated;
import cat.lechuga.anno.Handler;
import cat.lechuga.anno.Id;
import cat.lechuga.anno.Table;
import cat.lechuga.handler.DateStringHandler;

@Table("exps")
public class Exp {

    @Id
    ExpId id;

    String name;

    @Handler(value = DateStringHandler.class, args = { "dd-MM-yyyy" })
    @Column("fecha_ini")
    Date fecIni;

    @Enumerated
    public ESex sex;

    boolean alive;

    // TODO gran cosa rollo ORM !!!
    // @Query(lazy=true, result=Tex.class, query= //
    // "select {_ref.*} from {_ref.#} " //
    // "where _ref.idEns=_this.idEns and _ref.idEns=_this.idEns and
    // _ref.idEns=_this.idEns")
    // List<Tex> texs;

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