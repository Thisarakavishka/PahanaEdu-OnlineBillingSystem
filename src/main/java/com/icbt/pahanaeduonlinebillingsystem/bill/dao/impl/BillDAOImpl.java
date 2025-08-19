package com.icbt.pahanaeduonlinebillingsystem.bill.dao.impl;

import com.icbt.pahanaeduonlinebillingsystem.bill.dao.BillDAO;
import com.icbt.pahanaeduonlinebillingsystem.bill.entity.BillEntity;
import com.icbt.pahanaeduonlinebillingsystem.bill.mapper.BillMapper;
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
    public int addBill(Connection connection, BillEntity entity) throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO bills (customer_id, total_amount, created_by, created_at) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, entity.getCustomerId());
            pst.setBigDecimal(2, entity.getTotalAmount());
            pst.setObject(3, entity.getCreatedBy());
            pst.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            pst.executeUpdate();

            try (ResultSet resultSet = pst.getGeneratedKeys()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        } catch (PahanaEduOnlineBillingSystemException e) {
            LOGGER.log(Level.SEVERE, "Failed to add Bill in DAO: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        }
        return -1;
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
                return BillMapper.mapResultSetToBillEntity(resultSet);
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
        sqlBuilder.append(" ORDER BY id ASC");

        ResultSet resultSet = null;
        try {
            resultSet = DAOUtil.executeQuery(connection, sqlBuilder.toString(), params.toArray());
            while (resultSet.next()) {
                bills.add(BillMapper.mapResultSetToBillEntity(resultSet));
            }
        } finally {
            DBUtil.closeResultSet(resultSet);
        }
        return bills;
    }

    @Override
    public int getBillsCount(Connection connection) throws SQLException, ClassNotFoundException {
        String sql = "SELECT COUNT(id) FROM bills WHERE deleted_at IS NULL";
        ResultSet resultSet = null;
        try {
            resultSet = DAOUtil.executeQuery(connection, sql);
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;
        } finally {
            DBUtil.closeResultSet(resultSet);
        }
    }
}
