package no.pharmacy.infrastructure.jdbc;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.pharmacy.core.Money;
import no.pharmacy.core.Reference;
import no.pharmacy.infrastructure.ExceptionUtil;

public class JdbcSupport {

    private static final Logger logger = LoggerFactory.getLogger(JdbcSupport.class);

    private final DataSource dataSource;

    public JdbcSupport(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @FunctionalInterface
    public interface ResultSetMapper<T> {
        T read(ResultSet rs) throws SQLException;
    }

    @FunctionalInterface
    public interface ResultSetListMapper<T> {
        List<T> read(ResultSet rs) throws SQLException;
    }



    public int executeUpdate(String query, List<Object> parameters) {
        long startTime = System.currentTimeMillis();
        try (Connection conn = dataSource.getConnection()) {
            logger.trace("executeUpdate {} {}", query, parameters);
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                setParameters(stmt, parameters);
                return stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw soften(query, e);
        } finally {
            logExecution("executeUpdate", query, startTime);
        }
    }

    long executeInsert(String query, List<Object> parameters) {
        long startTime = System.currentTimeMillis();
        try (Connection conn = dataSource.getConnection()) {
            logger.trace("executeInsert {} {}", query, parameters);
            try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                setParameters(stmt, parameters);
                stmt.executeUpdate();
                try(ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    } else {
                        throw new RuntimeException("Could not return keys");
                    }
                }
            }
        } catch (SQLException e) {
            throw soften(query, e);
        } finally {
            logger.trace("executeUpdate {}: {}ms", query, System.currentTimeMillis()-startTime);
        }
    }

    private void setParameters(PreparedStatement stmt, List<Object> parameters) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            Object parameter = parameters.get(i);
            if (parameter instanceof Money) {
                stmt.setBigDecimal(i+1, ((Money)parameter).toBigDecimal());
            } else if (parameter instanceof Reference) {
                stmt.setString(i+1, ((Reference)parameter).getReference());
            } else if (parameter instanceof Enum<?>) {
                stmt.setString(i+1, ((Enum<?>)parameter).name());
            } else {
                stmt.setObject(i+1, parameter);
            }
        }
    }

    public <T> Optional<T> retrieveSingle(String query, List<Object> parameters, ResultSetMapper<T> mapper) {
        long startTime = System.currentTimeMillis();
        logger.trace("retrieveSingle {} {}", query, parameters);
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                setParameters(stmt, parameters);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapper.read(rs));
                    } else {
                        return Optional.empty();
                    }
                }
            }
        } catch (SQLException e) {
            throw ExceptionUtil.softenException(e);
        } finally {
            logExecution("retrieveSingle", query, startTime);
        }
    }

    protected <T> List<T> queryForResultSet(String query, List<Object> parameters, ResultSetListMapper<T> mapper) {
        long startTime = System.currentTimeMillis();
        logger.trace("queryForResultSet {} {}", query, parameters);
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                setParameters(stmt, parameters);
                try (ResultSet rs = stmt.executeQuery()) {
                    return mapper.read(rs);
                }
            }
        } catch (SQLException e) {
            throw soften(query, e);
        } finally {
            logExecution("queryForResultSet", query, startTime);
        }
    }

    public <T> List<T> queryForList(String query, List<Object> parameters, ResultSetMapper<T> mapper) {
        long startTime = System.currentTimeMillis();
        try (Connection conn = dataSource.getConnection()) {
            logger.trace("queryForList {} {}", query, parameters);
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                setParameters(stmt, parameters);
                try (ResultSet rs = stmt.executeQuery()) {
                    List<T> results = new ArrayList<>();
                    while (rs.next()) {
                        results.add(mapper.read(rs));
                    }
                    return results;
                }
            }
        } catch (SQLException e) {
            throw soften(query, e);
        } finally {
            logExecution("queryForList", query, startTime);
        }
    }

    protected LocalDate toLocalDate(Date date) {
        return date != null ? date.toLocalDate() : null;
    }

    public InsertBuilder insertInto(String tableName) {
        return new InsertBuilder(this, tableName);
    }

    protected UpdateBuilder update(String tableName) {
        return new UpdateBuilder(this, tableName);
    }

    @SuppressWarnings("unused")
    private RuntimeException soften(String query, SQLException e) {
        return ExceptionUtil.softenException(e);
    }

    private void logExecution(String method, String query, long startTime) {
        long executionTime = System.currentTimeMillis()-startTime;
        if (executionTime > 1000) {
            logger.warn("SLOW {} {}: {}ms", method, query, executionTime);
        } else {
            logger.trace("{} {}: {}ms", method, query, executionTime);
        }
    }

}
