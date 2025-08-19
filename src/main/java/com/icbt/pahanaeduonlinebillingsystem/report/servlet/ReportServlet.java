package com.icbt.pahanaeduonlinebillingsystem.report.servlet;

import com.icbt.pahanaeduonlinebillingsystem.bill.service.BillService;
import com.icbt.pahanaeduonlinebillingsystem.bill.service.impl.BillServiceImpl;
import com.icbt.pahanaeduonlinebillingsystem.common.util.LogUtil;
import com.icbt.pahanaeduonlinebillingsystem.common.util.SendResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Thisara Kavishka
 * @date 2025-08-19
 * @since 1.0
 */
@WebServlet(name = "ReportServlet", urlPatterns = "/reports-data")
public class ReportServlet extends HttpServlet {

    private BillService billService;
    private static final Logger LOGGER = LogUtil.getLogger(ReportServlet.class);

    @Override
    public void init() {
        billService = new BillServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String startDate = req.getParameter("startDate");
            String endDate = req.getParameter("endDate");

            if (startDate == null || endDate == null || startDate.isEmpty() || endDate.isEmpty()) {
                SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Start date and end date are required."));
                return;
            }

            Map<String, Object> reportData = billService.generateFinancialReport(startDate, endDate);

            SendResponse.sendJson(resp, HttpServletResponse.SC_OK, reportData);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to generate report", e);
            SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "An unexpected error occurred while generating the report."));
        }
    }
}