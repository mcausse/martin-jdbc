package org.lenteja.test;

public class IdPerson {

    Long idPerson;
    String dni;

    public IdPerson() {
        super();
    }

    public IdPerson(Long idPerson, String dni) {
        super();
        this.idPerson = idPerson;
        this.dni = dni;
    }

    public Long getIdPerson() {
        return idPerson;
    }

    public void setIdPerson(Long idPerson) {
        this.idPerson = idPerson;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    @Override
    public String toString() {
        return "IdPerson [idPerson=" + idPerson + ", dni=" + dni + "]";
    }

}