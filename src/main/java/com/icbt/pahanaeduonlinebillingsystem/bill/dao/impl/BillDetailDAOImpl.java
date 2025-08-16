package com.icbt.pahanaeduonlinebillingsystem.bill.dao.impl;

import com.icbt.pahanaeduonlinebillingsystem.bill.dao.BillDetailDAO;
import com.icbt.pahanaeduonlinebillingsystem.bill.entity.BillDetailEntity;
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
public class BillDetailDAOImpl implements BillDetailDAO {

    private static final Logger LOGGER = LogUtil.getLogger(BillDetailDAOImpl.class);

    @Override
    public boolean add(Connection connection, BillDetailEntity entity) throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO bill_details (bill_id, item_id, item_name_at_sale, unit_price_at_sale, units, total, created_by, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            return DAOUtil.executeUpdate(connection, sql,
                    entity.getBillId(),
                    entity.getId(),
                    entity.getItemNameAtSale(),
                    entity.getUnitPriceAtSale(),
                    entity.getUnits(),
                    entity.getTotal(),
                    entity.getCreatedBy(),
                    new Timestamp(System.currentTimeMillis())
            );
        } catch (PahanaEduOnlineBillingSystemException e) {
            LOGGER.log(Level.SEVERE, "Failed to add bill details in DAO: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        }
    }

    @Override
    public void addBillDetails(Connection connection, List<BillDetailEntity> details) throws SQLException {
        String sql = "INSERT INTO bill_details (bill_id, item_id, item_name_at_sale, unit_price_at_sale, units, total, created_by, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            for (BillDetailEntity detail : details) {
                pst.setInt(1, detail.getBillId());
                pst.setInt(2, detail.getItemId());
                pst.setString(3, detail.getItemNameAtSale());
                pst.setBigDecimal(4, detail.getUnitPriceAtSale());
                pst.setInt(5, detail.getUnits());
                pst.setBigDecimal(6, detail.getTotal());
                pst.setObject(7, detail.getCreatedBy());
                pst.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
                pst.addBatch();
            }
            pst.executeBatch();
        } catch (PahanaEduOnlineBillingSystemException e) {
            LOGGER.log(Level.SEVERE, "Failed to add bill details in DAO: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        }
    }

    @Override
    public boolean update(Connection connection, BillDetailEntity entity) throws SQLException, ClassNotFoundException {
        throw new UnsupportedOperationException("Update method not implemented yet");
    }

    @Override
    public boolean delete(Connection connection, Object... args) throws SQLException, ClassNotFoundException {
        throw new UnsupportedOperationException("Delete method not implemented yet");

    }

    @Override
    public BillDetailEntity searchById(Connection connection, Object... args) throws SQLException, ClassNotFoundException {
        throw new UnsupportedOperationException("Update method not implemented yet");
    }

    @Override
    public List<BillDetailEntity> getBillDetails(Connection connection, Integer billId) throws SQLException {
        List<BillDetailEntity> details = new ArrayList<>();
        String sql = "SELECT * FROM bill_details WHERE bill_id = ?";
        ResultSet rs = null;
        try {
            rs = DAOUtil.executeQuery(connection, sql, billId);
            while (rs.next()) {
                details.add(mapResultSetToBillDetailEntity(rs));
            }
        } finally {
            DBUtil.closeResultSet(rs);
        }
        return details;
    }

    @Override
    public List<BillDetailEntity> getAll(Connection connection, Map<String, String> searchParams) throws SQLException, ClassNotFoundException {
        throw new UnsupportedOperationException("GetAll method not implemented yet");

    }

    private BillDetailEntity mapResultSetToBillDetailEntity(ResultSet resultSet) throws SQLException {
        BillDetailEntity entity = new BillDetailEntity();
        entity.setId(resultSet.getInt("id"));
        entity.setBillId(resultSet.getInt("bill_id"));
        entity.setItemId(resultSet.getInt("item_id"));
        entity.setItemNameAtSale(resultSet.getString("item_name_at_sale"));
        entity.setUnitPriceAtSale(resultSet.getBigDecimal("unit_price_at_sale"));
        entity.setUnits(resultSet.getInt("units"));
        entity.setTotal(resultSet.getBigDecimal("total"));
        entity.setCreatedBy(resultSet.getObject("created_by", Integer.class));
        entity.setCreatedAt(resultSet.getTimestamp("created_at"));
        entity.setDeletedBy(resultSet.getObject("deleted_by", Integer.class));
        entity.setDeletedAt(resultSet.getTimestamp("deleted_at"));
        return entity;
    }
}
