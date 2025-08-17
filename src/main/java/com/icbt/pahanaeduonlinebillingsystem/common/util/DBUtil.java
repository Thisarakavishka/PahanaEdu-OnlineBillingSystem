package com.icbt.pahanaeduonlinebillingsystem.common.util;

import com.icbt.pahanaeduonlinebillingsystem.common.exception.ExceptionType;
import com.icbt.pahanaeduonlinebillingsystem.common.exception.PahanaEduOnlineBillingSystemException;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Thisara Kavishka
 * @date 2025-07-19
 * @since 1.0
 */
public class DBUtil {

    private static final Logger LOGGER = Logger.getLogger(DBUtil.class.getName());

    private static String dbUrl;
    private static String dbUsername;
    private static String dbPassword;

    static {
        try {
            // First, check for CI/CD environment variables set by GitHub Actions
            String ciDbUrl = System.getenv("TEST_DB_URL");
            String ciDbUser = System.getenv("TEST_DB_USER");
            String ciDbPass = System.getenv("TEST_DB_PASSWORD");

            if (ciDbUrl != null && !ciDbUrl.isEmpty()) {
                // --- CI/CD Environment ---
                LOGGER.info("CI environment detected. Using environment variables for database connection.");
                dbUrl = ciDbUrl;
                dbUsername = ciDbUser;
                dbPassword = ciDbPass;
            } else {
                // --- Local Development Environment ---
                LOGGER.info("Local environment detected. Loading database configuration from db.properties.");
                Properties properties = new Properties();
                try (InputStream inputStream = DBUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
                    if (inputStream == null) {
                        throw new PahanaEduOnlineBillingSystemException(ExceptionType.CONFIGURATION_ERROR);
                    }
                    properties.load(inputStream);
                    dbUrl = properties.getProperty("db.url");
                    dbUsername = properties.getProperty("db.username");
                    dbPassword = properties.getProperty("db.password");
                }
            }

            // Register the JDBC driver once
            Class.forName("com.mysql.cj.jdbc.Driver");
            LOGGER.info("JDBC driver loaded successfully.");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Database initialization failed. Check configuration.", e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.CONFIGURATION_ERROR);
        }
    }

    private DBUtil() {
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to establish database connection to: " + dbUrl, e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        }
    }


    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to close database connection", e);
            }
        }
    }

    public static void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
                LOGGER.log(Level.FINE, "ResultSet closed successfully");
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to close ResultSet " + e.getMessage(), e);
            }
        }
    }

    public static void closeStatement(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
                LOGGER.log(Level.FINE, "Statement closed successfully");
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to close Statement " + e.getMessage(), e);
            }
        }
    }

    public static void rollbackConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.getAutoCommit()) {
                    connection.rollback();
                    LOGGER.log(Level.INFO, "Database transaction rolled back successfully");
                } else {
                    LOGGER.log(Level.FINE, "Skipping rollback: Connection is in auto-commit mode.");
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Critical: Failed to rollback database transaction. Data consistency might be affected.", e);
            }
        } else {
            LOGGER.log(Level.FINE, "Skipping rollback: Provided connection is null.");
        }
    }
}
