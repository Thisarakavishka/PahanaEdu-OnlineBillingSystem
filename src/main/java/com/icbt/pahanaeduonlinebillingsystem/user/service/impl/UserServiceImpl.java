package com.icbt.pahanaeduonlinebillingsystem.user.service.impl;

import com.icbt.pahanaeduonlinebillingsystem.user.dao.UserDAO;
import com.icbt.pahanaeduonlinebillingsystem.user.dao.impl.UserDAOImpl;
import com.icbt.pahanaeduonlinebillingsystem.user.dto.UserDTO;
import com.icbt.pahanaeduonlinebillingsystem.user.service.UserService;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author Thisara Kavishka
 * @date 2025-07-17
 * @since 1.0
 */
public class UserServiceImpl implements UserService {
    @Override
    public boolean add(UserDTO dto) throws SQLException, ClassNotFoundException {
        return false;
    }

    @Override
    public boolean update(UserDTO dto) throws SQLException, ClassNotFoundException {
        return false;
    }

    @Override
    public boolean delete(Object... args) throws SQLException, ClassNotFoundException {
        return false;
    }

    @Override
    public UserDTO searchById(Object... args) throws SQLException, ClassNotFoundException {
        return null;
    }

    @Override
    public List<UserDTO> getAll(Map<String, String> searchParams) throws SQLException, ClassNotFoundException {
        return List.of();
    }
}
