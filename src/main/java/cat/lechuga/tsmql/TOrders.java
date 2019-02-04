package cat.lechuga.tsmql;

import java.util.Arrays;
import java.util.List;

import cat.lechuga.mql.Orders.Order;

public class TOrders<E> {

    private final List<TOrder<E>> orders;

    private TOrders(List<TOrder<E>> orders) {
        super();
        this.orders = orders;
    }

    public List<TOrder<E>> getOrders() {
        return orders;
    }

    @SafeVarargs
    public static <E> TOrders<E> by(TOrder<E>... orders) {
        return new TOrders<>(Arrays.asList(orders));
    }

    public static <E> TOrders<E> by(List<TOrder<E>> orders) {
        return new TOrders<>(orders);
    }

    public static class TOrder<E> {

        final MetaColumn<E, ?> metaColumn;
        final String order;

        private TOrder(MetaColumn<E, ?> metaColumn, String order) {
            super();
            this.metaColumn = metaColumn;
            this.order = order;
        }

        public static <E> TOrder<E> asc(MetaColumn<E, ?> metaColumn) {
            return new TOrder<>(metaColumn, Order.ASC);
        }

        public static <E> TOrder<E> desc(MetaColumn<E, ?> metaColumn) {
            return new TOrder<>(metaColumn, Order.DESC);
        }

        public MetaColumn<E, ?> getMetaColumn() {
            return metaColumn;
        }

        public String getOrder() {
            return order;
        }
    }
}