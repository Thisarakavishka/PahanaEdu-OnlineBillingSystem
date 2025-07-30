package com.icbt.pahanaeduonlinebillingsystem.user.dao;

import com.icbt.pahanaeduonlinebillingsystem.common.base.CrudDAO;
import com.icbt.pahanaeduonlinebillingsystem.user.entity.UserEntity;

import java.sql.Connection;

/**
 * @author Thisara Kavishka
 * @date 2025-07-16
 * @since 1.0
 */
public interface UserDAO extends CrudDAO<UserEntity> {

    UserEntity searchByUsername(Connection connection, String username) throws Exception;

}
