package com.icbt.pahanaeduonlinebillingsystem.dashboard.servlet;

import com.icbt.pahanaeduonlinebillingsystem.bill.service.BillService;
import com.icbt.pahanaeduonlinebillingsystem.bill.service.impl.BillServiceImpl;
import com.icbt.pahanaeduonlinebillingsystem.common.util.LogUtil;
import com.icbt.pahanaeduonlinebillingsystem.common.util.SendResponse;
import com.icbt.pahanaeduonlinebillingsystem.customer.service.CustomerService;
import com.icbt.pahanaeduonlinebillingsystem.customer.service.impl.CustomerServiceImpl;
import com.icbt.pahanaeduonlinebillingsystem.item.service.ItemService;
import com.icbt.pahanaeduonlinebillingsystem.item.service.impl.ItemServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            int customerCount = customerService.getCustomersCount();
            int itemCount = itemService.getItemsCount();
            int billCount = billService.getBillsCount();

            Map<String, Object> dashboardData = new HashMap<>();
            dashboardData.put("totalCustomers", customerCount);
            dashboardData.put("totalItems", itemCount);
            dashboardData.put("totalBills", billCount);

            SendResponse.sendJson(resp, HttpServletResponse.SC_OK, dashboardData);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to fetch dashboard data", e);
            SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Failed to load dashboard data."));
        }
    }
}