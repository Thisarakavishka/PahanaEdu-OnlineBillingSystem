package com.icbt.pahanaeduonlinebillingsystem.user.dao.impl;

import com.icbt.pahanaeduonlinebillingsystem.common.exception.ExceptionType;
import com.icbt.pahanaeduonlinebillingsystem.common.exception.PahanaEduOnlineBillingSystemException;
import com.icbt.pahanaeduonlinebillingsystem.common.util.DAOUtil;
import com.icbt.pahanaeduonlinebillingsystem.common.util.DBUtil;
import com.icbt.pahanaeduonlinebillingsystem.user.converter.UserMapper;
import com.icbt.pahanaeduonlinebillingsystem.user.dao.UserDAO;
import com.icbt.pahanaeduonlinebillingsystem.user.entity.UserEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Thisara Kavishka
 * @date 2025-07-16
 * @since 1.0
 */
public class UserDAOImpl implements UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAOImpl.class.getName());

    @Override
    public UserEntity searchByUsername(Connection connection, String username) throws SQLException, ClassNotFoundException {
        String sql = "SELECT * FROM users WHERE username = ? AND deleted_at IS NULL";
        ResultSet resultSet = null;
        try {
            resultSet = DAOUtil.executeQuery(connection, sql, username);
            if (resultSet.next()) {
                return UserMapper.mapResultSetToUserEntity(resultSet);
            }
            return null;
        } finally {
            DBUtil.closeResultSet(resultSet);
        }
    }

    @Override
    public UserEntity authenticateUser(Connection connection, String username, String hashedPassword, String salt) throws SQLException, ClassNotFoundException {
        return searchByUsername(connection, username);
    }

    @Override
    public int getUsersCount(Connection connection) throws SQLException, ClassNotFoundException {
        String sql = "SELECT COUNT(id) FROM users WHERE deleted_at IS NULL";
        ResultSet resultSet = null;
        try {
            resultSet = DAOUtil.executeQuery(connection, sql);
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;
        } finally {
            DBUtil.closeResultSet(resultSet);
        }
    }

    @Override
    public boolean add(Connection connection, UserEntity entity) throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO users (username, password, salt, role, created_by, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            return DAOUtil.executeUpdate(connection, sql,
                    entity.getUsername(),
                    entity.getPassword(),
                    entity.getSalt(),
                    entity.getRole().name(),
                    entity.getCreatedBy(),
                    new Timestamp(System.currentTimeMillis())
            );
        } catch (PahanaEduOnlineBillingSystemException e) {
            LOGGER.log(Level.SEVERE, "Failed to add user: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.USER_CREATION_FAILED);
        }
    }

    @Override
    public boolean update(Connection connection, UserEntity entity) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE users SET username = ?, password = ?, salt = ?, role = ?, updated_by = ?, updated_at = ? WHERE id = ? AND deleted_at IS NULL";
        try {
            return DAOUtil.executeUpdate(connection, sql,
                    entity.getUsername(),
                    entity.getPassword(),
                    entity.getSalt(),
                    entity.getRole().name(),
                    entity.getUpdatedBy(),
                    new Timestamp(System.currentTimeMillis()),
                    entity.getId()
            );
        } catch (PahanaEduOnlineBillingSystemException e) {
            LOGGER.log(Level.SEVERE, "Failed to update user: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.USER_UPDATE_FAILED);
        }
    }

    @Override
    public boolean delete(Connection connection, Object... args) throws SQLException, ClassNotFoundException {
        if (args.length < 2 || !(args[0] instanceof Integer) || !(args[1] instanceof Integer)) {
            throw new IllegalArgumentException("Delete requires user ID and deleter ID");
        }
        Integer userId = (Integer) args[0];
        Integer deletedBy = (Integer) args[1];

        // soft delete
        String sql = "UPDATE users SET deleted_at = ?, deleted_by = ? WHERE id = ? AND deleted_at IS NULL";
        try {
            return DAOUtil.executeUpdate(connection, sql,
                    new Timestamp(System.currentTimeMillis()),
                    deletedBy,
                    userId
            );
        } catch (PahanaEduOnlineBillingSystemException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete user: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.USER_DELETION_FAILED);
        }
    }

    @Override
    public UserEntity searchById(Connection connection, Object... args) throws SQLException, ClassNotFoundException {
        String sql = "SELECT * FROM users WHERE id = ? AND deleted_at IS NULL";
        Integer userId = (Integer) args[0];
        ResultSet resultSet = null;
        try {
            resultSet = DAOUtil.executeQuery(connection, sql, userId);
            if (resultSet.next()) {
                return UserMapper.mapResultSetToUserEntity(resultSet);
            }
            return null;
        } finally {
            DBUtil.closeResultSet(resultSet);
        }
    }

    @Override
    public List<UserEntity> getAll(Connection connection, Map<String, String> searchParams) throws SQLException, ClassNotFoundException {
        List<UserEntity> users = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM users WHERE deleted_at IS NULL");
        List<Object> params = new ArrayList<>();

        if (searchParams != null) {
            // Prioritize generic 'search' term
            if (searchParams.containsKey("search")) {
                String searchTerm = "%" + searchParams.get("search") + "%";
                sqlBuilder.append(" AND (username LIKE ? OR role LIKE ?)"); // Assuming search applies to username and role
                params.add(searchTerm);
                params.add(searchTerm);
            } else {
                // Specific search parameters if no generic search term is provided
                if (searchParams.containsKey("username")) {
                    sqlBuilder.append(" AND username LIKE ?");
                    params.add("%" + searchParams.get("username") + "%");
                }
                if (searchParams.containsKey("role")) {
                    sqlBuilder.append(" AND role = ?");
                    params.add(searchParams.get("role").toUpperCase());
                }
            }
        }
        sqlBuilder.append(" ORDER BY id ASC");

        ResultSet resultSet = null;
        try {
            resultSet = DAOUtil.executeQuery(connection, sqlBuilder.toString(), params.toArray());
            while (resultSet.next()) {
                users.add(UserMapper.mapResultSetToUserEntity(resultSet));
            }
        } finally {
            DBUtil.closeResultSet(resultSet);
        }
        return users;
    }

    @Override
    public List<UserEntity> getAllDeletedUsers(Connection connection) throws SQLException {
        List<UserEntity> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE deleted_at IS NOT NULL";
        try (PreparedStatement pst = connection.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                users.add(UserMapper.mapResultSetToUserEntity(rs));
            }
        }
        return users;
    }

    @Override
    public boolean restoreUser(Connection connection, int id) throws SQLException {
        String sql = "UPDATE users SET deleted_at = NULL, deleted_by = NULL WHERE id = ?";
        try {
            return DAOUtil.executeUpdate(connection, sql, id);
        } catch (PahanaEduOnlineBillingSystemException e) {
            throw new SQLException("Failed to restore user.", e);
        }
    }

    @Override
    public UserEntity searchDeletedById(Connection connection, int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ? AND deleted_at IS NOT NULL";
        ResultSet resultSet = null;
        try {
            resultSet = DAOUtil.executeQuery(connection, sql, id);
            if (resultSet.next()) {
                return UserMapper.mapResultSetToUserEntity(resultSet);
            }
            return null;
        } catch (PahanaEduOnlineBillingSystemException e) {
            throw new SQLException("DAO utility failed during searchDeletedById", e);
        } finally {
            DBUtil.closeResultSet(resultSet);
        }
    }
}
