package cat.lechuga.repository;

import org.lenteja.jdbc.query.IQueryObject;

import cat.lechuga.tsmql.MetaTable;

public interface Sort<E_ extends MetaTable<?>> {

    IQueryObject toPredicate(E_ meta);

}