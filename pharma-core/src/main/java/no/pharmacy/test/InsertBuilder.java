package no.pharmacy.test;

import java.util.ArrayList;
import java.util.List;

import no.pharmacy.medication.JdbcSupport;

public class InsertBuilder {

    private String tableName;

    private List<String> fieldNames = new ArrayList<>();
    private List<Object> fieldValues = new ArrayList<>();

    private JdbcSupport jdbcSupport;

    public InsertBuilder(JdbcSupport jdbcSupport, String tableName) {
        this.jdbcSupport = jdbcSupport;
        this.tableName = tableName;
    }

    public InsertBuilder value(String field, Object value) {
        this.fieldNames.add(field);
        this.fieldValues.add(value);
        return this;
    }

    public long executeInsert() {
        return jdbcSupport.executeInsert(getQuery(), fieldValues);
    }

    private String getQuery() {
        return "insert into "
                + tableName
                + " (" + String.join(", ", fieldNames) + ")"
                + " values"
                + " (" + String.join(", ", repeat("?", fieldNames.size())) + ")";
    }

    private List<String> repeat(String string, int count) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(string);
        }
        return result;
    }

}
