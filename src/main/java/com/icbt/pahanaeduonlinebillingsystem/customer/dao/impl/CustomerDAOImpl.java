package com.icbt.pahanaeduonlinebillingsystem.customer.dao.impl;

import com.icbt.pahanaeduonlinebillingsystem.common.util.DAOUtil;
import com.icbt.pahanaeduonlinebillingsystem.customer.dao.CustomerDAO;
import com.icbt.pahanaeduonlinebillingsystem.customer.entity.CustomerEntity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
        return DAOUtil.executeSql(
                connection,
                "UPDATE customers SET name = ?, address = ?, phone = ?, units_consumed = ?, updated_by = ?, updated_at = CURRENT_TIMESTAMP WHERE account_number = ? AND deleted_at IS NULL",
                entity.getName(), entity.getAddress(), entity.getPhone(), entity.getUnitsConsumed(), entity.getUpdatedBy(), entity.getAccountNumber()
        );
    }

    @Override
    public boolean delete(Connection connection, Object... args) throws SQLException, ClassNotFoundException {
        return DAOUtil.executeSql(
                connection,
                "UPDATE customers SET deleted_by = ?, deleted_at = CURRENT_TIMESTAMP WHERE account_number = ? AND deleted_at IS NULL",
                args[0], args[1]
        );
    }

    @Override
    public CustomerEntity searchById(Connection connection, Object... args) throws SQLException, ClassNotFoundException {
        ResultSet rs = DAOUtil.executeSql(
                connection,
                "SELECT * FROM customers WHERE id = ? AND deleted_at IS NULL",
                args[0]
        );
        if (rs.next()) {
            CustomerEntity entity = new CustomerEntity();
            entity.setId(rs.getInt("id"));
            entity.setAccountNumber(rs.getString("account_number"));
            entity.setName(rs.getString("name"));
            entity.setAddress(rs.getString("address"));
            entity.setPhone(rs.getString("phone"));
            entity.setUnitsConsumed(rs.getInt("units_consumed"));
            entity.setCreatedBy(rs.getInt("created_by"));
            entity.setCreatedAt(rs.getTimestamp("created_at"));
            entity.setUpdatedBy(rs.getInt("updated_by"));
            entity.setUpdatedAt(rs.getTimestamp("updated_at"));
            entity.setDeletedBy(rs.getInt("deleted_by"));
            entity.setDeletedAt(rs.getTimestamp("deleted_at"));
            return entity;
        }
        return null;
    }

    @Override
    public List<CustomerEntity> getAll(Connection connection, Map<String, String> searchParams) throws SQLException, ClassNotFoundException {
        ResultSet rs = DAOUtil.executeSql(
                connection,
                "SELECT * FROM customers WHERE deleted_at IS NULL"
        );

        List<CustomerEntity> list = new ArrayList<>();
        while (rs.next()) {
            CustomerEntity entity = new CustomerEntity();
            entity.setId(rs.getInt("id"));
            entity.setAccountNumber(rs.getString("account_number"));
            entity.setName(rs.getString("name"));
            entity.setAddress(rs.getString("address"));
            entity.setPhone(rs.getString("phone"));
            entity.setUnitsConsumed(rs.getInt("units_consumed"));
            list.add(entity);
        }
        return list;
    }

    @Override
    public boolean existsById(Connection connection, Object... args) throws SQLException, ClassNotFoundException {
        ResultSet rs = DAOUtil.executeSql(
                connection,
                "SELECT 1 FROM customers WHERE id = ? AND deleted_at IS NULL",
                args[0]
        );
        return rs.next();
    }

    @Override
    public boolean existsByAccountNumber(Connection connection, Object... args) throws SQLException, ClassNotFoundException {
        ResultSet rs = DAOUtil.executeSql(
                connection,
                "SELECT 1 FROM customers WHERE account_number = ? AND deleted_at IS NULL",
                args[0]
        );
        return rs.next();
    }

    @Override
    public boolean existsByPhoneNumber(Connection connection, Object... args) throws SQLException, ClassNotFoundException {
        ResultSet rs = DAOUtil.executeSql(
                connection,
                "SELECT 1 FROM customers WHERE phone = ? AND deleted_at IS NULL",
                args[0]
        );
        return rs.next();
    }

    @Override
    public CustomerEntity searchByAccountNumber(Connection connection, Object... args) throws SQLException, ClassNotFoundException {
        ResultSet rs = DAOUtil.executeSql(
                connection,
                "SELECT * FROM customers WHERE account_number = ? AND deleted_at IS NULL",
                args[0]
        );
        if (rs.next()) {
            CustomerEntity entity = new CustomerEntity();
            entity.setId(rs.getInt("id"));
            entity.setAccountNumber(rs.getString("account_number"));
            entity.setName(rs.getString("name"));
            entity.setAddress(rs.getString("address"));
            entity.setPhone(rs.getString("phone"));
            entity.setUnitsConsumed(rs.getInt("units_consumed"));
            entity.setCreatedBy(rs.getInt("created_by"));
            entity.setCreatedAt(rs.getTimestamp("created_at"));
            entity.setUpdatedBy(rs.getInt("updated_by"));
            entity.setUpdatedAt(rs.getTimestamp("updated_at"));
            entity.setDeletedBy(rs.getInt("deleted_by"));
            entity.setDeletedAt(rs.getTimestamp("deleted_at"));
            return entity;
        }
        return null;
    }
}
