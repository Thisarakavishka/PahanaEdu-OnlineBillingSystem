package com.icbt.pahanaeduonlinebillingsystem.user.dao.impl;

import com.icbt.pahanaeduonlinebillingsystem.user.dao.UserDAO;
import com.icbt.pahanaeduonlinebillingsystem.user.entity.UserEntity;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author Thisara Kavishka
 * @date 2025-07-16
 * @since 1.0
 */
public class UserDAOImpl implements UserDAO {

    @Override
    public boolean add(Connection connection, UserEntity entity) throws SQLException, ClassNotFoundException {
        return false;
    }

    @Override
    public boolean update(Connection connection, UserEntity entity) throws SQLException, ClassNotFoundException {
        return false;
    }

    @Override
    public boolean delete(Connection connection, Object... args) throws SQLException, ClassNotFoundException {
        return false;
    }

    @Override
    public UserEntity searchById(Connection connection, Object... args) throws SQLException, ClassNotFoundException {
        return null;
    }

    @Override
    public List<UserEntity> getAll(Connection connection, Map<String, String> searchParams) throws SQLException, ClassNotFoundException {
        return List.of();
    }
}
