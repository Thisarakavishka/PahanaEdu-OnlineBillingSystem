package com.icbt.pahanaeduonlinebillingsystem.item.servlet;

import com.icbt.pahanaeduonlinebillingsystem.common.exception.PahanaEduOnlineBillingSystemException;
import com.icbt.pahanaeduonlinebillingsystem.common.util.LogUtil;
import com.icbt.pahanaeduonlinebillingsystem.common.util.SendResponse;
import com.icbt.pahanaeduonlinebillingsystem.item.dto.ItemDTO;
import com.icbt.pahanaeduonlinebillingsystem.item.service.ItemService;
import com.icbt.pahanaeduonlinebillingsystem.item.service.impl.ItemServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
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
 * @date 2025-08-07
 * @since 1.0
 */
@WebServlet(name = "ItemServlet", urlPatterns = "/items")
public class ItemServlet extends HttpServlet {

    private ItemService itemService;
    private static final Logger LOGGER = LogUtil.getLogger(ItemServlet.class);
    private static final int INITIAL_ADMIN_ID = 1; // Assuming initial admin ID

    @Override
    public void init() {
        itemService = new ItemServiceImpl();
    }

    // Helper to get userId from session
    private Integer getUserIdFromSession(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            return (Integer) session.getAttribute("userId");
        }
        return null;
    }

    // Helper to get user role from session
    private String getUserRoleFromSession(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("role") != null) {
            return (String) session.getAttribute("role");
        }
        return null;
    }

    // Helper to convert ItemDTO to a Map for JSON serialization
    private Map<String, Object> itemDtoToMap(ItemDTO dto) {
        if (dto == null) return null;
        Map<String, Object> map = new HashMap<>();
        map.put("id", dto.getId());
        map.put("name", dto.getName());
        map.put("unitPrice", dto.getUnitPrice());
        map.put("stockQuantity", dto.getStockQuantity());
        map.put("createdBy", dto.getCreatedBy());
        map.put("createdAt", dto.getCreatedAt());
        map.put("updatedBy", dto.getUpdatedBy());
        map.put("updatedAt", dto.getUpdatedAt());
        map.put("deletedBy", dto.getDeletedBy());
        map.put("deletedAt", dto.getDeletedAt());
        return map;
    }

    // Helper to parse x-www-form-urlencoded body for POST/PUT
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
        String currentUserRole = getUserRoleFromSession(req);
        Integer currentUserId = getUserIdFromSession(req);

        if (!"ADMIN".equals(currentUserRole)) {
            SendResponse.sendJson(resp, HttpServletResponse.SC_FORBIDDEN, Map.of("message", "Unauthorized: Only admins can add items."));
            return;
        }
        if (currentUserId == null) {
            SendResponse.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, Map.of("message", "Unauthorized: Please log in."));
            return;
        }

        try {
            ItemDTO dto = new ItemDTO();
            dto.setName(req.getParameter("name"));
            String unitPriceStr = req.getParameter("unitPrice");
            String stockQuantityStr = req.getParameter("stockQuantity");

            // Basic validation
            if (dto.getName() == null || dto.getName().trim().isEmpty() ||
                    unitPriceStr == null || unitPriceStr.trim().isEmpty() ||
                    stockQuantityStr == null || stockQuantityStr.trim().isEmpty()) {
                SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Missing required item fields (name, unitPrice, stockQuantity)."));
                return;
            }

            dto.setUnitPrice(new BigDecimal(unitPriceStr));
            dto.setStockQuantity(Integer.parseInt(stockQuantityStr));
            dto.setCreatedBy(currentUserId);

            boolean isAdded = itemService.add(dto);
            if (isAdded) {
                ItemDTO addedItem = itemService.searchByName(dto.getName());
                SendResponse.sendJson(resp, HttpServletResponse.SC_CREATED, Map.of("message", "Item added successfully", "item", itemDtoToMap(addedItem)));
            } else {
                SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Item addition failed unexpectedly."));
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid number format for unitPrice or stockQuantity: " + e.getMessage());
            SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Invalid format for price or quantity. Please enter valid numbers."));
        } catch (PahanaEduOnlineBillingSystemException e) {
            LOGGER.log(Level.WARNING, "Business error during item add: " + e.getExceptionType().name() + " - " + e.getMessage());
            String errorMessage;
            int statusCode = HttpServletResponse.SC_BAD_REQUEST;
            switch (e.getExceptionType()) {
                case ITEM_ALREADY_EXISTS:
                    errorMessage = "Item with this name already exists.";
                    break;
                case ITEM_CREATION_FAILED:
                    errorMessage = "Failed to create item due to a system error.";
                    statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                    break;
                case DATABASE_ERROR:
                    errorMessage = "A database error occurred during item addition.";
                    statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                    break;
                default:
                    errorMessage = "An unexpected business error occurred: " + e.getExceptionType().name();
                    break;
            }
            SendResponse.sendJson(resp, statusCode, Map.of("message", errorMessage));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during POST /items (add item): " + e.getMessage(), e);
            SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Internal server error during item addition."));
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // All roles can view items
        try {
            String idStr = req.getParameter("id"); // Check for ID for single item view
            String name = req.getParameter("name"); // Check for name for single item view

            if (idStr != null && !idStr.trim().isEmpty()) {
                Integer itemId = Integer.parseInt(idStr);
                ItemDTO dto = itemService.searchById(itemId);
                SendResponse.sendJson(resp, HttpServletResponse.SC_OK, itemDtoToMap(dto));
            } else if (name != null && !name.trim().isEmpty()) {
                ItemDTO dto = itemService.searchByName(name);
                SendResponse.sendJson(resp, HttpServletResponse.SC_OK, itemDtoToMap(dto));
            }
            else {
                Map<String, String> searchParams = new HashMap<>();
                String searchTerm = req.getParameter("search");
                if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                    searchParams.put("search", searchTerm);
                }
                List<ItemDTO> list = itemService.getAll(searchParams);
                List<Map<String, Object>> itemMaps = list.stream()
                        .map(this::itemDtoToMap)
                        .collect(Collectors.toList());
                SendResponse.sendJson(resp, HttpServletResponse.SC_OK, itemMaps);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid ID format for GET /items: " + e.getMessage());
            SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Invalid item ID format."));
        } catch (PahanaEduOnlineBillingSystemException e) {
            LOGGER.log(Level.WARNING, "Business error during GET /items: " + e.getExceptionType().name() + " - " + e.getMessage());
            String errorMessage;
            int statusCode = HttpServletResponse.SC_BAD_REQUEST;
            switch (e.getExceptionType()) {
                case ITEM_NOT_FOUND:
                    errorMessage = "Item not found.";
                    statusCode = HttpServletResponse.SC_NOT_FOUND;
                    break;
                case DATABASE_ERROR:
                    errorMessage = "A database error occurred while fetching items.";
                    statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                    break;
                default:
                    errorMessage = "An unexpected business error occurred: " + e.getExceptionType().name();
                    break;
            }
            SendResponse.sendJson(resp, statusCode, Map.of("message", errorMessage));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during GET /items: " + e.getMessage(), e);
            SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Internal server error while fetching items."));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String currentUserRole = getUserRoleFromSession(req);
        Integer currentUserId = getUserIdFromSession(req);

        if (!"ADMIN".equals(currentUserRole)) { // Only admins can update items
            SendResponse.sendJson(resp, HttpServletResponse.SC_FORBIDDEN, Map.of("message", "Unauthorized: Only admins can update items."));
            return;
        }
        if (currentUserId == null) {
            SendResponse.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, Map.of("message", "Unauthorized: Please log in."));
            return;
        }

        try {
            Map<String, String> requestBodyParams = parseUrlEncodedBody(req);
            String action = requestBodyParams.get("action"); // FIX: Get action from parsed body

            ItemDTO dto = new ItemDTO();
            String idStr = requestBodyParams.get("id");
            if (idStr != null && !idStr.isEmpty()) {
                dto.setId(Integer.parseInt(idStr));
            } else {
                SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Item ID is required for update."));
                return;
            }

            if ("restock".equals(action)) {
                // Handle Restock operation
                String quantityToAddStr = requestBodyParams.get("quantityToAdd");
                if (quantityToAddStr == null || quantityToAddStr.isEmpty()) {
                    SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Quantity to add is required for restock."));
                    return;
                }
                int quantityToAdd = Integer.parseInt(quantityToAddStr);

                boolean isRestocked = itemService.restockItem(dto.getId(), quantityToAdd, currentUserId);
                if (isRestocked) {
                    ItemDTO restockedItem = itemService.searchById(dto.getId());
                    SendResponse.sendJson(resp, HttpServletResponse.SC_OK, Map.of("message", "Item restocked successfully", "item", itemDtoToMap(restockedItem)));
                } else {
                    SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Item restock failed unexpectedly."));
                }

            } else if ("update".equals(action)) {
                // Handle general Update operation
                dto.setName(requestBodyParams.get("name"));
                String unitPriceStr = requestBodyParams.get("unitPrice");
                String stockQuantityStr = requestBodyParams.get("stockQuantity");

                if (dto.getName() == null || dto.getName().trim().isEmpty() ||
                        unitPriceStr == null || unitPriceStr.trim().isEmpty() ||
                        stockQuantityStr == null || stockQuantityStr.trim().isEmpty()) {
                    SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Missing required item fields for update."));
                    return;
                }

                dto.setUnitPrice(new BigDecimal(unitPriceStr));
                dto.setStockQuantity(Integer.parseInt(stockQuantityStr));
                dto.setUpdatedBy(currentUserId);

                boolean isUpdated = itemService.update(dto);
                if (isUpdated) {
                    ItemDTO updatedItem = itemService.searchById(dto.getId()); // Fetch by ID after update
                    SendResponse.sendJson(resp, HttpServletResponse.SC_OK, Map.of("message", "Item updated successfully", "item", itemDtoToMap(updatedItem)));
                } else {
                    SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Item update failed unexpectedly."));
                }
            } else {
                SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Invalid action specified for PUT request."));
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid number format for unitPrice or stockQuantity: " + e.getMessage());
            SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Invalid format for price or quantity. Please enter valid numbers."));
        } catch (PahanaEduOnlineBillingSystemException e) {
            LOGGER.log(Level.WARNING, "Business error during item update: " + e.getExceptionType().name() + " - " + e.getMessage());
            String errorMessage;
            int statusCode = HttpServletResponse.SC_BAD_REQUEST;
            switch (e.getExceptionType()) {
                case ITEM_NOT_FOUND:
                    errorMessage = "Item not found for update.";
                    statusCode = HttpServletResponse.SC_NOT_FOUND;
                    break;
                case ITEM_ALREADY_EXISTS:
                    errorMessage = "Item with this name already exists.";
                    break;
                case ITEM_UPDATE_FAILED:
                    errorMessage = "Failed to update item due to a system error.";
                    statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                    break;
                case DATABASE_ERROR:
                    errorMessage = "A database error occurred during item update.";
                    statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                    break;
                default:
                    errorMessage = "An unexpected business error occurred: " + e.getExceptionType().name();
                    break;
            }
            SendResponse.sendJson(resp, statusCode, Map.of("message", errorMessage));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during PUT /items (update item): " + e.getMessage(), e);
            SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Internal server error during item update."));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String currentUserRole = getUserRoleFromSession(req);
        Integer currentUserId = getUserIdFromSession(req);

        // Only Initial Admin (ID = 1) can delete items
        if (!"ADMIN".equals(currentUserRole) || (currentUserId != null && currentUserId != INITIAL_ADMIN_ID)) {
            SendResponse.sendJson(resp, HttpServletResponse.SC_FORBIDDEN, Map.of("message", "Unauthorized: Only the Initial Admin can delete items."));
            return;
        }
        if (currentUserId == null) {
            SendResponse.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, Map.of("message", "Unauthorized: Please log in."));
            return;
        }

        String idStr = req.getParameter("id"); // Item ID comes as query param for DELETE

        if (idStr == null || idStr.trim().isEmpty()) {
            SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Item ID is required for deletion."));
            return;
        }

        try {
            Integer itemId = Integer.parseInt(idStr);
            boolean isDeleted = itemService.delete(currentUserId, itemId); // Pass deleter ID and item ID
            if (isDeleted) {
                SendResponse.sendJson(resp, HttpServletResponse.SC_OK, Map.of("message", "Item deleted successfully."));
            } else {
                SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Item deletion failed unexpectedly."));
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid item ID format for deletion: " + e.getMessage());
            SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Invalid item ID format."));
        } catch (PahanaEduOnlineBillingSystemException e) {
            LOGGER.log(Level.WARNING, "Business error during item delete: " + e.getExceptionType().name() + " - " + e.getMessage());
            String errorMessage;
            int statusCode = HttpServletResponse.SC_BAD_REQUEST;
            switch (e.getExceptionType()) {
                case ITEM_NOT_FOUND:
                    errorMessage = "Item not found for deletion.";
                    statusCode = HttpServletResponse.SC_NOT_FOUND;
                    break;
                case ITEM_DELETION_FAILED:
                    errorMessage = "Failed to delete item due to a system error.";
                    statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                    break;
                case DATABASE_ERROR:
                    errorMessage = "A database error occurred during item deletion.";
                    statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                    break;
                default:
                    errorMessage = "An unexpected business error occurred: " + e.getExceptionType().name();
                    break;
            }
            SendResponse.sendJson(resp, statusCode, Map.of("message", errorMessage));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during DELETE /items: " + e.getMessage(), e);
            SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Internal server error during item deletion."));
        }
    }
}