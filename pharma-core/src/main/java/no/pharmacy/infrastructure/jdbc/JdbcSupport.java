package no.pharmacy.infrastructure.jdbc;

import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.pharmacy.core.Money;
import no.pharmacy.core.Reference;
import no.pharmacy.infrastructure.ExceptionUtil;

public class JdbcSupport {

    private static final Logger logger = LoggerFactory.getLogger(JdbcSupport.class);

    private final DataSource dataSource;

    private SecretKey secretKey;

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
            logExecution("executeUpdate", query, startTime, parameters);
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
            setParameter(stmt, i, parameters.get(i));
        }
    }

    private void setParameter(PreparedStatement stmt, int i, Object parameter) throws SQLException {
        if (parameter instanceof Money) {
            stmt.setBigDecimal(i+1, ((Money)parameter).toBigDecimal());
        } else if (parameter instanceof Reference) {
            stmt.setString(i+1, ((Reference)parameter).getReference());
        } else if (parameter instanceof Enum<?>) {
            stmt.setString(i+1, ((Enum<?>)parameter).name());
        } else if (parameter instanceof ZonedDateTime) {
            stmt.setTimestamp(i+1, new Timestamp(((ZonedDateTime)parameter).toInstant().toEpochMilli()));
        } else if (parameter instanceof Instant) {
            stmt.setTimestamp(i+1, new Timestamp(((Instant)parameter).toEpochMilli()));
        } else if (parameter instanceof Optional) {
            setParameter(stmt, i, ((Optional<?>)parameter).orElse(null));
        } else {
            stmt.setObject(i+1, parameter);
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
            logExecution("retrieveSingle", query, startTime, parameters);
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
            logExecution("queryForResultSet", query, startTime, parameters);
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
            logExecution("queryForList", query, startTime, parameters);
        }
    }

    protected LocalDate toLocalDate(Date date) {
        return date != null ? date.toLocalDate() : null;
    }

    public InsertBuilder insertInto(String tableName) {
        return new InsertBuilder(this, tableName);
    }

    public UpdateBuilder update(String tableName) {
        return new UpdateBuilder(this, tableName);
    }

    @SuppressWarnings("unused")
    private RuntimeException soften(String query, SQLException e) {
        return ExceptionUtil.softenException(e);
    }

    private void logExecution(String method, String query, long startTime, List<Object> parameters) {
        long executionTime = System.currentTimeMillis()-startTime;
        if (executionTime > 1000) {
            logger.warn("SLOW {} {}: {}ms {}", method, query, executionTime, parameters);
        } else {
            logger.trace("{} {}: {}ms", method, query, executionTime);
        }
    }

    // TODO: The encryption is very weak. Can you see why? Can you fix it? :-)
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return null;
        }
        if (secretKey == null) {
            throw new IllegalStateException("Initialize " + this + " with setSecretKey to support encrypted columns");
        }
        try {
            Cipher encryptCipher = Cipher.getInstance("AES");
            encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(encryptCipher.doFinal(plainText.getBytes()));
        } catch (GeneralSecurityException e) {
            throw ExceptionUtil.softenException(e);
        }
    }

    public String decrypt(String cryptoText) {
        if (secretKey == null) {
            throw new IllegalStateException("Initialize " + this + " with setSecretKey to support encrypted columns");
        }
        try {
            Cipher encryptCipher = Cipher.getInstance("AES");
            encryptCipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(encryptCipher.doFinal(Base64.getDecoder().decode(cryptoText.getBytes())));
        } catch (GeneralSecurityException e) {
            throw ExceptionUtil.softenException(e);
        }
    }

    public void setSecretKey(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    public SelectBuilder selectStarFrom(String tableName) {
        return new SelectBuilder(this, tableName);
    }

}
