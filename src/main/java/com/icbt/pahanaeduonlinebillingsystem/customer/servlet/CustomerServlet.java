package com.icbt.pahanaeduonlinebillingsystem.customer.servlet;

import com.icbt.pahanaeduonlinebillingsystem.common.exception.PahanaEduOnlineBillingSystemException;
import com.icbt.pahanaeduonlinebillingsystem.common.util.LogUtil;
import com.icbt.pahanaeduonlinebillingsystem.common.util.SendResponse;
import com.icbt.pahanaeduonlinebillingsystem.customer.dto.CustomerDTO;
import com.icbt.pahanaeduonlinebillingsystem.customer.service.impl.CustomerServiceImpl;
import com.icbt.pahanaeduonlinebillingsystem.customer.service.CustomerService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Thisara Kavishka
 * @date 2025-07-18
 * @since 1.0
 */
@WebServlet(name = "CustomerServlet", urlPatterns = "/customers")
public class CustomerServlet extends HttpServlet {

    private CustomerService customerService;
    private static final Logger LOGGER = LogUtil.getLogger(CustomerServlet.class);

    @Override
    public void init() {
        customerService = new CustomerServiceImpl();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            CustomerDTO dto = new CustomerDTO();
            dto.setAccountNumber(req.getParameter("accountNumber"));
            dto.setName(req.getParameter("name"));
            dto.setAddress(req.getParameter("address"));
            dto.setPhone(req.getParameter("phone"));
            dto.setUnitsConsumed(Integer.parseInt(req.getParameter("unitsConsumed")));
            dto.setCreatedBy(1);

            boolean isAdded = customerService.add(dto);
            SendResponse.sendPlainText(resp, isAdded ? HttpServletResponse.SC_CREATED : HttpServletResponse.SC_BAD_REQUEST, isAdded ? "Customer added successfully" : "Customer added failed");
        } catch (PahanaEduOnlineBillingSystemException e) {
            LOGGER.warning("Business error: " + e.getExceptionType());
            SendResponse.sendPlainText(resp, HttpServletResponse.SC_BAD_REQUEST, "Error: " + e.getExceptionType());
        } catch (Exception e) {
            LOGGER.warning("Unexpected error: " + e.getMessage());
            SendResponse.sendPlainText(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String accountNumber = req.getParameter("accountNumber");

            if (accountNumber != null) {
                CustomerDTO dto = customerService.searchByAccountNumber(accountNumber);
                SendResponse.sendPlainText(resp, HttpServletResponse.SC_OK, "Customer: " + dto.getName() + " | Account Number: " + dto.getAccountNumber() + " | Phone Number: " + dto.getPhone());
            } else {
                List<CustomerDTO> list = customerService.getAll(new HashMap<>());
                PrintWriter writer = resp.getWriter();
                for (CustomerDTO dto : list) {
                    writer.println(dto.getName() + " - " + dto.getAccountNumber());
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Unexpected error: " + e.getMessage());
            SendResponse.sendPlainText(resp, HttpServletResponse.SC_BAD_REQUEST, "Internal server error");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Map<String, String> params = new HashMap<>();
            BufferedReader reader = req.getReader();
            reader.lines().forEach(line -> {
                for (String part : line.split("&")) {
                    String[] pair = part.split("=");
                    if (pair.length == 2) {
                        params.put(pair[0], pair[1]);
                    }
                }
            });

            CustomerDTO dto = new CustomerDTO();
            dto.setAccountNumber(params.get("accountNumber"));
            dto.setName(params.get("name"));
            dto.setAddress(params.get("address"));
            dto.setPhone(params.get("phone"));
            dto.setUnitsConsumed(Integer.parseInt(params.get("unitsConsumed")));

            dto.setCreatedBy(1);

            boolean isUpdated = customerService.update(dto);
            SendResponse.sendPlainText(resp, isUpdated ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_REQUEST, isUpdated ? "Customer updated successfully" : "Customer update failed");
        } catch (PahanaEduOnlineBillingSystemException e) {
            LOGGER.warning("Business error: " + e.getExceptionType());
            SendResponse.sendPlainText(resp, HttpServletResponse.SC_BAD_REQUEST, "Error: " + e.getExceptionType());
        } catch (Exception e) {
            LOGGER.warning("Unexpected error: " + e.getMessage());
            SendResponse.sendPlainText(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String accountNumber = req.getParameter("accountNumber");
            if (accountNumber != null || accountNumber.isEmpty()) {
                SendResponse.sendPlainText(resp, HttpServletResponse.SC_BAD_REQUEST, "Account number is required");
                return;
            }

            boolean isDeleted = customerService.delete(1, accountNumber);
            SendResponse.sendPlainText(resp, isDeleted ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_REQUEST, isDeleted ? "Customer deleted successfully" : "Customer delete failed");
        } catch (PahanaEduOnlineBillingSystemException e) {
            LOGGER.warning("Business error: " + e.getExceptionType());
            SendResponse.sendPlainText(resp, HttpServletResponse.SC_BAD_REQUEST, "Error: " + e.getExceptionType());
        } catch (Exception e) {
            LOGGER.warning("Unexpected error: " + e.getMessage());
            SendResponse.sendPlainText(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }

    }

}
