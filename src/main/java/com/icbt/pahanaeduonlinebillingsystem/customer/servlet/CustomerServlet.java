package com.icbt.pahanaeduonlinebillingsystem.customer.servlet;

import com.icbt.pahanaeduonlinebillingsystem.common.exception.PahanaEduOnlineBillingSystemException;
import com.icbt.pahanaeduonlinebillingsystem.common.util.LogUtil;
import com.icbt.pahanaeduonlinebillingsystem.common.util.SendResponse;
import com.icbt.pahanaeduonlinebillingsystem.common.util.ServletUtil;
import com.icbt.pahanaeduonlinebillingsystem.common.util.Validator;
import com.icbt.pahanaeduonlinebillingsystem.customer.dto.CustomerDTO;
import com.icbt.pahanaeduonlinebillingsystem.customer.mapper.CustomerMapper;
import com.icbt.pahanaeduonlinebillingsystem.customer.service.CustomerService;
import com.icbt.pahanaeduonlinebillingsystem.customer.service.impl.CustomerServiceImpl;
import com.icbt.pahanaeduonlinebillingsystem.user.dto.UserDTO;
import com.icbt.pahanaeduonlinebillingsystem.user.service.UserService;
import com.icbt.pahanaeduonlinebillingsystem.user.service.impl.UserServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Thisara Kavishka
 * @date 2025-07-18
 * @since 1.0
 */
@WebServlet(name = "CustomerServlet", urlPatterns = "/customers")
public class CustomerServlet extends HttpServlet {

    private CustomerService customerService;
    private UserService userService;
    private static final Logger LOGGER = LogUtil.getLogger(CustomerServlet.class);
    private static final int INITIAL_ADMIN_ID = 1;

    @Override
    public void init() {
        customerService = new CustomerServiceImpl();
        userService = new UserServiceImpl();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer currentUserId = ServletUtil.getUserIdFromSession(req);
        String currentUserRole = ServletUtil.getUserRoleFromSession(req);

        if (!"ADMIN".equals(currentUserRole)) {
            SendResponse.sendJson(resp, HttpServletResponse.SC_FORBIDDEN, Map.of("message", "Unauthorized: Only admins can add customers."));
            return;
        }
        if (currentUserId == null) {
            SendResponse.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, Map.of("message", "Unauthorized: Please log in."));
            return;
        }

