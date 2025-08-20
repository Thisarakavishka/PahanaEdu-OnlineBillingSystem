package com.icbt.pahanaeduonlinebillingsystem.user.service;

import com.icbt.pahanaeduonlinebillingsystem.user.dto.UserDTO;
import com.icbt.pahanaeduonlinebillingsystem.common.base.CrudService;

import java.sql.SQLException;
import java.util.List;

/**
 * @author Thisara Kavishka
 * @date 2025-07-17
 * @since 1.0
 */
public interface UserService extends CrudService<UserDTO> {

    UserDTO authenticate(String username, String password) throws SQLException, ClassNotFoundException;

    UserDTO searchByUsername(String username) throws SQLException, ClassNotFoundException;

    List<UserDTO> getAllDeletedUsers() throws SQLException, ClassNotFoundException;

    boolean restoreUser(int id) throws SQLException, ClassNotFoundException;
}
