package no.pharmacy.medication;

import java.util.ArrayList;
import java.util.List;

public class UpdateBuilder {

    private List<String> whereFields = new ArrayList<>();
    private List<Object> whereValues = new ArrayList<>();

    private List<String> updateFields = new ArrayList<>();
    private List<Object> updateValues = new ArrayList<>();
    private JdbcSupport jdbcSupport;
    private String tableName;

    public UpdateBuilder(JdbcSupport jdbcSupport, String tableName) {
        this.jdbcSupport = jdbcSupport;
        this.tableName = tableName;
    }

    public UpdateBuilder where(String field, Object value) {
        whereFields.add(field + " = ?");
        whereValues.add(value);
        return this;
    }

    public UpdateBuilder set(String field, Object value) {
        updateFields.add(field + " = ?");
        updateValues.add(value);
        return this;
    }

    public int executeUpdate() {
        return jdbcSupport.executeUpdate(query(), parameters());
    }

    private List<Object> parameters() {
        ArrayList<Object> result = new ArrayList<>();
        result.addAll(updateValues);
        result.addAll(whereValues);
        return result;
    }

    private String query() {
        return "update " + tableName
                + " set " + String.join(", ", updateFields)
                + " where " + String.join(" and ", whereFields);
    }

}