        try {
            CustomerDTO dto = new CustomerDTO();
            dto.setAccountNumber(req.getParameter("accountNumber"));
            dto.setName(req.getParameter("name"));
            dto.setAddress(req.getParameter("address"));
            dto.setPhone(req.getParameter("phone"));
            String unitsConsumedStr = req.getParameter("unitsConsumed");
            dto.setUnitsConsumed(unitsConsumedStr != null && !unitsConsumedStr.isEmpty() ? Integer.parseInt(unitsConsumedStr) : 0);

            Map<String, String> validationErrors = Validator.customerValidate(dto);
            if (!validationErrors.isEmpty()) {
                SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Validation failed", "errors", validationErrors));
                return;
            }

            dto.setCreatedBy(currentUserId);

            boolean isAdded = customerService.add(dto);
            if (isAdded) {
                CustomerDTO addedCustomer = customerService.searchByAccountNumber(dto.getAccountNumber());
                SendResponse.sendJson(resp, HttpServletResponse.SC_CREATED, Map.of("message", "Customer added successfully", "customer", CustomerMapper.toMap(addedCustomer, null, null, null)));
            } else {
                SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Customer addition failed unexpectedly."));
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid number format for unitsConsumed: " + e.getMessage());
            SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Invalid format for units consumed. Please enter a valid number."));
        } catch (PahanaEduOnlineBillingSystemException e) {
            handlePahanaException(resp, e, "addition");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during POST /customers: " + e.getMessage(), e);
            SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Internal server error during customer addition."));
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String accountNumber = req.getParameter("accountNumber");
            String phone = req.getParameter("phone");
            Map<String, String> searchParams = new HashMap<>();

            if (phone != null && !phone.trim().isEmpty()) {
                CustomerDTO dto = customerService.searchByPhone(phone);

                String createdByUsername = null;
                String updatedByUsername = null;
                String deletedByUsername = null;

                if (dto.getCreatedBy() != null) {
                    UserDTO user = userService.searchById(dto.getCreatedBy());
                    if (user != null) createdByUsername = user.getUsername();
                }
                if (dto.getUpdatedBy() != null) {
                    UserDTO user = userService.searchById(dto.getUpdatedBy());
                    if (user != null) updatedByUsername = user.getUsername();
                }
                if (dto.getDeletedBy() != null) {
                    UserDTO user = userService.searchById(dto.getDeletedBy());
                    if (user != null) deletedByUsername = user.getUsername();
                }

                SendResponse.sendJson(resp, HttpServletResponse.SC_OK, CustomerMapper.toMap(dto, createdByUsername, updatedByUsername, deletedByUsername));
            } else if (accountNumber != null && !accountNumber.trim().isEmpty()) {
                CustomerDTO dto = customerService.searchByAccountNumber(accountNumber);

                String createdByUsername = null;
                String updatedByUsername = null;
                String deletedByUsername = null;

                if (dto.getCreatedBy() != null) {
                    UserDTO user = userService.searchById(dto.getCreatedBy());
                    if (user != null) createdByUsername = user.getUsername();
                }
                if (dto.getUpdatedBy() != null) {
                    UserDTO user = userService.searchById(dto.getUpdatedBy());
                    if (user != null) updatedByUsername = user.getUsername();
                }
                if (dto.getDeletedBy() != null) {
                    UserDTO user = userService.searchById(dto.getDeletedBy());
                    if (user != null) deletedByUsername = user.getUsername();
                }

                SendResponse.sendJson(resp, HttpServletResponse.SC_OK, CustomerMapper.toMap(dto, createdByUsername, updatedByUsername, deletedByUsername));
            } else {
                String searchTerm = req.getParameter("search");
                if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                    searchParams.put("search", searchTerm);
                }

                List<CustomerDTO> list = customerService.getAll(searchParams);
                List<Map<String, Object>> customerMaps = list.stream()
                        .map(item -> CustomerMapper.toMap(item, null, null, null)) // Pass null for usernames in list view
                        .collect(Collectors.toList());
                SendResponse.sendJson(resp, HttpServletResponse.SC_OK, customerMaps);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid ID format for GET /customers: " + e.getMessage());
            SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Invalid customer ID format."));
        } catch (PahanaEduOnlineBillingSystemException e) {
            handlePahanaException(resp, e, "fetching");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during GET /customers: " + e.getMessage(), e);
            SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Internal server error while fetching customers."));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String currentUserRole = ServletUtil.getUserRoleFromSession(req);
        Integer currentUserId = ServletUtil.getUserIdFromSession(req);

        if (!"ADMIN".equals(currentUserRole)) {
            SendResponse.sendJson(resp, HttpServletResponse.SC_FORBIDDEN, Map.of("message", "Unauthorized: Only admins can update customers."));
            return;
        }
        if (currentUserId == null) {
            SendResponse.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, Map.of("message", "Unauthorized: Please log in."));
            return;
        }

        try {
            Map<String, String> requestBodyParams = ServletUtil.parseUrlEncodedBody(req);

            CustomerDTO dto = new CustomerDTO();

            String idStr = requestBodyParams.get("id");
            if (idStr != null && !idStr.isEmpty()) {
                dto.setId(Integer.parseInt(idStr));
            } else {
                SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Customer ID is required for update."));
                return;
            }

            dto.setAccountNumber(requestBodyParams.get("accountNumber"));
            dto.setName(requestBodyParams.get("name"));
            dto.setAddress(requestBodyParams.get("address"));
            dto.setPhone(requestBodyParams.get("phone"));
            String unitsConsumedStr = requestBodyParams.get("unitsConsumed");
            dto.setUnitsConsumed(unitsConsumedStr != null && !unitsConsumedStr.isEmpty() ? Integer.parseInt(unitsConsumedStr) : 0);

            Map<String, String> validationErrors = Validator.customerValidate(dto);
            if (!validationErrors.isEmpty()) {
                SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Validation failed", "errors", validationErrors));
                return;
            }

            dto.setUpdatedBy(currentUserId);

            boolean isUpdated = customerService.update(dto);
            if (isUpdated) {
                CustomerDTO updatedCustomer = customerService.searchByAccountNumber(dto.getAccountNumber());
                SendResponse.sendJson(resp, HttpServletResponse.SC_OK, Map.of("message", "Customer updated successfully", "customer", CustomerMapper.toMap(updatedCustomer, null, null, null)));
            } else {
                SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Customer update failed unexpectedly."));
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid number format for unitsConsumed during update: " + e.getMessage());
            SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Invalid format for units consumed. Please enter a valid number."));
        } catch (PahanaEduOnlineBillingSystemException e) {
            handlePahanaException(resp, e, "update");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during PUT /customers: " + e.getMessage(), e);
            SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Internal server error during customer update."));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String currentUserRole = ServletUtil.getUserRoleFromSession(req);
        Integer currentUserId = ServletUtil.getUserIdFromSession(req);

        if (!"ADMIN".equals(currentUserRole) || (currentUserId != null && currentUserId != INITIAL_ADMIN_ID)) {
            SendResponse.sendJson(resp, HttpServletResponse.SC_FORBIDDEN, Map.of("message", "Unauthorized: Only the Initial Admin can delete customers."));
            return;
        }
        if (currentUserId == null) {
            SendResponse.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, Map.of("message", "Unauthorized: Please log in."));
            return;
        }

        String accountNumber = req.getParameter("accountNumber");

        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Account number is required for deletion."));
            return;
        }

        try {
            boolean isDeleted = customerService.delete(currentUserId, accountNumber);
            if (isDeleted) {
                SendResponse.sendJson(resp, HttpServletResponse.SC_OK, Map.of("message", "Customer deleted successfully."));
            } else {
                SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Customer deletion failed unexpectedly."));
            }
        } catch (PahanaEduOnlineBillingSystemException e) {
            handlePahanaException(resp, e, "deletion");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during DELETE /customers: " + e.getMessage(), e);
            SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Internal server error during customer deletion."));
        }
    }

