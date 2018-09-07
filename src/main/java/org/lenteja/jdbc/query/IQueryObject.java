package org.lenteja.jdbc.query;

import java.util.List;

public interface IQueryObject {

    String getQuery();

    Object[] getArgs();

    List<Object> getArgsList();

}
