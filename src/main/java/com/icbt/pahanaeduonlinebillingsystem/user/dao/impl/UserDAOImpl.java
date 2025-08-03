package com.icbt.pahanaeduonlinebillingsystem.user.dao.impl;

import com.icbt.pahanaeduonlinebillingsystem.common.constant.Role;
import com.icbt.pahanaeduonlinebillingsystem.common.exception.ExceptionType;
import com.icbt.pahanaeduonlinebillingsystem.common.exception.PahanaEduOnlineBillingSystemException;
import com.icbt.pahanaeduonlinebillingsystem.common.util.DAOUtil;
import com.icbt.pahanaeduonlinebillingsystem.common.util.DBUtil;
import com.icbt.pahanaeduonlinebillingsystem.user.dao.UserDAO;
import com.icbt.pahanaeduonlinebillingsystem.user.entity.UserEntity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
                return mapResultSetToUserEntity(resultSet);
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
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.USER_CREATE_FAILED);
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
        if (args.length < 1 || !(args[0] instanceof Integer)) {
            throw new IllegalArgumentException("Search requires user ID");
        }
        Integer id = (Integer) args[0];
        String sql = "SELECT * FROM users WHERE id = ? AND deleted_at IS NULL";
        ResultSet resultSet = null;
        try {
            resultSet = DAOUtil.executeQuery(connection, sql, id);
            if (resultSet.next()) {
                return mapResultSetToUserEntity(resultSet);
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

        // Add search parameters
        if (searchParams != null && searchParams.containsKey("username")) {
            sqlBuilder.append(" AND username LIKE ?");
            params.add("%" + searchParams.get("username") + "%");
        }

        if (searchParams != null && searchParams.containsKey("role")) {
            sqlBuilder.append(" AND role = ?");
            params.add(searchParams.get("role").toUpperCase());
        }

        sqlBuilder.append(" ORDER BY username ASC");

        ResultSet resultSet = null;
        try {
            resultSet = DAOUtil.executeQuery(connection, sqlBuilder.toString(), params.toArray());
            while (resultSet.next()) {
                users.add(mapResultSetToUserEntity(resultSet));
            }
        } finally {
            DBUtil.closeResultSet(resultSet);
        }
        return users;
    }

    private UserEntity mapResultSetToUserEntity(ResultSet rs) throws SQLException {
        UserEntity user = new UserEntity();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password")); // Hashed password
        user.setSalt(rs.getString("salt"));
        user.setRole(Role.valueOf(rs.getString("role"))); // Convert String to Enum

        // Audit fields
        user.setCreatedBy(rs.getObject("created_by", Integer.class)); // Handles NULL
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setUpdatedBy(rs.getObject("updated_by", Integer.class));
        user.setUpdatedAt(rs.getTimestamp("updated_at"));
        user.setDeletedBy(rs.getObject("deleted_by", Integer.class));
        user.setDeletedAt(rs.getTimestamp("deleted_at"));
        return user;
    }
}
