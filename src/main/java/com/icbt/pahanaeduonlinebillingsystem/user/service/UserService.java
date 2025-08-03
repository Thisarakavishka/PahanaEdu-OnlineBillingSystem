package com.icbt.pahanaeduonlinebillingsystem.user.service;

import com.icbt.pahanaeduonlinebillingsystem.user.dto.UserDTO;
import com.icbt.pahanaeduonlinebillingsystem.common.base.CrudService;

import java.sql.SQLException;

/**
 * @author Thisara Kavishka
 * @date 2025-07-17
 * @since 1.0
 */
public interface UserService extends CrudService<UserDTO> {

    UserDTO authenticate(String username, String password) throws SQLException, ClassNotFoundException;

}
