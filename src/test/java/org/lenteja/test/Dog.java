package org.lenteja.test;

public class Dog {

    Integer idDog;
    String name;
    Boolean alive;
    ESex sex;
    Long idJefe;

    public Dog() {
        super();
    }

    public Dog(Integer idDog, String name, Boolean alive, ESex sex, Long idJefe) {
        super();
        this.idDog = idDog;
        this.name = name;
        this.alive = alive;
        this.sex = sex;
        this.idJefe = idJefe;
    }

    public Integer getIdDog() {
        return idDog;
    }

    public void setIdDog(Integer idDog) {
        this.idDog = idDog;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getAlive() {
        return alive;
    }

    public void setAlive(Boolean alive) {
        this.alive = alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public ESex getSex() {
        return sex;
    }

    public void setSex(ESex sex) {
        this.sex = sex;
    }

    public Long getIdJefe() {
        return idJefe;
    }

    public void setIdJefe(Long idJefe) {
        this.idJefe = idJefe;
    }

    @Override
    public String toString() {
        return "Dog [idDog=" + idDog + ", name=" + name + ", alive=" + alive + ", sex=" + sex + ", idJefe=" + idJefe
                + "]";
    }

}