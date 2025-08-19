package com.icbt.pahanaeduonlinebillingsystem.bill.dao;

import com.icbt.pahanaeduonlinebillingsystem.bill.entity.BillEntity;
import com.icbt.pahanaeduonlinebillingsystem.common.base.CrudDAO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author Thisara Kavishka
 * @date 2025-08-11
 * @since 1.0
 */
public interface BillDAO extends CrudDAO<BillEntity> {

    int addBill(Connection connection, BillEntity entity) throws SQLException, ClassNotFoundException;

    int getBillsCount(Connection connection) throws SQLException, ClassNotFoundException;

    List<BillEntity> getRecentBills(Connection connection, int limit) throws SQLException;

    BigDecimal calculateTotalRevenue(Connection connection) throws SQLException;

    List<Map<String, Object>> getSalesForLast7Days(Connection connection) throws SQLException;

    Map<String, Object> getFinancialSummary(Connection connection, String startDate, String endDate) throws SQLException;

    List<Map<String, Object>> getTopSellingItems(Connection connection, String startDate, String endDate, int limit) throws SQLException;

    Map<String, Object> getTopCustomer(Connection connection, String startDate, String endDate) throws SQLException;
}