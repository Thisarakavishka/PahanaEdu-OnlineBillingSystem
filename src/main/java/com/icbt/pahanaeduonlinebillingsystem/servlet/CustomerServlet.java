package com.icbt.pahanaeduonlinebillingsystem.servlet;

import com.icbt.pahanaeduonlinebillingsystem.dto.CustomerDTO;
import com.icbt.pahanaeduonlinebillingsystem.service.ServiceFactory;
import com.icbt.pahanaeduonlinebillingsystem.service.ServiceType;
import com.icbt.pahanaeduonlinebillingsystem.service.services.CustomerService;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * @author Thisara Kavishka
 * @date 2025-07-18
 * @since 1.0
 */
@WebServlet(name = "CustomerServlet", urlPatterns = "/customers")
public class CustomerServlet extends HttpServlet {

    private CustomerService customerService;

    @Override
    public void init() {
        customerService = ServiceFactory.getInstance().getService(ServiceType.CUSTOMER_SERVICE);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String accountNumber = req.getParameter("accountNumber");
            String name = req.getParameter("name");
            String address = req.getParameter("address");
            String phone = req.getParameter("phone");
            int unitsConsumed = Integer.parseInt(req.getParameter("unitsConsumed"));

            CustomerDTO dto = new CustomerDTO();
            dto.setAccountNumber(accountNumber);
            dto.setName(name);
            dto.setAddress(address);
            dto.setPhone(phone);
            dto.setUnitsConsumed(unitsConsumed);
            dto.setCreatedBy(1); // Simulated logged-in user

            boolean result = customerService.add(dto);
            resp.setStatus(result ? HttpServletResponse.SC_CREATED : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(result ? "Customer added successfully" : "Failed to add customer");

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Error: " + e.getMessage());
        }
    }
}
