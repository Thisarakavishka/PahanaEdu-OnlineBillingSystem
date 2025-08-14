package com.icbt.pahanaeduonlinebillingsystem.customer.dao;

import com.icbt.pahanaeduonlinebillingsystem.common.base.CrudDAO;
import com.icbt.pahanaeduonlinebillingsystem.customer.entity.CustomerEntity;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Thisara Kavishka
 * @date 2025-07-16
 * @since 1.0
 */
public interface CustomerDAO extends CrudDAO<CustomerEntity> {

    boolean existsById(Connection connection, Object... args) throws SQLException, ClassNotFoundException;

    boolean existsByAccountNumber(Connection connection, Object... args) throws SQLException, ClassNotFoundException;

    boolean existsByPhoneNumber(Connection connection, Object... args) throws SQLException, ClassNotFoundException;

    CustomerEntity searchByAccountNumber(Connection connection, Object... args) throws SQLException, ClassNotFoundException;

    CustomerEntity searchByPhone(Connection connection, String phone) throws SQLException, ClassNotFoundException;
}
