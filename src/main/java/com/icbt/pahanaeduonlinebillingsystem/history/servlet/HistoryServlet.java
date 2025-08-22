package com.icbt.pahanaeduonlinebillingsystem.history.servlet;

import com.icbt.pahanaeduonlinebillingsystem.common.util.SendResponse;
import com.icbt.pahanaeduonlinebillingsystem.common.util.ServletUtil;
import com.icbt.pahanaeduonlinebillingsystem.customer.dto.CustomerDTO;
import com.icbt.pahanaeduonlinebillingsystem.customer.mapper.CustomerMapper;
import com.icbt.pahanaeduonlinebillingsystem.customer.service.CustomerService;
import com.icbt.pahanaeduonlinebillingsystem.customer.service.impl.CustomerServiceImpl;
import com.icbt.pahanaeduonlinebillingsystem.item.dto.ItemDTO;
import com.icbt.pahanaeduonlinebillingsystem.item.mapper.ItemMapper;
import com.icbt.pahanaeduonlinebillingsystem.item.service.ItemService;
import com.icbt.pahanaeduonlinebillingsystem.item.service.impl.ItemServiceImpl;
import com.icbt.pahanaeduonlinebillingsystem.user.converter.UserMapper;
import com.icbt.pahanaeduonlinebillingsystem.user.dto.UserDTO;
import com.icbt.pahanaeduonlinebillingsystem.user.service.UserService;
import com.icbt.pahanaeduonlinebillingsystem.user.service.impl.UserServiceImpl;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Thisara Kavishka
 * @date 2025-08-20
 * @since 1.0
 */
@WebServlet(name = "HistoryServlet", urlPatterns = "/history-data")
public class HistoryServlet extends HttpServlet {

    private UserService userService;
    private CustomerService customerService;
    private ItemService itemService;
    private static final int INITIAL_ADMIN_ID = 1;

    @Override
    public void init() {
        userService = new UserServiceImpl();
        customerService = new CustomerServiceImpl();
        itemService = new ItemServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String currentUserRole = ServletUtil.getUserRoleFromSession(req);
        Integer currentUserId = ServletUtil.getUserIdFromSession(req);

        if (!"ADMIN".equals(currentUserRole)) {
            SendResponse.sendJson(resp, HttpServletResponse.SC_FORBIDDEN, Map.of("message", "Access denied."));
            return;
        }

        try {
            Map<String, Object> historyData = new HashMap<>();

            List<UserDTO> deletedUsers = userService.getAllDeletedUsers();

            List<Map<String, Object>> userMaps = deletedUsers.stream()
                    .map(user -> UserMapper.toMap(user, null, null, null))
                    .collect(Collectors.toList());
            historyData.put("deletedUsers", userMaps);

            if (currentUserId != null && currentUserId == INITIAL_ADMIN_ID) {
                List<CustomerDTO> deletedCustomers = customerService.getAllDeletedCustomers();
                List<ItemDTO> deletedItems = itemService.getAllDeletedItems();

                List<Map<String, Object>> customerMaps = deletedCustomers.stream()
                        .map(customer -> CustomerMapper.toMap(customer, null, null, null))
                        .collect(Collectors.toList());
                historyData.put("deletedCustomers", customerMaps);

                List<Map<String, Object>> itemMaps = deletedItems.stream()
                        .map(item -> ItemMapper.toMap(item, null, null, null))
                        .collect(Collectors.toList());
                historyData.put("deletedItems", itemMaps);
            }
            SendResponse.sendJson(resp, HttpServletResponse.SC_OK, historyData);
        } catch (Exception e) {
            SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Failed to load history data."));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer currentUserId = ServletUtil.getUserIdFromSession(req);
        if (currentUserId == null || currentUserId != INITIAL_ADMIN_ID) {
            SendResponse.sendJson(resp, HttpServletResponse.SC_FORBIDDEN, Map.of("message", "Only the initial admin can restore records."));
            return;
        }

        String type = req.getParameter("type");
        int id = Integer.parseInt(req.getParameter("id"));

        try {
            boolean success = false;
            switch (type) {
                case "user":
                    success = userService.restoreUser(id);
                    break;
                case "customer":
                    success = customerService.restoreCustomer(id);
                    break;
                case "item":
                    success = itemService.restoreItem(id);
                    break;
            }

            if (success) {
                SendResponse.sendJson(resp, HttpServletResponse.SC_OK, Map.of("message", "Record restored successfully."));
            } else {
                throw new Exception("Restore operation failed at the service layer.");
            }
        } catch (Exception e) {
            SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", e.getMessage()));
        }
    }
}