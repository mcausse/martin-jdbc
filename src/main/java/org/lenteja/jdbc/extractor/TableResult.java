package org.lenteja.jdbc.extractor;

import java.util.List;
import java.util.Map;

public class TableResult {

    final List<String> columnNames;
    final List<Map<String, Object>> rows;

    public TableResult(List<String> columnNames, List<Map<String, Object>> rows) {
        super();
        this.columnNames = columnNames;
        this.rows = rows;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    @Override
    public String toString() {
        return getRows().toString();
    }

}