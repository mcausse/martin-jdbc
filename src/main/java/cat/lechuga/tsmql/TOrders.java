package cat.lechuga.tsmql;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.lenteja.jdbc.query.IQueryObject;

import cat.lechuga.mql.Orders.Order;

public class TOrders implements IQueryObject {

    private final List<TOrder> orders;

    private TOrders(List<TOrder> orders) {
        super();
        this.orders = orders;
    }

    public List<TOrder> getOrders() {
        return orders;
    }

    @SafeVarargs
    public static TOrders by(TOrder... orders) {
        return new TOrders(Arrays.asList(orders));
    }

    public static TOrders by(List<TOrder> orders) {
        return new TOrders(orders);
    }

    ///////////////////////////////////////////////////

    @Override
    public String getQuery() {
        StringBuilder s = new StringBuilder();
        int c = 0;
        for (TOrder o : orders) {
            if (c > 0) {
                s.append(", ");
            }
            c++;
            MetaColumn<?, ?> metac = o.getMetaColumn();
            s.append("{" + metac.getAlias() + "." + metac.getPropertyName() + "} " + o.getOrder());
        }
        return s.toString();
    }

    @Override
    public Object[] getArgs() {
        return new Object[] {};
    }

    @Override
    public List<Object> getArgsList() {
        return Collections.emptyList();
    }

    ///////////////////////////////////////////////////

    public static class TOrder {

        final MetaColumn<?, ?> metaColumn;
        final String order;

        private TOrder(MetaColumn<?, ?> metaColumn, String order) {
            super();
            this.metaColumn = metaColumn;
            this.order = order;
        }

        public static TOrder asc(MetaColumn<?, ?> metaColumn) {
            return new TOrder(metaColumn, Order.ASC);
        }

        public static TOrder desc(MetaColumn<?, ?> metaColumn) {
            return new TOrder(metaColumn, Order.DESC);
        }

        public MetaColumn<?, ?> getMetaColumn() {
            return metaColumn;
        }

        public String getOrder() {
            return order;
        }
    }

}