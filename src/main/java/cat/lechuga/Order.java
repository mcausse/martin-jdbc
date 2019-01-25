package cat.lechuga;

import java.util.Arrays;
import java.util.List;

public class Order<E> {

    @SafeVarargs
    public static <E> List<Order<E>> by(Order<E>... orders) {
        return Arrays.asList(orders);
    }

    ///////////////

    final String propName;
    final String order;

    private Order(String propName, String order) {
        super();
        this.propName = propName;
        this.order = order;
    }

    public static <E> Order<E> asc(String propName) {
        return new Order<>(propName, " asc");
    }

    public static <E> Order<E> desc(String propName) {
        return new Order<>(propName, " desc");
    }

    public String getPropName() {
        return propName;
    }

    public String getOrder() {
        return order;
    }

}