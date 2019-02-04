package cat.lechuga.mql;

import java.util.Arrays;
import java.util.List;

public class Orders<E> {

    private final List<Order<E>> orders;

    public Orders(List<Order<E>> orders) {
        super();
        this.orders = orders;
    }

    public List<Order<E>> getOrders() {
        return orders;
    }

    @SafeVarargs
    public static <E> Orders<E> by(Order<E>... orders) {
        return new Orders<>(Arrays.asList(orders));
    }

    public static <E> Orders<E> by(List<Order<E>> orders) {
        return new Orders<>(orders);
    }

    public static class Order<E> {

        public static final String ASC = " asc";
        public static final String DESC = " desc";

        final String propName;
        final String order;

        private Order(String propName, String order) {
            super();
            this.propName = propName;
            this.order = order;
        }

        public static <E> Order<E> asc(String propName) {
            return new Order<>(propName, ASC);
        }

        public static <E> Order<E> desc(String propName) {
            return new Order<>(propName, DESC);
        }

        public String getPropName() {
            return propName;
        }

        public String getOrder() {
            return order;
        }
    }
}
