package org.lenteja.jdbc.extractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cat.lechuga.Mapable;

public class ResultSetPagedExtractor<T> implements ResultSetExtractor<PageResult<T>> {

    final Mapable<T> rowMapper;
    final Pager<T> pager;

    public ResultSetPagedExtractor(Mapable<T> rowMapper, Pager<T> pager) {
        super();
        this.rowMapper = rowMapper;
        this.pager = pager;
    }

    @Override
    public PageResult<T> extract(ResultSet rs) throws SQLException {
        final List<T> r = new ArrayList<T>();
        rs.absolute(pager.getPageSize() * pager.getNumPage());
        int k = 0;
        while (rs.next() && k < pager.getPageSize()) {
            r.add(rowMapper.map(rs));
            k++;
        }

        rs.last();
        final int totalRows = rs.getRow();

        int totalPages;
        if (totalRows % pager.getPageSize() == 0) {
            totalPages = totalRows / pager.getPageSize();
        } else {
            totalPages = totalRows / pager.getPageSize() + 1;
        }

        return new PageResult<T>(pager, totalRows, totalPages, r);
    }

}