    private void handlePahanaException(HttpServletResponse resp, PahanaEduOnlineBillingSystemException e, String action) throws IOException {
        LOGGER.log(Level.WARNING, "Business error during customer " + action + ": " + e.getExceptionType().name() + " - " + e.getMessage());
        String errorMessage;
        int statusCode = HttpServletResponse.SC_BAD_REQUEST;

        try {
            switch (e.getExceptionType()) {
                case CUSTOMER_NOT_FOUND:
                    errorMessage = "Customer not found.";
                    statusCode = HttpServletResponse.SC_NOT_FOUND;
                    break;
                case CUSTOMER_ACCOUNT_NUMBER_ALREADY_EXISTS:
                    errorMessage = "Customer with this account number already exists.";
                    break;
                case CUSTOMER_PHONE_NUMBER_ALREADY_EXISTS:
                    errorMessage = "Another customer with this phone number already exists.";
                    break;
                case CUSTOMER_CREATION_FAILED:
                case CUSTOMER_UPDATE_FAILED:
                case CUSTOMER_DELETION_FAILED:
                    errorMessage = "Failed to " + action + " customer due to a system error.";
                    statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                    break;
                case DATABASE_ERROR:
                    errorMessage = "A database error occurred during customer " + action + ".";
                    statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                    break;
                default:
                    errorMessage = "An unexpected business error occurred: " + e.getExceptionType().name();
                    break;
            }
            SendResponse.sendJson(resp, statusCode, Map.of("message", errorMessage));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Unexpected error during handlePahanaException: " + ex.getMessage(), ex);
            SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Internal server error during handlePahanaException."));
        }
    }
}