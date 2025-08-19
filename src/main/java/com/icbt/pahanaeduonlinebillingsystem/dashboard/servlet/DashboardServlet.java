package com.icbt.pahanaeduonlinebillingsystem.dashboard.servlet;

import com.icbt.pahanaeduonlinebillingsystem.bill.dto.BillDTO;
import com.icbt.pahanaeduonlinebillingsystem.bill.service.BillService;
import com.icbt.pahanaeduonlinebillingsystem.bill.service.impl.BillServiceImpl;
import com.icbt.pahanaeduonlinebillingsystem.common.util.LogUtil;
import com.icbt.pahanaeduonlinebillingsystem.common.util.SendResponse;
import com.icbt.pahanaeduonlinebillingsystem.customer.service.CustomerService;
import com.icbt.pahanaeduonlinebillingsystem.customer.service.impl.CustomerServiceImpl;
import com.icbt.pahanaeduonlinebillingsystem.item.service.ItemService;
import com.icbt.pahanaeduonlinebillingsystem.item.service.impl.ItemServiceImpl;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Thisara Kavishka
 * @date 2025-08-19
 * @since 1.0
 */
@WebServlet(name = "DashboardServlet", urlPatterns = "/dashboard-data")
public class DashboardServlet extends HttpServlet {

    private CustomerService customerService;
    private ItemService itemService;
    private BillService billService;
    private static final Logger LOGGER = LogUtil.getLogger(DashboardServlet.class);

    @Override
    public void init() {
        customerService = new CustomerServiceImpl();
        itemService = new ItemServiceImpl();
        billService = new BillServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // Fetch all data points in parallel
            int customerCount = customerService.getCustomersCount();
            int itemCount = itemService.getItemsCount();
            int billCount = billService.getBillsCount();
            BigDecimal totalRevenue = billService.getTotalRevenue();
            List<BillDTO> recentBills = billService.getRecentBills(5);
            List<Map<String, Object>> weeklySales = billService.getWeeklySalesData();
            List<Map<String, Object>> topItems = billService.getTopSellingItems(5);
            List<Map<String, Object>> topCustomers = billService.getTopSpendingCustomers(5);
            List<Map<String, Object>> topUsers = billService.getTopPerformingUsers(5);


            // Assemble the data into a single map
            Map<String, Object> dashboardData = new HashMap<>();
            dashboardData.put("totalCustomers", customerCount);
            dashboardData.put("totalItems", itemCount);
            dashboardData.put("totalBills", billCount);
            dashboardData.put("totalRevenue", totalRevenue);
            dashboardData.put("weeklySales", weeklySales);
            dashboardData.put("topSellingItems", topItems);
            dashboardData.put("topCustomers", topCustomers);
            dashboardData.put("topPerformingUsers", topUsers);

            // Convert recent bills to a map for JSON serialization
            List<Map<String, Object>> recentBillsMap = recentBills.stream()
                    .map(bill -> {
                        Map<String, Object> billMap = new HashMap<>();
                        billMap.put("id", bill.getId());
                        billMap.put("customerName", bill.getCustomerName());
                        billMap.put("totalAmount", bill.getTotalAmount());
                        billMap.put("generatedAt", bill.getCreatedAt().toString());
                        return billMap;
                    })
                    .collect(Collectors.toList());

            dashboardData.put("recentBills", recentBillsMap);

            SendResponse.sendJson(resp, HttpServletResponse.SC_OK, dashboardData);

        } catch (Exception e) {
            SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Failed to load dashboard data."));
        }
    }
}