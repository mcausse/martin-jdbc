package cat.lechuga.jdbc.mql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import cat.lechuga.jdbc.EntityManager;
import cat.lechuga.jdbc.EntityManagerFactory;
import cat.lechuga.jdbc.test.ESex;
import cat.lechuga.jdbc.test.Exp;
import cat.lechuga.jdbc.test.ExpId;

public class QueryBuilderTest {

    @Test
    public void testName() throws Exception {

        EntityManagerFactory emf = new EntityManagerFactory(null);
        EntityManager<Exp, ExpId> expMan = emf.buildEntityManager(Exp.class);

        {
            QueryBuilder qb = new QueryBuilder(null);
            qb.addAlias("e", expMan);

            qb.append("select {e.*} ");
            qb.append("from {e.#} ");
            qb.append("where {e.id.anyExp=?} and {e.sex in (?,?)}", 1982, ESex.FEMALE, ESex.MALE);

            System.out.println(qb.getQueryObject().toString());
        }

        {
            QueryBuilder qb = new QueryBuilder(null);
            qb.addAlias("e", expMan);

            try {
                qb.append("where {e.id.anyExp=?} and {e.sex in (?,?)}", 1982, ESex.FEMALE);
                fail();
            } catch (Exception e) {
                assertEquals("expected one more argument", e.getMessage());
            }
        }
        {
            QueryBuilder qb = new QueryBuilder(null);
            qb.addAlias("e", expMan);

            try {
                qb.append("where {e.id.anyExp=?} and {e.sex=?}", 1982, ESex.FEMALE, ESex.MALE);
                fail();
            } catch (Exception e) {
                assertEquals("unused argument at index 2", e.getMessage());
            }
        }

        {
            QueryBuilder qb = new QueryBuilder(null);
            qb.addAlias("e", expMan);

            try {
                qb.append("where {kk.id.anyExp=?}", 1982);
                fail();
            } catch (Exception e) {
                assertEquals("alias not found: 'kk', valid are: [e]", e.getMessage());
            }
        }
        {
            QueryBuilder qb = new QueryBuilder(null);
            qb.addAlias("e", expMan);

            try {
                qb.append("where {e.kkk=?}", 1982);
                fail();
            } catch (Exception e) {
                assertEquals("property not defined: 'cat.lechuga.jdbc.test.Exp#kkk", e.getMessage());
            }
        }

    }
}
