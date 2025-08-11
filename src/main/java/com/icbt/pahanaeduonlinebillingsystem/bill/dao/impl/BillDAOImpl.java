package com.icbt.pahanaeduonlinebillingsystem.bill.dao.impl;

import com.icbt.pahanaeduonlinebillingsystem.bill.dao.BillDAO;
import com.icbt.pahanaeduonlinebillingsystem.bill.entity.BillEntity;
import com.icbt.pahanaeduonlinebillingsystem.common.exception.ExceptionType;
import com.icbt.pahanaeduonlinebillingsystem.common.exception.PahanaEduOnlineBillingSystemException;
import com.icbt.pahanaeduonlinebillingsystem.common.util.DAOUtil;
import com.icbt.pahanaeduonlinebillingsystem.common.util.DBUtil;
import com.icbt.pahanaeduonlinebillingsystem.common.util.LogUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Thisara Kavishka
 * @date 2025-08-11
 * @since 1.0
 */
public class BillDAOImpl implements BillDAO {

    private static final Logger LOGGER = LogUtil.getLogger(BillDAOImpl.class);

    @Override
    public boolean add(Connection connection, BillEntity entity) throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO bills (customer_id, total_amount, created_by, created_at) VALUES (?, ?, ?, ?)";
        try {
            return DAOUtil.executeUpdate(connection, sql,
                    entity.getCustomerId(),
                    entity.getTotalAmount(),
                    entity.getCreatedBy(),
                    new Timestamp(System.currentTimeMillis())
            );
        } catch (PahanaEduOnlineBillingSystemException e) {
            LOGGER.log(Level.SEVERE, "Failed to add Bill in DAO: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        }
    }

    @Override
    public boolean update(Connection connection, BillEntity entity) throws SQLException, ClassNotFoundException {
        throw new UnsupportedOperationException("Update method not implemented yet");
    }

    @Override
    public boolean delete(Connection connection, Object... args) throws SQLException, ClassNotFoundException {
        if (args.length < 2 || !(args[0] instanceof Integer) || !(args[1] instanceof Integer)) {
            throw new IllegalArgumentException("Delete requires deleter ID (Integer) and bill ID (Integer).");
        }
        Integer deletedBy = (Integer) args[0];
        Integer billId = (Integer) args[1];

        String sql = "UPDATE bills SET deleted_by = ?, deleted_at = ? WHERE id = ? AND deleted_at IS NULL";
        try {
            return DAOUtil.executeUpdate(connection, sql,
                    deletedBy,
                    new Timestamp(System.currentTimeMillis()),
                    billId
            );
        } catch (PahanaEduOnlineBillingSystemException e) {
            LOGGER.log(Level.SEVERE, "Failed to soft delete bill in DAO: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        }
    }

    @Override
    public BillEntity searchById(Connection connection, Object... args) throws SQLException, ClassNotFoundException {
        String sql = "SELECT * FROM bills WHERE id = ? AND deleted_at IS NULL";
        Integer billId = (Integer) args[0];
        ResultSet resultSet = null;
        try {
            resultSet = DAOUtil.executeQuery(connection, sql, billId);
            if (resultSet.next()) {
                return mapResultSetToBillEntity(resultSet);
            }
            return null;
        } finally {
            DBUtil.closeResultSet(resultSet);
        }
    }

    @Override
    public List<BillEntity> getAll(Connection connection, Map<String, String> searchParams) throws SQLException, ClassNotFoundException {
        List<BillEntity> bills = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM bills WHERE deleted_at IS NULL");
        List<Object> params = new ArrayList<>();

        if (searchParams != null && searchParams.containsKey("search")) {
            String searchTerm = "%" + searchParams.get("search") + "%";
            sqlBuilder.append(" AND customer_id IN (SELECT id FROM customers WHERE name LIKE ? OR account_number LIKE ?)");
            params.add(searchTerm);
            params.add(searchTerm);
        }
        sqlBuilder.append(" ORDER BY name ASC");

        ResultSet resultSet = null;
        try {
            resultSet = DAOUtil.executeQuery(connection, sqlBuilder.toString(), params.toArray());
            while (resultSet.next()) {
                bills.add(mapResultSetToBillEntity(resultSet));
            }
        } finally {
            DBUtil.closeResultSet(resultSet);
        }
        return bills;
    }

    private BillEntity mapResultSetToBillEntity(ResultSet resultSet) throws SQLException {
        BillEntity entity = new BillEntity();
        entity.setId(resultSet.getInt("id"));
        entity.setCustomerId(resultSet.getInt("customer_id"));
        entity.setTotalAmount(resultSet.getBigDecimal("total_amount"));
        entity.setCreatedBy(resultSet.getObject("created_by", Integer.class));
        entity.setCreatedAt(resultSet.getTimestamp("created_at"));
        entity.setDeletedBy(resultSet.getObject("deleted_by", Integer.class));
        entity.setDeletedAt(resultSet.getTimestamp("deleted_at"));
        return entity;
    }
}
