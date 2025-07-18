package com.icbt.pahanaeduonlinebillingsystem.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Thisara Kavishka
 * @date 2025-07-19
 * @since 1.0
 */
public class DBUtil {

    private static final String USERNAME;
    private static final String PASSWORD;
    private static final String URL;
    private static final String DRIVER;
    private static final Logger logger = Logger.getLogger(DBUtil.class.getName());

    private DBUtil() {
    }

    static {
        try (InputStream inputStream = DBUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            Properties properties = new Properties();
            if (inputStream == null) {
                logger.log(Level.SEVERE, "Failed to load properties file (Unable to find db.properties)");
                throw new RuntimeException("Failed to load properties file (Unable to find db.properties)");
            }
            properties.load(inputStream);
            USERNAME = properties.getProperty("db.username");
            PASSWORD = properties.getProperty("db.password");
            URL = properties.getProperty("db.url");
            DRIVER = properties.getProperty("db.driver");

            Class.forName(DRIVER);
            logger.log(Level.INFO, "Database connection established successfully");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load properties file (Unable to find db.properties)", e);
            throw new RuntimeException("Failed to load properties file (Unable to find db.properties)");
        }
    }

    public static Connection getConnection() {
        try {
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            logger.log(Level.FINE, "Database connection established successfully");
            return connection;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to connect to database", e);
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                logger.log(Level.INFO, "Database connection closed successfully");
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to close database connection", e);
            }
        }
    }
}
