package org.lenteja.test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;
import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.JdbcDataAccesFacade;
import org.lenteja.jdbc.extractor.PageResult;
import org.lenteja.jdbc.extractor.Pager;
import org.lenteja.jdbc.script.SqlScriptExecutor;
import org.lenteja.mapper.GenericDao;
import org.lenteja.mapper.collabs.JoinColumn;
import org.lenteja.mapper.collabs.ManyToOne;
import org.lenteja.mapper.collabs.OneToMany;
import org.lenteja.mapper.query.Executor;
import org.lenteja.mapper.query.Order;

public class GenericDaoTest {

    final DataAccesFacade facade;

    public GenericDaoTest() {
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

    public static class PersonDao extends GenericDao<Person, IdPerson> {

        final OneToMany<Person, Dog> dogsOfPerson;

        public PersonDao(DataAccesFacade facade) {
            super(facade, new PersonTable());

            DogTable dogRef = new DogTable();
            PersonTable personRef = new PersonTable();
            this.dogsOfPerson = new OneToMany<>(personRef, dogRef, new JoinColumn<>(personRef.idPerson, dogRef.idJefe));
        }

        public OneToMany<Person, Dog> getDogsOfPerson() {
            return dogsOfPerson;
        }

    }

    public static class DogDao extends GenericDao<Dog, Integer> {

        final ManyToOne<Dog, Person> jefeOfDog;

        public DogDao(DataAccesFacade facade) {
            super(facade, new DogTable());

            DogTable dogRef = new DogTable();
            PersonTable personRef = new PersonTable();
            this.jefeOfDog = new ManyToOne<>(dogRef, personRef, new JoinColumn<>(dogRef.idJefe, personRef.idPerson));
        }

        public ManyToOne<Dog, Person> getJefeOfDog() {
            return jefeOfDog;
        }

    }

    @Test
    public void test() throws Exception {

        facade.begin();
        try {

            DogTable d = new DogTable();

            PersonDao pdao = new PersonDao(facade);
            DogDao ddao = new DogDao(facade);

            Person mhc = new Person(new IdPerson(null, "8P"), "mhc", 36, new Date(0L));
            Person mem = new Person(new IdPerson(null, "9P"), "mem", 40, new Date(0L));
            pdao.storeAll(Arrays.asList(mhc, mem));

            Dog chucho = new Dog(null, "chucho", true, ESex.FEMALE, mhc.getId().getIdPerson());
            Dog din = new Dog(null, "din", false, ESex.MALE, mhc.getId().getIdPerson());
            ddao.storeAll(Arrays.asList(chucho, din));

            List<Dog> mhcDogs = ddao.queryFor(d) //
                    .append("select {} from {} ", d.all(), d) //
                    .append("where {} ", d.idJefe.eq(mhc.getId().getIdPerson())) //
                    .append("order by {} ", Order.asc(d.idDog)) //
                    .getExecutor(facade) //
                    .load() //
            ;
            assertEquals(2, mhcDogs.size());
            assertEquals( //
                    "[Dog [idDog=100, name=chucho, alive=true, sex=FEMALE, idJefe=10], " + //
                            "Dog [idDog=101, name=din, alive=false, sex=MALE, idJefe=10]]", //
                    mhcDogs.toString());

            {
                List<Integer> mhcDogsIds = ddao.scalarQueryFor(d.idDog) //
                        .append("select {} from {} ", d.idDog, d) //
                        .append("where {} ", d.idJefe.eq(mhc.getId().getIdPerson())) //
                        .append("order by {} ", Order.asc(d.idDog)) //
                        .getExecutor(facade) //
                        .load() //
                ;
                assertEquals(2, mhcDogsIds.size());
                assertEquals("[100, 101]", mhcDogsIds.toString());
            }

            mhcDogs = pdao.getDogsOfPerson().fetch(facade, mhc);
            List<Dog> memDogs = pdao.getDogsOfPerson().fetch(facade, mem);
            assertEquals(2, mhcDogs.size());
            assertEquals(0, memDogs.size());

            assertEquals("Person [id=IdPerson [idPerson=10, dni=8P], name=mhc, age=36, birthDate=01/01/1970]",
                    ddao.getJefeOfDog().fetch(facade, chucho).toString());

            facade.commit();
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }
    }

    @Test
    public void testPagination() throws Exception {

        facade.begin();
        try {

            DogTable d = new DogTable();

            PersonDao pdao = new PersonDao(facade);
            DogDao ddao = new DogDao(facade);

            Person mhc = new Person(new IdPerson(null, "8P"), "mhc", 36, new Date(0L));
            pdao.store(mhc);

            Dog chucho = new Dog(null, "chucho", true, ESex.FEMALE, mhc.getId().getIdPerson());
            Dog din = new Dog(null, "din", false, ESex.MALE, mhc.getId().getIdPerson());
            ddao.storeAll(Arrays.asList(chucho, din));

            Executor<Dog> executor = ddao.queryFor() //
                    .append("select * from {} order by {}", d, Order.desc(d.idDog)) //
                    .getExecutor(facade);

            {
                Pager<Dog> pager = new Pager<>(2, 0);
                PageResult<Dog> p0 = executor.loadPage(pager);
                assertEquals(
                        "PageResult [pager=Pager [pageSize=2, numPage=0], totalRows=2, totalPages=1, page=[Dog [idDog=101, name=din, alive=false, sex=MALE, idJefe=10], Dog [idDog=100, name=chucho, alive=true, sex=FEMALE, idJefe=10]]]",
                        p0.toString());
            }
            {
                Pager<Dog> pager = new Pager<>(2, 1);
                PageResult<Dog> p1 = executor.loadPage(pager);
                assertEquals("PageResult [pager=Pager [pageSize=2, numPage=1], totalRows=2, totalPages=1, page=[]]",
                        p1.toString());
            }
            {
                Dog faria = new Dog(null, "faria", false, ESex.FEMALE, mhc.getId().getIdPerson());
                ddao.store(faria);
            }
            {
                Pager<Dog> pager = new Pager<>(2, 1);
                PageResult<Dog> p1 = executor.loadPage(pager);
                assertEquals(
                        "PageResult [pager=Pager [pageSize=2, numPage=1], totalRows=3, totalPages=2, page=[Dog [idDog=100, name=chucho, alive=true, sex=FEMALE, idJefe=10]]]",
                        p1.toString());
            }

            facade.commit();
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }
    }

}
