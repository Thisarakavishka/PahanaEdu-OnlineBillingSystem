package com.icbt.pahanaeduonlinebillingsystem.bill.dao.impl;

import com.icbt.pahanaeduonlinebillingsystem.bill.dao.BillDAO;
import com.icbt.pahanaeduonlinebillingsystem.bill.entity.BillEntity;
import com.icbt.pahanaeduonlinebillingsystem.bill.mapper.BillMapper;
import com.icbt.pahanaeduonlinebillingsystem.common.exception.ExceptionType;
import com.icbt.pahanaeduonlinebillingsystem.common.exception.PahanaEduOnlineBillingSystemException;
import com.icbt.pahanaeduonlinebillingsystem.common.util.DAOUtil;
import com.icbt.pahanaeduonlinebillingsystem.common.util.DBUtil;
import com.icbt.pahanaeduonlinebillingsystem.common.util.LogUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
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

    @Override
    public List<BillEntity> getRecentBills(Connection connection, int limit) throws SQLException {
        List<BillEntity> recentBills = new ArrayList<>();
        String sql = "SELECT * FROM bills WHERE deleted_at IS NULL ORDER BY created_at DESC LIMIT ?";
        ResultSet resultSet = null;
        try {
            resultSet = DAOUtil.executeQuery(connection, sql, limit);
            while (resultSet.next()) {
                recentBills.add(BillMapper.mapResultSetToBillEntity(resultSet));
            }
        } catch (PahanaEduOnlineBillingSystemException e) {
            LOGGER.log(Level.SEVERE, "Failed to fetch recent bills in DAO", e);
        } finally {
            DBUtil.closeResultSet(resultSet);
        }
        return recentBills;
    }

    @Override
    public BigDecimal calculateTotalRevenue(Connection connection) throws SQLException {
        String sql = "SELECT SUM(total_amount) FROM bills WHERE deleted_at IS NULL";
        ResultSet rs = null;
        try {
            rs = DAOUtil.executeQuery(connection, sql);
            if (rs.next()) {
                return rs.getBigDecimal(1) != null ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        } catch (PahanaEduOnlineBillingSystemException e) {
            LOGGER.log(Level.SEVERE, "Failed to calculate total revenue", e);
        } finally {
            DBUtil.closeResultSet(rs);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public List<Map<String, Object>> getSalesForLast7Days(Connection connection) throws SQLException {
        List<Map<String, Object>> salesData = new ArrayList<>();
        String sql = "SELECT DATE(created_at) as sale_date, SUM(total_amount) as daily_total " +
                "FROM bills " +
                "WHERE created_at >= CURDATE() - INTERVAL 6 DAY AND deleted_at IS NULL " +
                "GROUP BY sale_date " +
                "ORDER BY sale_date ASC";
        ResultSet rs = null;
        try {
            rs = DAOUtil.executeQuery(connection, sql);
            while (rs.next()) {
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", rs.getDate("sale_date"));
                dayData.put("total", rs.getBigDecimal("daily_total"));
                salesData.add(dayData);
            }
        } catch (PahanaEduOnlineBillingSystemException e) {
            LOGGER.log(Level.SEVERE, "Failed to fetch weekly sales data", e);
        } finally {
            DBUtil.closeResultSet(rs);
        }
        return salesData;
    }

    @Override
    public Map<String, Object> getFinancialSummary(Connection connection, String startDate, String endDate) throws SQLException {
        Map<String, Object> summary = new HashMap<>();
        String sql = "SELECT " +
                "SUM(b.total_amount) AS totalRevenue, " +
                "COUNT(DISTINCT b.id) AS numberOfBills, " +
                "SUM(bd.units) AS totalItemsSold " +
                "FROM bills b JOIN bill_details bd ON b.id = bd.bill_id " +
                "WHERE b.deleted_at IS NULL AND b.created_at BETWEEN ? AND ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, startDate);
            pst.setString(2, endDate + " 23:59:59");
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    summary.put("totalRevenue", rs.getBigDecimal("totalRevenue") != null ? rs.getBigDecimal("totalRevenue") : BigDecimal.ZERO);
                    summary.put("numberOfBills", rs.getInt("numberOfBills"));
                    summary.put("totalItemsSold", rs.getInt("totalItemsSold"));
                }
            }
        }
        return summary;
    }

    @Override
    public Map<String, Object> getTopCustomer(Connection connection, String startDate, String endDate) throws SQLException {
        Map<String, Object> topCustomer = new HashMap<>();
        String sql = "SELECT c.name, SUM(b.total_amount) as totalSpent " +
                "FROM bills b JOIN customers c ON b.customer_id = c.id " +
                "WHERE b.deleted_at IS NULL AND b.created_at BETWEEN ? AND ? " +
                "GROUP BY c.name " +
                "ORDER BY totalSpent DESC " +
                "LIMIT 1";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, startDate);
            pst.setString(2, endDate + " 23:59:59");
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    topCustomer.put("name", rs.getString("name"));
                    topCustomer.put("totalSpent", rs.getBigDecimal("totalSpent"));
                }
            }
        }
        return topCustomer;
    }

    @Override
    public List<Map<String, Object>> getTopSellingItems(Connection connection, String startDate, String endDate, int limit) throws SQLException {
        List<Map<String, Object>> topItems = new ArrayList<>();
        String sql = "SELECT bd.item_name_at_sale as itemName, SUM(bd.units) as totalQuantity, SUM(bd.total) as totalRevenue " +
                "FROM bill_details bd JOIN bills b ON bd.bill_id = b.id " +
                "WHERE b.deleted_at IS NULL AND b.created_at BETWEEN ? AND ? " +
                "GROUP BY itemName " +
                "ORDER BY totalRevenue DESC " +
                "LIMIT ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, startDate);
            pst.setString(2, endDate + " 23:59:59");
            pst.setInt(3, limit);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> itemData = new HashMap<>();
                    itemData.put("name", rs.getString("itemName"));
                    itemData.put("quantity", rs.getInt("totalQuantity"));
                    itemData.put("revenue", rs.getBigDecimal("totalRevenue"));
                    topItems.add(itemData);
                }
            }
        }
        return topItems;
    }
}
