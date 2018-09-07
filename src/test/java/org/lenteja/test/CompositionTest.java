package org.lenteja.test;

import java.util.Date;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;
import org.lenteja.Column;
import org.lenteja.EnumColumnHandler;
import org.lenteja.Table;
import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.JdbcDataAccesFacade;
import org.lenteja.jdbc.script.SqlScriptExecutor;

public class CompositionTest {

    final DataAccesFacade facade;

    public CompositionTest() {
        final JDBCDataSource ds = new JDBCDataSource();
        ds.setUrl("jdbc:hsqldb:mem:a");
        ds.setUser("sa");
        ds.setPassword("");
        this.facade = new JdbcDataAccesFacade(ds);
    }

    @Before
    public void before() {
        facade.begin();
        try {
            SqlScriptExecutor sql = new SqlScriptExecutor(facade);
            sql.runFromClasspath("films.sql");
            facade.commit();
        } catch (Exception e) {
            facade.rollback();
            throw e;
        }
    }

    @Test
    public void test() throws Exception {
        // TODO
    }

    public static class DogTable extends Table<Dog> {

        public final Column<Dog, Integer> id = addPkColumn(Integer.class, "idDog", "id_dog");
        public final Column<Dog, String> name = addColumn(String.class, "name", "name");
        public final Column<Dog, Boolean> alive = addColumn(Boolean.class, "alive", "is_alive");
        public final Column<Dog, ESex> sex = addColumn(ESex.class, "sex", "sex", new EnumColumnHandler<>(ESex.class));
        public final Column<Dog, Integer> idJefe = addPkColumn(Integer.class, "idJefe", "id_jefe");

        public DogTable(String alias) {
            super(Dog.class, "dogs", alias);
        }

        public DogTable() {
            this(null);
        }
    }

    public static class PersonTable extends Table<Person> {

        public final Column<Person, Long> idPerson = addPkColumn(Long.class, "id.idPerson", "id_person");
        public final Column<Person, String> dni = addColumn(String.class, "id.dni", "dni");
        public final Column<Person, String> name = addColumn(String.class, "name", "name");
        public final Column<Person, Integer> age = addColumn(Integer.class, "age", "age");
        public final Column<Person, Date> birthDate = addColumn(Date.class, "birthDate", "birth_date");

        public PersonTable(String alias) {
            super(Person.class, "persons", alias);
        }

        public PersonTable() {
            this(null);
        }
    }

    public static enum ESex {
        FEMALE, MALE;
    }

    public static class Dog {

        Integer idDog;
        String name;
        boolean alive;
        ESex sex;
        Long idJefe;

        public Dog() {
            super();
        }

        public Dog(Integer idDog, String name, boolean alive, ESex sex, Long idJefe) {
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

        public boolean isAlive() {
            return alive;
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

    public static class IdPerson {

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

    public static class Person {

        IdPerson id;
        String name;
        int age;
        Date birthDate;

        public Person() {
            super();
        }

        public Person(IdPerson id, String name, int age, Date birthDate) {
            super();
            this.id = id;
            this.name = name;
            this.age = age;
            this.birthDate = birthDate;
        }

        public IdPerson getId() {
            return id;
        }

        public void setId(IdPerson id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public Date getBirthDate() {
            return birthDate;
        }

        public void setBirthDate(Date birthDate) {
            this.birthDate = birthDate;
        }

        @Override
        public String toString() {
            return "Person [id=" + id + ", name=" + name + ", age=" + age + ", birthDate=" + birthDate + "]";
        }

    }

}
