package org.lenteja.jdbc.extractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.lenteja.mapper.Mapable;

public class ResultSetPagedExtractor<T> implements ResultSetExtractor<Pager<T>> {

    final Mapable<T> rowMapper;
    final int pageSize;
    final int numPage;

    public ResultSetPagedExtractor(Mapable<T> rowMapper, int pageSize, int numPage) {
        super();
        this.rowMapper = rowMapper;
        this.pageSize = pageSize;
        this.numPage = numPage;
    }

    @Override
    public Pager<T> extract(ResultSet rs) throws SQLException {
        final List<T> r = new ArrayList<T>();
        rs.absolute(pageSize * numPage);
        int k = 0;
        while (rs.next() && k < pageSize) {
            r.add(rowMapper.map(rs));
            k++;
        }

        rs.last();
        final int totalRows = rs.getRow();

        int totalPages;
        if (totalRows % pageSize == 0) {
            totalPages = totalRows / pageSize;
        } else {
            totalPages = totalRows / pageSize + 1;
        }

        return new Pager<>(pageSize, numPage, totalRows, totalPages, r);
    }

}