package com.icbt.pahanaeduonlinebillingsystem.bill.service;

import com.icbt.pahanaeduonlinebillingsystem.bill.dto.BillDTO;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author Thisara Kavishka
 * @date 2025-08-11
 * @since 1.0
 */
public interface BillService {

    BillDTO generateBill(BillDTO billDTO, Integer generatedByUserId) throws ClassNotFoundException;

    boolean delete(Integer deletedByUserId, Integer billId) throws ClassNotFoundException;

    BillDTO getBillById(Integer billId) throws ClassNotFoundException;

    List<BillDTO> getAll(Map<String, String> searchParams) throws ClassNotFoundException;

    int getBillsCount() throws SQLException, ClassNotFoundException;

    List<BillDTO> getRecentBills(int limit) throws ClassNotFoundException;

    BigDecimal getTotalRevenue() throws ClassNotFoundException;

    List<Map<String, Object>> getWeeklySalesData() throws ClassNotFoundException;

    List<Map<String, Object>> getTopSellingItems(int limit) throws ClassNotFoundException;

    List<Map<String, Object>> getTopSpendingCustomers(int limit) throws ClassNotFoundException;

    List<Map<String, Object>> getTopPerformingUsers(int limit) throws ClassNotFoundException;

    Map<String, Object> generateFinancialReport(String startDate, String endDate) throws ClassNotFoundException;
}
