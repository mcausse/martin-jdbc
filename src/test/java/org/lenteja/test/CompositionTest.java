package org.lenteja.test;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;
import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.JdbcDataAccesFacade;
import org.lenteja.jdbc.extractor.MapResultSetExtractor;
import org.lenteja.jdbc.script.SqlScriptExecutor;
import org.lenteja.mapper.Column;
import org.lenteja.mapper.EntityManager;
import org.lenteja.mapper.EnumColumnHandler;
import org.lenteja.mapper.Table;
import org.lenteja.mapper.autogen.impl.HsqldbIdentity;
import org.lenteja.mapper.autogen.impl.HsqldbSequence;
import org.lenteja.mapper.collabs.JoinColumn;
import org.lenteja.mapper.collabs.ManyToOne;
import org.lenteja.mapper.collabs.OneToMany;
import org.lenteja.mapper.query.ELike;
import org.lenteja.mapper.query.Order;
import org.lenteja.mapper.query.Relational;

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
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }
    }

    @Test
    public void test() throws Exception {

        facade.begin();
        try {

            EntityManager entityManager = new EntityManager(facade);

            DogTable dogTable = new DogTable();
            PersonTable personTable = new PersonTable();

            Person mhc = new Person(new IdPerson(null, "8P"), "mhc", 36, new Date(0L));
            assertEquals("Person [id=IdPerson [idPerson=null, dni=8P], name=mhc, age=36, birthDate=01/01/1970]",
                    mhc.toString());
            entityManager.store(personTable, mhc);
            assertEquals("Person [id=IdPerson [idPerson=10, dni=8P], name=mhc, age=36, birthDate=01/01/1970]",
                    mhc.toString());

            Dog chucho = new Dog(null, "chucho", true, ESex.FEMALE, mhc.getId().getIdPerson());
            Dog din = new Dog(null, "din", false, ESex.MALE, mhc.getId().getIdPerson());

            assertEquals("Dog [idDog=null, name=chucho, alive=true, sex=FEMALE, idJefe=10]", chucho.toString());
            assertEquals("Dog [idDog=null, name=din, alive=false, sex=MALE, idJefe=10]", din.toString());
            entityManager.store(dogTable, chucho);
            entityManager.store(dogTable, din);
            assertEquals("Dog [idDog=100, name=chucho, alive=true, sex=FEMALE, idJefe=10]", chucho.toString());
            assertEquals("Dog [idDog=101, name=din, alive=false, sex=MALE, idJefe=10]", din.toString());

            assertEquals("Dog [idDog=101, name=din, alive=false, sex=MALE, idJefe=10]", //
                    entityManager.queryFor(dogTable) //
                            .append("select * from {} ", dogTable) //
                            .append("where {}", dogTable.idDog.eq(101)) //
                            .getExecutor(facade) //
                            .loadUnique() //
                            .toString() //
            );
            assertEquals("201", //
                    entityManager.scalarQueryFor(dogTable.idDog) //
                            .append("select sum({}) as {} ", dogTable.idDog, dogTable.idDog) //
                            .append("from {} ", dogTable) //
                            .getExecutor(facade) //
                            .loadUnique() //
                            .toString());

            assertEquals(
                    "[Dog [idDog=100, name=chucho, alive=true, sex=FEMALE, idJefe=10], Dog [idDog=101, name=din, alive=false, sex=MALE, idJefe=10]]", //
                    entityManager.query(dogTable, Relational.all(), Order.by(Order.asc(dogTable.idDog))).toString());

            assertEquals("Dog [idDog=101, name=din, alive=false, sex=MALE, idJefe=10]", //
                    entityManager.queryUnique(dogTable, dogTable.name.ilike(ELike.CONTAINS, "i")).toString());

            assertEquals("Dog [idDog=101, name=din, alive=false, sex=MALE, idJefe=10]", //
                    entityManager.queryUnique(dogTable, din).toString());

            Dog example = new Dog(null, null, null, null, mhc.getId().getIdPerson());
            assertEquals(
                    "[Dog [idDog=100, name=chucho, alive=true, sex=FEMALE, idJefe=10], Dog [idDog=101, name=din, alive=false, sex=MALE, idJefe=10]]", //
                    entityManager.query(dogTable, example).toString());

            din.setSex(ESex.FEMALE);
            entityManager.store(dogTable, din);

            assertEquals(
                    "[Dog [idDog=100, name=chucho, alive=true, sex=FEMALE, idJefe=10], Dog [idDog=101, name=din, alive=false, sex=FEMALE, idJefe=10]]", //
                    entityManager.query(dogTable, example).toString());

            entityManager.delete(dogTable, din);

            assertEquals("[Dog [idDog=100, name=chucho, alive=true, sex=FEMALE, idJefe=10]]", //
                    entityManager.query(dogTable, example).toString());

            ////

            Dog faria = new Dog(null, "faria", false, ESex.FEMALE, mhc.getId().getIdPerson());
            entityManager.insert(dogTable, faria);

            {

                DogTable dt = new DogTable("d");
                PersonTable pt = new PersonTable("p");

                Dog chucho2 = entityManager.queryFor(dt) //
                        .append("select {} ", dt.all()) //
                        .append("from {} join {} ", pt, dt) //
                        .append("on {} ", pt.idPerson.eq(dt.idJefe)) //
                        .append("where {} ", Relational.and( //
                                pt.dni.isNotNull(), //
                                pt.dni.eq("8P"), //
                                dt.sex.in(ESex.FEMALE, ESex.MALE), //
                                dt.alive.eq(true) //
                        )) //
                        .getExecutor(facade) //
                        .loadUnique() //
                ;

                // select d.id_dog,d.name,d.is_alive,d.sex,d.id_jefe from persons p join dogs d
                // on p.id_person=d.id_jefe where p.dni IS NOT NULL and p.dni=? and d.sex in
                // (?,?) and d.is_alive=? -- [8P(String), FEMALE(String), MALE(String),
                // true(Boolean)]

                assertEquals("Dog [idDog=100, name=chucho, alive=true, sex=FEMALE, idJefe=10]", //
                        chucho2.toString());
            }

            {
                List<Map<String, Object>> result = entityManager.queryFor(dogTable) //
                        .append("select {} from {} ", dogTable.all(), dogTable) //
                        .getExecutor(facade) //
                        .extract(new MapResultSetExtractor()) //
                ;

                assertEquals( //
                        "[{ID_DOG=100, NAME=chucho, IS_ALIVE=true, SEX=FEMALE, ID_JEFE=10}, " + //
                                "{ID_DOG=102, NAME=faria, IS_ALIVE=false, SEX=FEMALE, ID_JEFE=10}]", //
                        result.toString());
            }

            facade.commit();
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }
    }

    @Test
    public void testCompositions() throws Exception {

        facade.begin();
        try {

            EntityManager entityManager = new EntityManager(facade);

            DogTable dogTable = new DogTable();
            PersonTable personTable = new PersonTable();

            Person mhc = new Person(new IdPerson(null, "8P"), "mhc", 36, new Date(0L));
            entityManager.insert(personTable, mhc);

            Dog chucho = new Dog(null, "chucho", true, ESex.FEMALE, mhc.getId().getIdPerson());
            Dog din = new Dog(null, "din", false, ESex.FEMALE, mhc.getId().getIdPerson());
            entityManager.store(dogTable, chucho);
            entityManager.store(dogTable, din);
            din.setSex(ESex.MALE);
            entityManager.store(dogTable, din);

            ////

            DogTable dogRef = new DogTable("d");
            PersonTable personRef = new PersonTable("p");

            ////

            ManyToOne<Dog, Person> jefeOfDog = new ManyToOne<>(dogRef, personRef,
                    new JoinColumn<>(dogRef.idJefe, personRef.idPerson));

            Person mhc2 = jefeOfDog.fetch(facade, chucho);
            Person mhc3 = jefeOfDog.fetch(facade, din);

            assertEquals("Person [id=IdPerson [idPerson=10, dni=8P], name=mhc, age=36, birthDate=01/01/1970]",
                    mhc.toString());
            assertEquals("Person [id=IdPerson [idPerson=10, dni=8P], name=mhc, age=36, birthDate=01/01/1970]",
                    mhc2.toString());
            assertEquals("Person [id=IdPerson [idPerson=10, dni=8P], name=mhc, age=36, birthDate=01/01/1970]",
                    mhc3.toString());

            ////

            final OneToMany<Person, Dog> dogsOfPerson = new OneToMany<>(personRef, dogRef,
                    new JoinColumn<>(personRef.idPerson, dogRef.idJefe));

            List<Dog> dogs = dogsOfPerson.fetch(facade, mhc);
            assertEquals(
                    "[Dog [idDog=100, name=chucho, alive=true, sex=FEMALE, idJefe=10], Dog [idDog=101, name=din, alive=false, sex=MALE, idJefe=10]]",
                    dogs.toString());

            facade.commit();
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }
    }

    public static class DogTable extends Table<Dog> {

        public final Column<Dog, Integer> idDog = addPkColumn(Integer.class, "idDog", "id_dog");
        public final Column<Dog, String> name = addColumn(String.class, "name", "name");
        public final Column<Dog, Boolean> alive = addColumn(Boolean.class, "alive", "is_alive");
        public final Column<Dog, ESex> sex = addColumn(ESex.class, "sex", "sex", new EnumColumnHandler<>(ESex.class));
        public final Column<Dog, Long> idJefe = addPkColumn(Long.class, "idJefe", "id_jefe");

        public DogTable(String alias) {
            super(Dog.class, "dogs", alias);
            addAutoGenerated(new HsqldbIdentity<>(idDog));
        }

        public DogTable() {
            this(null);
        }
    }

    public static class PersonTable extends Table<Person> {

        public final Column<Person, Long> idPerson = addPkColumn(Long.class, "id.idPerson");
        public final Column<Person, String> dni = addColumn(String.class, "id.dni");
        public final Column<Person, String> name = addColumn(String.class, "name");
        public final Column<Person, Integer> age = addColumn(Integer.class, "age");
        public final Column<Person, Date> birthDate = addColumn(Date.class, "birthDate");

        public PersonTable(String alias) {
            super(Person.class, "persons", alias);
            addAutoGenerated(new HsqldbSequence<>(idPerson, "seq_persons"));
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
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return "Person [id=" + id + ", name=" + name + ", age=" + age + ", birthDate=" + sdf.format(birthDate)
                    + "]";
        }

    }

}
