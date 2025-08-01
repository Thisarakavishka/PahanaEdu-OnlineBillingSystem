package com.icbt.pahanaeduonlinebillingsystem.common.util;

import com.icbt.pahanaeduonlinebillingsystem.common.exception.ExceptionType;
import com.icbt.pahanaeduonlinebillingsystem.common.exception.PahanaEduOnlineBillingSystemException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Thisara Kavishka
 * @date 2025-07-14
 * @since 1.0
 */
public class DAOUtil {

    private static final Logger LOGGER = LogUtil.getLogger(DAOUtil.class);

    private DAOUtil() {
    }

    // Executes an SQL UPDATE, INSERT, or DELETE statement.
    public static boolean executeUpdate(Connection connection, String sql, Object... args) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) {
                preparedStatement.setObject(i + 1, args[i]);
            }
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL Update/Insert/Delete failed " + sql + "-" + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        }
    }

    // Executes an SQL SELECT statement
    public static ResultSet executeQuery(Connection connection, String sql, Object... args) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            for (int i = 0; i < args.length; i++) {
                preparedStatement.setObject((i + 1), args[i]);
            }
            return preparedStatement.executeQuery();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL Select failed " + sql + "-" + e.getMessage(), e);
            // Close the statement if an error occurs before returning ResultSet
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException exception) {
                    LOGGER.log(Level.WARNING, "Failed to close PreparedStatement after error ", exception);
                }
            }
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        }
    }

    public static <T> T executeSql(Connection connection, String sql, Object... args) throws SQLException {

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            for (int i = 0; i < args.length; i++) {
                preparedStatement.setObject((i + 1), args[i]);
            }

            if (sql.trim().toUpperCase().startsWith("SELECT")) {
                return (T) preparedStatement.executeQuery();
            }

            return (T) ((Boolean) (preparedStatement.executeUpdate() > 0));
        }
    }
}
