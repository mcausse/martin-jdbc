package org.lenteja.jdbc.extractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.lenteja.mapper.Mapable;

public class EntityMapResultSetExtractor2<K, E> implements ResultSetExtractor<Map<K, E>> {

    final Mapable<K> keyMapper;
    final Mapable<E> valueMapper;

    public EntityMapResultSetExtractor2(Mapable<K> keyMapper, Mapable<E> valueMapper) {
        super();
        this.keyMapper = keyMapper;
        this.valueMapper = valueMapper;
    }

    @Override
    public Map<K, E> extract(ResultSet rs) throws SQLException {
        Map<K, E> r = new LinkedHashMap<>();

        while (rs.next()) {
            K key = keyMapper.map(rs);
            E row = valueMapper.map(rs);
            r.put(key, row);
        }

        return r;
    }

}