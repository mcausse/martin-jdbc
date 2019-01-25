package org.lenteja.mapper.query;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.mapper.Column;

public class Order<E> implements IQueryObject {

    @SafeVarargs
    public static <E> List<Order<E>> by(Order<E>... orders) {
        return Arrays.asList(orders);
    }

    ///////////////

    final Column<E, ?> column;
    final String order;

    private Order(Column<E, ?> column, String order) {
        super();
        this.column = column;
        this.order = order;
    }

    public static <E> Order<E> asc(Column<E, ?> column) {
        return new Order<>(column, " asc");
    }

    public static <E> Order<E> desc(Column<E, ?> column) {
        return new Order<>(column, " desc");
    }

    @Override
    public String toString() {
        return column.getAliasedName() + order;
    }

    @Override
    public String getQuery() {
        return toString();
    }

    @Override
    public Object[] getArgs() {
        return new Object[] {};
    }

    @Override
    public List<Object> getArgsList() {
        return Collections.emptyList();
    }
}