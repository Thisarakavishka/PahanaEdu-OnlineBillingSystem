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

    private static final String USERNAME;
    private static final String PASSWORD;
    private static final String URL;
    private static final String DRIVER;

    static {
        Properties properties = new Properties();
        try (InputStream inputStream = DBUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (inputStream == null) {
                // Log and throw an exception if the properties file is missing.
                LOGGER.log(Level.SEVERE, "Missing db.properties file in classpath");
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.CONFIGURATION_ERROR);
            }
            properties.load(inputStream);
            USERNAME = properties.getProperty("db.username");
            PASSWORD = properties.getProperty("db.password");
            URL = properties.getProperty("db.url");
            DRIVER = properties.getProperty("db.driver");

            // Register the JDBC driver.
            Class.forName(DRIVER);
            LOGGER.log(Level.INFO, "JDBC driver '" + DRIVER + "' loaded successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Database initialization failed. Check db.properties or JDBC driver.", e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.CONFIGURATION_ERROR);
        }
    }

    private DBUtil() {
    }

    public static Connection getConnection() {
        try {
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            LOGGER.log(Level.FINE, "Database connection established to URL: " + URL + " successfully");
            return connection;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to connect to database at URL: " + URL, e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        }
    }


    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                LOGGER.log(Level.INFO, "Database connection closed successfully");
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
