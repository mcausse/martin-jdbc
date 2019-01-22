package cat.lechuga.jdbc.reflect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import cat.lechuga.jdbc.test.ESex;
import cat.lechuga.jdbc.test.Exp;

public class PropertyScannerTest {

    @Test
    public void testName() throws Exception {

        Map<String, Property> ps = new PropertyScanner().propertyScanner(Exp.class);
        for (Entry<String, Property> p : ps.entrySet()) {
            System.out.println(p);
        }

        Exp exp = new Exp();
        assertEquals("Exp [id=null, name=null, fecIni=null, sex=null, alive=false]", exp.toString());

        assertNull(ps.get("id.idEns").get(exp));
        assertNull(ps.get("id.anyExp").get(exp));
        assertNull(ps.get("id.numExp").get(exp));
        assertNull(ps.get("name").get(exp));
        assertNull(ps.get("fecIni").get(exp));
        assertNull(ps.get("sex").get(exp));

        ps.get("id.idEns").set(exp, 40123L);
        ps.get("id.anyExp").set(exp, 2019);
        ps.get("id.numExp").set(exp, 123L);
        ps.get("name").set(exp, "jou");
        ps.get("fecIni").set(exp, new Date(0L));
        ps.get("sex").set(exp, ESex.FEMALE);

        assertEquals(
                "Exp [id=ExpId [idEns=40123, anyExp=2019, numExp=123], name=jou, fecIni=19700101, sex=FEMALE, alive=false]",
                exp.toString());

        assertEquals(new Long(40123L), ps.get("id.idEns").get(exp));
        assertEquals(new Integer(2019), ps.get("id.anyExp").get(exp));
        assertEquals(new Long(123L), ps.get("id.numExp").get(exp));
        assertEquals("jou", ps.get("name").get(exp));
        assertEquals(new Date(0L), ps.get("fecIni").get(exp));
        assertEquals(ESex.FEMALE, ps.get("sex").get(exp));

    }

}
