package com.icbt.pahanaeduonlinebillingsystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author Thisara Kavishka
 * @date 2025-07-14
 * @since 1.0
 */
public class DAOUtil {

    public static <T> T executeSql(Connection connection, String sql, Object... args) throws SQLException{

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
