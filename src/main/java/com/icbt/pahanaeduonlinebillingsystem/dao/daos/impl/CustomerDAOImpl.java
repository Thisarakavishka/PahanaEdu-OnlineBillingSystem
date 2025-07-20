package com.icbt.pahanaeduonlinebillingsystem.dao.daos.impl;

import com.icbt.pahanaeduonlinebillingsystem.dao.DAOUtil;
import com.icbt.pahanaeduonlinebillingsystem.dao.daos.CustomerDAO;
import com.icbt.pahanaeduonlinebillingsystem.entity.CustomerEntity;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author Thisara Kavishka
 * @date 2025-07-16
 * @since 1.0
 */
public class CustomerDAOImpl implements CustomerDAO {

    @Override
    public boolean add(Connection connection, CustomerEntity entity) throws SQLException, ClassNotFoundException {
        return DAOUtil.executeSql(
                connection,
                "INSERT INTO customers (account_number, name, address, phone, units_consumed, created_by) VALUES (?, ?, ?, ?, ?, ?)",
                entity.getAccountNumber(), entity.getName(), entity.getAddress(), entity.getPhone(), entity.getUnitsConsumed(), entity.getCreatedBy()
        );
    }

    @Override
    public boolean update(Connection connection, CustomerEntity entity) throws SQLException, ClassNotFoundException {
        return false;
    }

    @Override
    public boolean delete(Connection connection, Object... args) throws SQLException, ClassNotFoundException {
        return false;
    }

    @Override
    public CustomerEntity searchById(Connection connection, Object... args) throws SQLException, ClassNotFoundException {
        return null;
    }

    @Override
    public List<CustomerEntity> getAll(Connection connection, Map<String, String> searchParams) throws SQLException, ClassNotFoundException {
        return List.of();
    }
}
