package com.icbt.pahanaeduonlinebillingsystem.user.dao;

import com.icbt.pahanaeduonlinebillingsystem.common.base.CrudDAO;
import com.icbt.pahanaeduonlinebillingsystem.user.entity.UserEntity;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Thisara Kavishka
 * @date 2025-07-16
 * @since 1.0
 */
public interface UserDAO extends CrudDAO<UserEntity> {

    UserEntity searchByUsername(Connection connection, String username) throws SQLException, ClassNotFoundException;

    UserEntity authenticateUser(Connection connection, String username, String hashedPassword, String salt) throws SQLException, ClassNotFoundException;

    int getUsersCount(Connection connection) throws SQLException, ClassNotFoundException;

    List<UserEntity> getAllDeletedUsers(Connection connection) throws SQLException;

    boolean restoreUser(Connection connection, int id) throws SQLException;

    UserEntity searchDeletedById(Connection connection, int id) throws SQLException;
}
