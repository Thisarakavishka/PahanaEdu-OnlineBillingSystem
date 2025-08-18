package com.icbt.pahanaeduonlinebillingsystem.customer.dao.impl;

import com.icbt.pahanaeduonlinebillingsystem.common.exception.ExceptionType;
import com.icbt.pahanaeduonlinebillingsystem.common.exception.PahanaEduOnlineBillingSystemException;
import com.icbt.pahanaeduonlinebillingsystem.common.util.DAOUtil;
import com.icbt.pahanaeduonlinebillingsystem.common.util.DBUtil;
import com.icbt.pahanaeduonlinebillingsystem.common.util.LogUtil;
import com.icbt.pahanaeduonlinebillingsystem.customer.dao.CustomerDAO;
import com.icbt.pahanaeduonlinebillingsystem.customer.entity.CustomerEntity;
import com.icbt.pahanaeduonlinebillingsystem.customer.mapper.CustomerMapper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Thisara Kavishka
 * @date 2025-07-16
 * @since 1.0
 */
public class CustomerDAOImpl implements CustomerDAO {

    private static final Logger LOGGER = LogUtil.getLogger(CustomerDAOImpl.class);


    @Override
    public boolean add(Connection connection, CustomerEntity entity) throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO customers (account_number, name, address, phone, units_consumed, created_by) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            return DAOUtil.executeUpdate(connection, sql,
                    entity.getAccountNumber(),
                    entity.getName(),
                    entity.getAddress(),
                    entity.getPhone(),
                    entity.getUnitsConsumed(),
                    entity.getCreatedBy()
            );
        } catch (PahanaEduOnlineBillingSystemException e) {
            LOGGER.log(Level.SEVERE, "Failed to add customer in DAO: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        }
    }

    @Override
    public boolean update(Connection connection, CustomerEntity entity) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE customers SET name = ?, address = ?, phone = ?, units_consumed = ?, updated_by = ?, updated_at = ? WHERE account_number = ? AND deleted_at IS NULL";
        try {
            return DAOUtil.executeUpdate(connection, sql,
                    entity.getName(),
                    entity.getAddress(),
                    entity.getPhone(),
                    entity.getUnitsConsumed(),
                    entity.getUpdatedBy(),
                    new Timestamp(System.currentTimeMillis()),
                    entity.getAccountNumber()
            );
        } catch (PahanaEduOnlineBillingSystemException e) {
            LOGGER.log(Level.SEVERE, "Failed to update customer in DAO: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        }
    }

    @Override
    public boolean delete(Connection connection, Object... args) throws SQLException, ClassNotFoundException {
        if (args.length < 2 || !(args[0] instanceof Integer) || !(args[1] instanceof String)) {
            throw new IllegalArgumentException("Delete requires deleter ID (Integer) and account number (String).");
        }

        Integer deletedBy = (Integer) args[0];
        String accountNumber = (String) args[1];

        String sql = "UPDATE customers SET deleted_by = ?, deleted_at = ? WHERE account_number = ? AND deleted_at IS NULL";
        try {
            return DAOUtil.executeUpdate(connection, sql,
                    deletedBy,
                    new Timestamp(System.currentTimeMillis()),
                    accountNumber
            );
        } catch (PahanaEduOnlineBillingSystemException e) {
            LOGGER.log(Level.SEVERE, "Failed to soft delete customer in DAO: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        }
    }

    @Override
    public CustomerEntity searchById(Connection connection, Object... args) throws SQLException, ClassNotFoundException {
        String sql = "SELECT * FROM customers WHERE id = ? AND deleted_at IS NULL";
        Integer id = (Integer) args[0];
        ResultSet resultSet = null;
        try {
            resultSet = DAOUtil.executeQuery(connection, sql, id);
            if (resultSet.next()) {
                return CustomerMapper.mapResultSetToCustomerEntity(resultSet);
            }
            return null;
        } finally {
            DBUtil.closeResultSet(resultSet);
        }
    }

    @Override
    public List<CustomerEntity> getAll(Connection connection, Map<String, String> searchParams) throws SQLException, ClassNotFoundException {
        List<CustomerEntity> customerEntities = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM customers WHERE deleted_at IS NULL");
        List<Object> params = new ArrayList<>();

        if (searchParams != null) {
            if (searchParams.containsKey("search")) {
                String searchTerm = "%" + searchParams.get("search") + "%";
                sqlBuilder.append(" AND (name LIKE ? OR phone LIKE ? OR account_number LIKE ?)");
                params.add(searchTerm);
                params.add(searchTerm);
                params.add(searchTerm);
            } else {
                if (searchParams.containsKey("name")) {
                    sqlBuilder.append(" AND name LIKE ?");
                    params.add("%" + searchParams.get("name") + "%");
                }
                if (searchParams.containsKey("phone")) {
                    sqlBuilder.append(" AND phone LIKE ?");
                    params.add("%" + searchParams.get("phone") + "%");
                }
                if (searchParams.containsKey("accountNumber")) {
                    sqlBuilder.append(" AND account_number LIKE ?");
                    params.add("%" + searchParams.get("accountNumber") + "%");
                }
            }
        }
        sqlBuilder.append(" ORDER BY name ASC");

        ResultSet resultSet = null;
        try {
            resultSet = DAOUtil.executeQuery(connection, sqlBuilder.toString(), params.toArray());
            while (resultSet.next()) {
                customerEntities.add(CustomerMapper.mapResultSetToCustomerEntity(resultSet));
            }
        } finally {
            DBUtil.closeResultSet(resultSet);
        }
        return customerEntities;
    }

    @Override
    public boolean existsById(Connection connection, Object... args) throws SQLException, ClassNotFoundException {
        String sql = "SELECT 1 FROM customers WHERE id = ? AND deleted_at IS NULL";
        ResultSet resultSet = null;
        try {
            resultSet = DAOUtil.executeQuery(connection, sql, args);
            return resultSet.next();
        } finally {
            DBUtil.closeResultSet(resultSet);
        }
    }

    @Override
    public boolean existsByAccountNumber(Connection connection, Object... args) throws SQLException, ClassNotFoundException {
        String sql = "SELECT 1 FROM customers WHERE account_number = ? AND deleted_at IS NULL";
        String accountNumber = (String) args[0];
        ResultSet resultSet = null;
        try {
            resultSet = DAOUtil.executeQuery(connection, sql, accountNumber);
            return resultSet.next();
        } finally {
            DBUtil.closeResultSet(resultSet);
        }
    }

    @Override
    public boolean existsByPhoneNumber(Connection connection, Object... args) throws SQLException, ClassNotFoundException {
        String sql = "SELECT 1 FROM customers WHERE phone = ? AND deleted_at IS NULL";
        String phoneNumber = (String) args[0];
        ResultSet resultSet = null;
        try {
            resultSet = DAOUtil.executeQuery(connection, sql, phoneNumber);
            return resultSet.next();
        } finally {
            DBUtil.closeResultSet(resultSet);
        }
    }

    @Override
    public CustomerEntity searchByAccountNumber(Connection connection, Object... args) throws SQLException, ClassNotFoundException {
        String sql = "SELECT * FROM customers WHERE account_number = ? AND deleted_at IS NULL";
        String accountNumber = (String) args[0];
        ResultSet resultSet = null;
        try {
            resultSet = DAOUtil.executeQuery(connection, sql, accountNumber);
            if (resultSet.next()) {
                return CustomerMapper.mapResultSetToCustomerEntity(resultSet);
            }
            return null;
        } finally {
            DBUtil.closeResultSet(resultSet);
        }
    }

    @Override
    public CustomerEntity searchByPhone(Connection connection, String phone) throws SQLException {
        String sql = "SELECT * FROM customers WHERE phone = ? AND deleted_at IS NULL";
        ResultSet resultSet = null;
        try {
            resultSet = DAOUtil.executeQuery(connection, sql, phone);
            if (resultSet.next()) {
                return CustomerMapper.mapResultSetToCustomerEntity(resultSet);
            }
            return null;
        } finally {
            DBUtil.closeResultSet(resultSet);
        }
    }

}
