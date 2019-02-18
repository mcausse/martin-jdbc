package cat.lechuga.mql;

import java.util.Arrays;
import java.util.List;

public class Orders {

    private final List<Order> orders;

    public Orders(List<Order> orders) {
        super();
        this.orders = orders;
    }

    public List<Order> getOrders() {
        return orders;
    }

    @SafeVarargs
    public static Orders by(Order... orders) {
        return new Orders(Arrays.asList(orders));
    }

    public static Orders by(List<Order> orders) {
        return new Orders(orders);
    }

    public static class Order {

        public static final String ASC = " asc";
        public static final String DESC = " desc";

        final String propName;
        final String order;

        private Order(String propName, String order) {
            super();
            this.propName = propName;
            this.order = order;
        }

        public static Order asc(String propName) {
            return new Order(propName, ASC);
        }

        public static Order desc(String propName) {
            return new Order(propName, DESC);
        }

        public String getPropName() {
            return propName;
        }

        public String getOrder() {
            return order;
        }
    }
}
