package com.icbt.pahanaeduonlinebillingsystem.customer.servlet;

import com.icbt.pahanaeduonlinebillingsystem.common.exception.PahanaEduOnlineBillingSystemException;
import com.icbt.pahanaeduonlinebillingsystem.common.util.LogUtil;
import com.icbt.pahanaeduonlinebillingsystem.common.util.SendResponse;
import com.icbt.pahanaeduonlinebillingsystem.customer.dto.CustomerDTO;
import com.icbt.pahanaeduonlinebillingsystem.customer.service.CustomerService;
import com.icbt.pahanaeduonlinebillingsystem.customer.service.impl.CustomerServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
    private static final Logger LOGGER = LogUtil.getLogger(CustomerServlet.class);

    @Override
    public void init() {
        customerService = new CustomerServiceImpl();
    }

    // Helper to get userId from session
    private Integer getUserIdFromSession(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            return (Integer) session.getAttribute("userId");
        }
        return null;
    }

    // Helper to convert CustomerDTO to a Map for JSON serialization
    private Map<String, Object> customerDtoToMap(CustomerDTO dto) {
        if (dto == null) return null;
        Map<String, Object> map = new HashMap<>();
        map.put("id", dto.getId());
        map.put("accountNumber", dto.getAccountNumber());
        map.put("name", dto.getName());
        map.put("address", dto.getAddress());
        map.put("phone", dto.getPhone());
        map.put("unitsConsumed", dto.getUnitsConsumed());
        map.put("createdBy", dto.getCreatedBy());
        map.put("createdAt", dto.getCreatedAt());
        map.put("updatedBy", dto.getUpdatedBy());
        map.put("updatedAt", dto.getUpdatedAt());
        map.put("deletedBy", dto.getDeletedBy());
        map.put("deletedAt", dto.getDeletedAt());
        return map;
    }

    // Helper to parse x-www-form-urlencoded body for PUT/DELETE
    private Map<String, String> parseUrlEncodedBody(HttpServletRequest req) throws IOException {
        Map<String, String> params = new HashMap<>();
        try (BufferedReader reader = req.getReader()) {
            String body = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            if (body != null && !body.isEmpty()) {
                Arrays.stream(body.split("&"))
                        .forEach(pair -> {
                            String[] keyValue = pair.split("=");
                            if (keyValue.length == 2) {
                                try {
                                    params.put(URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8.name()),
                                            URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8.name()));
                                } catch (Exception e) {
                                    LOGGER.log(Level.WARNING, "Error decoding URL-encoded parameter: " + e.getMessage());
                                }
                            }
                        });
            }
        }
        return params;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer currentUserId = getUserIdFromSession(req);
        if (currentUserId == null) {
            SendResponse.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, Map.of("message", "Unauthorized: Please log in."));
            return;
        }

        try {
            // Read parameters from form data (application/x-www-form-urlencoded)
            CustomerDTO dto = new CustomerDTO();
            dto.setAccountNumber(req.getParameter("accountNumber"));
            dto.setName(req.getParameter("name"));
            dto.setAddress(req.getParameter("address"));
            dto.setPhone(req.getParameter("phone"));
            String unitsConsumedStr = req.getParameter("unitsConsumed");
            dto.setUnitsConsumed(unitsConsumedStr != null && !unitsConsumedStr.isEmpty() ? Integer.parseInt(unitsConsumedStr) : 0);

            // Basic validation
            if (dto.getAccountNumber() == null || dto.getAccountNumber().trim().isEmpty() ||
                    dto.getName() == null || dto.getName().trim().isEmpty() ||
                    dto.getPhone() == null || dto.getPhone().trim().isEmpty()) {
                SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Missing required customer fields (accountNumber, name, phone)."));
                return;
            }

            // Set createdBy from session
            dto.setCreatedBy(currentUserId);

            boolean isAdded = customerService.add(dto);
            if (isAdded) {
                // Fetch the newly added customer to get its ID and audit timestamps
                CustomerDTO addedCustomer = customerService.searchByAccountNumber(dto.getAccountNumber());
                SendResponse.sendJson(resp, HttpServletResponse.SC_CREATED, Map.of("message", "Customer added successfully", "customer", customerDtoToMap(addedCustomer)));
            } else {
                SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Customer addition failed unexpectedly."));
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid number format for unitsConsumed: " + e.getMessage());
            SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Invalid format for units consumed. Please enter a valid number."));
        } catch (PahanaEduOnlineBillingSystemException e) {
            LOGGER.log(Level.WARNING, "Business error during customer add: " + e.getExceptionType().name() + " - " + e.getMessage());
            String errorMessage;
            int statusCode = HttpServletResponse.SC_BAD_REQUEST;
            switch (e.getExceptionType()) {
                case CUSTOMER_ACCOUNT_NUMBER_ALREADY_EXISTS:
                    errorMessage = "Customer with this account number already exists.";
                    break;
                case CUSTOMER_PHONE_NUMBER_ALREADY_EXISTS:
                    errorMessage = "Customer with this phone number already exists.";
                    break;
                case CUSTOMER_CREATION_FAILED:
                    errorMessage = "Failed to create customer due to a system error.";
                    statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                    break;
                case DATABASE_ERROR:
                    errorMessage = "A database error occurred during customer addition.";
                    statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                    break;
                default:
                    errorMessage = "An unexpected business error occurred: " + e.getExceptionType().name();
                    break;
            }
            SendResponse.sendJson(resp, statusCode, Map.of("message", errorMessage));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during POST /customers: " + e.getMessage(), e);
            SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Internal server error during customer addition."));
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String accountNumber = req.getParameter("accountNumber");
            Map<String, String> searchParams = new HashMap<>();

            if (accountNumber != null && !accountNumber.trim().isEmpty()) {
                CustomerDTO dto = customerService.searchByAccountNumber(accountNumber);
                SendResponse.sendJson(resp, HttpServletResponse.SC_OK, customerDtoToMap(dto));
            } else {
                // FIX: If 'search' parameter is present, use it directly
                String searchTerm = req.getParameter("search");
                if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                    searchParams.put("search", searchTerm); // Put into map with key "search"
                }

                List<CustomerDTO> list = customerService.getAll(searchParams);
                List<Map<String, Object>> customerMaps = list.stream()
                        .map(this::customerDtoToMap)
                        .collect(Collectors.toList());
                SendResponse.sendJson(resp, HttpServletResponse.SC_OK, customerMaps);
            }
        } catch (PahanaEduOnlineBillingSystemException e) {
            LOGGER.log(Level.WARNING, "Business error during GET /customers: " + e.getExceptionType().name() + " - " + e.getMessage());
            String errorMessage;
            int statusCode = HttpServletResponse.SC_BAD_REQUEST;
            switch (e.getExceptionType()) {
                case CUSTOMER_NOT_FOUND:
                    errorMessage = "Customer not found.";
                    statusCode = HttpServletResponse.SC_NOT_FOUND;
                    break;
                case DATABASE_ERROR:
                    errorMessage = "A database error occurred while fetching customers.";
                    statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                    break;
                default:
                    errorMessage = "An unexpected business error occurred: " + e.getExceptionType().name();
                    break;
            }
            SendResponse.sendJson(resp, statusCode, Map.of("message", errorMessage));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during GET /customers: " + e.getMessage(), e);
            SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Internal server error while fetching customers."));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer currentUserId = getUserIdFromSession(req);
        if (currentUserId == null) {
            SendResponse.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, Map.of("message", "Unauthorized: Please log in."));
            return;
        }

        try {
            // Manually parse the request body for URL-encoded parameters
            Map<String, String> requestBodyParams = parseUrlEncodedBody(req);

            CustomerDTO dto = new CustomerDTO();

            // Retrieve parameters from the parsed body map
            String idStr = requestBodyParams.get("id");
            LOGGER.log(Level.INFO, "Received ID for update (from parsed body): " + idStr); // Added log

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

            // Basic validation
            if (dto.getAccountNumber() == null || dto.getAccountNumber().trim().isEmpty() ||
                    dto.getName() == null || dto.getName().trim().isEmpty() ||
                    dto.getPhone() == null || dto.getPhone().trim().isEmpty()) {
                SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Missing required customer fields for update."));
                return;
            }
            // Set updatedBy from session
            dto.setUpdatedBy(currentUserId);

            boolean isUpdated = customerService.update(dto);
            if (isUpdated) {
                CustomerDTO updatedCustomer = customerService.searchByAccountNumber(dto.getAccountNumber());
                SendResponse.sendJson(resp, HttpServletResponse.SC_OK, Map.of("message", "Customer updated successfully", "customer", customerDtoToMap(updatedCustomer)));
            } else {
                SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Customer update failed unexpectedly."));
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid number format for unitsConsumed during update: " + e.getMessage());
            SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Invalid format for units consumed. Please enter a valid number."));
        } catch (PahanaEduOnlineBillingSystemException e) {
            LOGGER.log(Level.WARNING, "Business error during customer update: " + e.getExceptionType().name() + " - " + e.getMessage());
            String errorMessage;
            int statusCode = HttpServletResponse.SC_BAD_REQUEST;
            switch (e.getExceptionType()) {
                case CUSTOMER_NOT_FOUND:
                    errorMessage = "Customer not found for update.";
                    statusCode = HttpServletResponse.SC_NOT_FOUND;
                    break;
                case CUSTOMER_PHONE_NUMBER_ALREADY_EXISTS:
                    errorMessage = "Another customer with this phone number already exists.";
                    break;
                case CUSTOMER_UPDATE_FAILED:
                    errorMessage = "Failed to update customer due to a system error.";
                    statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                    break;
                case DATABASE_ERROR:
                    errorMessage = "A database error occurred during customer update.";
                    statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                    break;
                default:
                    errorMessage = "An unexpected business error occurred: " + e.getExceptionType().name();
                    break;
            }
            SendResponse.sendJson(resp, statusCode, Map.of("message", errorMessage));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during PUT /customers: " + e.getMessage(), e);
            SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Internal server error during customer update."));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer currentUserId = getUserIdFromSession(req);
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
            LOGGER.log(Level.WARNING, "Business error during customer delete: " + e.getExceptionType().name() + " - " + e.getMessage());
            String errorMessage;
            int statusCode = HttpServletResponse.SC_BAD_REQUEST;
            switch (e.getExceptionType()) {
                case CUSTOMER_NOT_FOUND:
                    errorMessage = "Customer not found for deletion.";
                    statusCode = HttpServletResponse.SC_NOT_FOUND;
                    break;
                case CUSTOMER_DELETION_FAILED:
                    errorMessage = "Failed to delete customer due to a system error.";
                    statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                    break;
                case DATABASE_ERROR:
                    errorMessage = "A database error occurred during customer deletion.";
                    statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                    break;
                default:
                    errorMessage = "An unexpected business error occurred: " + e.getExceptionType().name();
                    break;
            }
            SendResponse.sendJson(resp, statusCode, Map.of("message", errorMessage));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during DELETE /customers: " + e.getMessage(), e);
            SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Internal server error during customer deletion."));
        }
    }
}