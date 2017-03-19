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
import no.pharmacy.infrastructure.ExceptionUtil;
import no.pharmacy.order.Reference;

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
            logger.debug("executeUpdate {}: {}ms", query, System.currentTimeMillis()-startTime);
        }
    }

    public long executeInsert(String query, List<Object> parameters) {
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
            logger.debug("executeInsert {}: {}ms", query, System.currentTimeMillis()-startTime);
        }
    }

    private RuntimeException soften(String query, SQLException e) {
        return ExceptionUtil.softenException(e);
    }

    private void setParameters(PreparedStatement stmt, List<Object> parameters) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            Object parameter = parameters.get(i);
            if (parameter instanceof Money) {
                stmt.setBigDecimal(i+1, ((Money)parameter).toBigDecimal());
            } else if (parameter instanceof Reference) {
                stmt.setString(i+1, ((Reference)parameter).getReference());
            } else {
                stmt.setObject(i+1, parameter);
            }
        }
    }

    protected <T> Optional<T> retrieveSingle(String query, List<Object> parameters, ResultSetMapper<T> mapper) {
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
        }
    }

    protected <T> List<T> queryForList(String query, List<Object> parameters, ResultSetMapper<T> mapper) {
        try (Connection conn = dataSource.getConnection()) {
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
        }
    }

    protected LocalDate toLocalDate(Date date) {
        return date != null ? date.toLocalDate() : null;
    }

    protected InsertBuilder insertInto(String tableName) {
        return new InsertBuilder(this, tableName);
    }

    protected UpdateBuilder update(String tableName) {
        return new UpdateBuilder(this, tableName);
    }

}
