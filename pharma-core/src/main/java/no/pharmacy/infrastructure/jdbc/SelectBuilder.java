package no.pharmacy.infrastructure.jdbc;

import java.util.ArrayList;
import java.util.List;

import no.pharmacy.infrastructure.jdbc.JdbcSupport.ResultSetMapper;

public class SelectBuilder {

    private final JdbcSupport jdbcSupport;

    private List<String> filters = new ArrayList<>();
    private List<Object> parameters = new ArrayList<>();
    private String tableName;

    SelectBuilder(JdbcSupport jdbcSupport, String tableName) {
        this.jdbcSupport = jdbcSupport;
        this.tableName = tableName;
    }

    public SelectBuilder where(String expression, Object parameter) {
        if (isPresent(parameter)) {
            this.filters.add(expression);
            this.parameters.add(parameter);
        }
        return this;
    }

    private boolean isPresent(Object parameter) {
        return ((parameter != null) && !parameter.equals(""));
    }

    public <T> List<T> list(ResultSetMapper<T> mapper) {
        return jdbcSupport.queryForList(getQuery(), getParameters(), mapper);
    }

    private List<Object> getParameters() {
        return parameters;
    }

    private String getQuery() {
        return "select * from " + tableName + " where " + String.join(" and ", filters);
    }

}
