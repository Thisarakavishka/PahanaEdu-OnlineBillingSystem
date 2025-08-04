package com.icbt.pahanaeduonlinebillingsystem.user.service.impl;

import com.icbt.pahanaeduonlinebillingsystem.common.exception.ExceptionType;
import com.icbt.pahanaeduonlinebillingsystem.common.exception.PahanaEduOnlineBillingSystemException;
import com.icbt.pahanaeduonlinebillingsystem.common.util.DBUtil;
import com.icbt.pahanaeduonlinebillingsystem.common.util.LogUtil;
import com.icbt.pahanaeduonlinebillingsystem.common.util.PasswordUtil;
import com.icbt.pahanaeduonlinebillingsystem.user.converter.UserConverter;
import com.icbt.pahanaeduonlinebillingsystem.user.dao.UserDAO;
import com.icbt.pahanaeduonlinebillingsystem.user.dao.impl.UserDAOImpl;
import com.icbt.pahanaeduonlinebillingsystem.user.dto.UserDTO;
import com.icbt.pahanaeduonlinebillingsystem.user.entity.UserEntity;
import com.icbt.pahanaeduonlinebillingsystem.user.service.UserService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Thisara Kavishka
 * @date 2025-07-17
 * @since 1.0
 */
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LogUtil.getLogger(UserServiceImpl.class);
    private final UserDAO userDAO;
    private static final int INITIAL_ADMIN_ID = 1;

    public UserServiceImpl() {
        this.userDAO = new UserDAOImpl();
    }

    @Override
    public UserDTO authenticate(String username, String password) throws SQLException, ClassNotFoundException {
        Connection connection = null;
        try {
            connection = DBUtil.getConnection();
            UserEntity userEntity = userDAO.searchByUsername(connection, username);

            if (userEntity == null) {
                LOGGER.log(Level.WARNING, "Authentication failed: User not found for username: " + username);
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.INVALID_CREDENTIALS);
            }

            if (PasswordUtil.verifyPassword(password, userEntity.getPassword(), userEntity.getSalt())) {
                LOGGER.log(Level.INFO, "User authenticated successfully: " + username);
                return UserConverter.toDto(userEntity);
            } else {
                LOGGER.log(Level.WARNING, "Authentication failed: Invalid password for username: " + username);
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.INVALID_CREDENTIALS);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during authentication for username: " + username + " - " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        } finally {
            DBUtil.closeConnection(connection);
        }
    }

    @Override
    public boolean add(UserDTO dto) throws SQLException, ClassNotFoundException {
        Connection connection = null;
        try {
            connection = DBUtil.getConnection();
            connection.setAutoCommit(false);

            if (userDAO.searchByUsername(connection, dto.getUsername()) != null) {
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.USER_ALREADY_EXISTS);
            }

            String salt = PasswordUtil.generateSalt();
            String hashedPassword = PasswordUtil.hashPassword(dto.getPassword(), salt);

            dto.setSalt(salt);
            dto.setPassword(hashedPassword);

            UserEntity userEntity = UserConverter.toEntity(dto);
            boolean isUserAddedSuccess = userDAO.add(connection, userEntity);
            if (isUserAddedSuccess) {
                connection.commit();
                LOGGER.log(Level.INFO, "User added successfully: " + dto.getUsername());
            } else {
                connection.rollback();
                LOGGER.log(Level.WARNING, "Failed to add user: " + dto.getUsername() + ", rolling back.");
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.USER_CREATE_FAILED);
            }
            return isUserAddedSuccess;
        } catch (SQLException e) {
            DBUtil.rollbackConnection(connection);
            LOGGER.log(Level.SEVERE, "Database error during user add: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        } finally {
            DBUtil.closeConnection(connection);
        }

    }

    @Override
    public boolean update(UserDTO dto) throws SQLException, ClassNotFoundException {
        Connection connection = null;
        try {
            connection = DBUtil.getConnection();
            connection.setAutoCommit(false);

            UserEntity existingUser = userDAO.searchById(connection, dto.getId());
            if (existingUser == null) {
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.USER_NOT_FOUND);
            }

            if (dto.getPassword() != null && !dto.getPassword().isEmpty() && !PasswordUtil.verifyPassword(dto.getPassword(), existingUser.getPassword(), existingUser.getSalt())) {
                String newSalt = PasswordUtil.generateSalt();
                String hashedPassword = PasswordUtil.hashPassword(dto.getPassword(), newSalt);
                dto.setSalt(newSalt);
                dto.setPassword(hashedPassword);
            } else {
                dto.setSalt(existingUser.getSalt());
                dto.setPassword(existingUser.getPassword());
            }

            UserEntity userEntity = UserConverter.toEntity(dto);
            boolean isUserUpdatedSuccess = userDAO.update(connection, userEntity);
            if (isUserUpdatedSuccess) {
                connection.commit();
                LOGGER.log(Level.INFO, "User updated successfully: " + dto.getUsername());
            } else {
                connection.rollback();
                LOGGER.log(Level.WARNING, "Failed to update user: " + dto.getUsername() + ", rolling back.");
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.USER_UPDATE_FAILED);
            }
            return isUserUpdatedSuccess;
        } catch (SQLException e) {
            DBUtil.rollbackConnection(connection);
            LOGGER.log(Level.SEVERE, "Database error during user update: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        } finally {
            DBUtil.closeConnection(connection);
        }
    }

    @Override
    public boolean delete(Object... args) throws SQLException, ClassNotFoundException {
        if (args.length < 2 || !(args[0] instanceof Integer) || !(args[1] instanceof Integer)) {
            throw new IllegalArgumentException("Delete requires user ID and deleter ID.");
        }

        Integer userIdToDelete = (Integer) args[0];
        Integer deletedByUserId = (Integer) args[1];

        // Stop deletion of the initial admin user
        if (userIdToDelete.equals(INITIAL_ADMIN_ID)) {
            LOGGER.log(Level.WARNING, "Attempt to delete initial admin user (ID: " + INITIAL_ADMIN_ID + ") by user ID: " + deletedByUserId + " was blocked.");
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.UNAUTHORIZED_ACCESS);
        }

        Connection connection = null;
        try {
            connection = DBUtil.getConnection();
            connection.setAutoCommit(false);

            UserEntity userEntity = userDAO.searchById(connection, userIdToDelete);
            if (userEntity == null) {
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.USER_NOT_FOUND);
            }

            boolean isUserDeletedSuccess = userDAO.delete(connection, userIdToDelete, deletedByUserId);
            if (isUserDeletedSuccess) {
                connection.commit();
                LOGGER.log(Level.INFO, "User soft deleted successfully: ID " + userIdToDelete + " by user ID " + deletedByUserId);
            } else {
                connection.rollback();
                LOGGER.log(Level.WARNING, "Failed to soft delete user: ID " + userIdToDelete + ", rolling back.");
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.USER_DELETION_FAILED);
            }
            return isUserDeletedSuccess;
        } catch (SQLException e) {
            DBUtil.rollbackConnection(connection);
            LOGGER.log(Level.SEVERE, "Database error during user soft delete: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        } finally {
            DBUtil.closeConnection(connection);
        }
    }

    @Override
    public UserDTO searchById(Object... args) throws SQLException, ClassNotFoundException {
        Connection connection = null;
        try {
            connection = DBUtil.getConnection();
            UserEntity userEntity = userDAO.searchById(connection, args);
            return UserConverter.toDto(userEntity);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during search userById: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        } finally {
            DBUtil.closeConnection(connection);
        }
    }

    @Override
    public List<UserDTO> getAll(Map<String, String> searchParams) throws SQLException, ClassNotFoundException {
        Connection connection = null;
        try {
            connection = DBUtil.getConnection();
            List<UserEntity> userEntities = userDAO.getAll(connection, searchParams);
            return UserConverter.toDTOList(userEntities);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during getAll users: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        } finally {
            DBUtil.closeConnection(connection);
        }
    }
}
