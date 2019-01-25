package cat.lechuga.jdbc.mql;

import java.util.Map;

import org.lenteja.jdbc.query.IQueryObject;

import cat.lechuga.jdbc.EntityMeta;

public interface QueryFormatter {

    IQueryObject format(Map<String, EntityMeta<?>> aliases, String fragment, Object[] args);

}
