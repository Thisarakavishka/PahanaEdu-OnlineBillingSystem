package com.icbt.pahanaeduonlinebillingsystem.item.servlet;

import com.icbt.pahanaeduonlinebillingsystem.common.exception.PahanaEduOnlineBillingSystemException;
import com.icbt.pahanaeduonlinebillingsystem.common.util.LogUtil;
import com.icbt.pahanaeduonlinebillingsystem.common.util.SendResponse;
import com.icbt.pahanaeduonlinebillingsystem.common.util.ServletUtil;
import com.icbt.pahanaeduonlinebillingsystem.common.util.Validator;
import com.icbt.pahanaeduonlinebillingsystem.item.dto.ItemDTO;
import com.icbt.pahanaeduonlinebillingsystem.item.mapper.ItemMapper;
import com.icbt.pahanaeduonlinebillingsystem.item.service.ItemService;
import com.icbt.pahanaeduonlinebillingsystem.item.service.impl.ItemServiceImpl;
import com.icbt.pahanaeduonlinebillingsystem.user.dto.UserDTO;
import com.icbt.pahanaeduonlinebillingsystem.user.service.UserService;
import com.icbt.pahanaeduonlinebillingsystem.user.service.impl.UserServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
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
    private UserService userService;
    private static final Logger LOGGER = LogUtil.getLogger(ItemServlet.class);
    private static final int INITIAL_ADMIN_ID = 1;

    @Override
    public void init() {
        itemService = new ItemServiceImpl();
        userService = new UserServiceImpl();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String currentUserRole = ServletUtil.getUserRoleFromSession(req);
        Integer currentUserId = ServletUtil.getUserIdFromSession(req);

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

            if (dto.getName() == null || dto.getName().trim().isEmpty() ||
                    unitPriceStr == null || unitPriceStr.trim().isEmpty() ||
                    stockQuantityStr == null || stockQuantityStr.trim().isEmpty()) {
                SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Missing required item fields (name, unitPrice, stockQuantity)."));
                return;
            }

            dto.setUnitPrice(new BigDecimal(unitPriceStr));
            dto.setStockQuantity(Integer.parseInt(stockQuantityStr));
            dto.setCreatedBy(currentUserId);

            Map<String, String> errors = Validator.itemValidate(dto);
            if (!errors.isEmpty()) {
                LOGGER.log(Level.WARNING, "Item creation validation failed: " + errors);
                SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Invalid item data provided.", "errors", errors));
                return;
            }

            boolean isAdded = itemService.add(dto);
            if (isAdded) {
                ItemDTO addedItem = itemService.searchByName(dto.getName());
                SendResponse.sendJson(resp, HttpServletResponse.SC_CREATED, Map.of("message", "Item added successfully", "item", ItemMapper.toMap(addedItem, null, null, null)));
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
        try {
            String idStr = req.getParameter("id");
            String name = req.getParameter("name");

            if (idStr != null && !idStr.trim().isEmpty()) {
                Integer itemId = Integer.parseInt(idStr);
                ItemDTO dto = itemService.searchById(itemId);

                // Fetch usernames for audit fields
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

                SendResponse.sendJson(resp, HttpServletResponse.SC_OK, ItemMapper.toMap(dto, createdByUsername, updatedByUsername, deletedByUsername));
            } else if (name != null && !name.trim().isEmpty()) {
                ItemDTO dto = itemService.searchByName(name);
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
                SendResponse.sendJson(resp, HttpServletResponse.SC_OK, ItemMapper.toMap(dto, createdByUsername, updatedByUsername, deletedByUsername));
            } else {
                Map<String, String> searchParams = new HashMap<>();
                String searchTerm = req.getParameter("search");
                if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                    searchParams.put("search", searchTerm);
                }
                List<ItemDTO> list = itemService.getAll(searchParams);
                List<Map<String, Object>> itemMaps = list.stream()
                        .map(item -> ItemMapper.toMap(item, null, null, null)) // Pass null for usernames in list view
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
        String currentUserRole = ServletUtil.getUserRoleFromSession(req);
        Integer currentUserId = ServletUtil.getUserIdFromSession(req);

        if (!"ADMIN".equals(currentUserRole)) {
            SendResponse.sendJson(resp, HttpServletResponse.SC_FORBIDDEN, Map.of("message", "Unauthorized: Only admins can update items."));
            return;
        }
        if (currentUserId == null) {
            SendResponse.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, Map.of("message", "Unauthorized: Please log in."));
            return;
        }

        try {
            Map<String, String> requestBodyParams = ServletUtil.parseUrlEncodedBody(req);
            String action = requestBodyParams.get("action");

            ItemDTO dto = new ItemDTO();
            String idStr = requestBodyParams.get("id");
            if (idStr != null && !idStr.isEmpty()) {
                dto.setId(Integer.parseInt(idStr));
            } else {
                SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Item ID is required for update."));
                return;
            }

            if ("restock".equals(action)) {
                String quantityToAddStr = requestBodyParams.get("quantityToAdd");
                int quantityToAdd = Integer.parseInt(quantityToAddStr);

                Map<String, String> errors = Validator.restockValidate(quantityToAdd);
                if (!errors.isEmpty()) {
                    LOGGER.log(Level.WARNING, "Item restock validation failed: " + errors);
                    SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Invalid restock data.", "errors", errors));
                    return;
                }

                boolean isRestocked = itemService.restockItem(dto.getId(), quantityToAdd, currentUserId);
                if (isRestocked) {
                    ItemDTO restockedItem = itemService.searchById(dto.getId());
                    SendResponse.sendJson(resp, HttpServletResponse.SC_OK, Map.of("message", "Item restocked successfully", "item", ItemMapper.toMap(restockedItem, null, null, null)));
                } else {
                    SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Item restock failed unexpectedly."));
                }

            } else if ("update".equals(action)) {
                dto.setName(requestBodyParams.get("name"));
                String unitPriceStr = requestBodyParams.get("unitPrice");
                String stockQuantityStr = requestBodyParams.get("stockQuantity");

                dto.setUnitPrice(new BigDecimal(unitPriceStr));
                dto.setStockQuantity(Integer.parseInt(stockQuantityStr));
                dto.setUpdatedBy(currentUserId);

                Map<String, String> errors = Validator.itemValidate(dto);
                if (!errors.isEmpty()) {
                    LOGGER.log(Level.WARNING, "Item update validation failed: " + errors);
                    SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Invalid item data provided.", "errors", errors));
                    return;
                }

                boolean isUpdated = itemService.update(dto);
                if (isUpdated) {
                    ItemDTO updatedItem = itemService.searchById(dto.getId());
                    SendResponse.sendJson(resp, HttpServletResponse.SC_OK, Map.of("message", "Item updated successfully", "item", ItemMapper.toMap(updatedItem, null, null, null)));
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
        String currentUserRole = ServletUtil.getUserRoleFromSession(req);
        Integer currentUserId = ServletUtil.getUserIdFromSession(req);

        if (!"ADMIN".equals(currentUserRole) || (currentUserId != null && currentUserId != INITIAL_ADMIN_ID)) {
            SendResponse.sendJson(resp, HttpServletResponse.SC_FORBIDDEN, Map.of("message", "Unauthorized: Only the Initial Admin can delete items."));
            return;
        }
        if (currentUserId == null) {
            SendResponse.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, Map.of("message", "Unauthorized: Please log in."));
            return;
        }

        String idStr = req.getParameter("id");

        if (idStr == null || idStr.trim().isEmpty()) {
            SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Item ID is required for deletion."));
            return;
        }

        try {
            Integer itemId = Integer.parseInt(idStr);
            boolean isDeleted = itemService.delete(currentUserId, itemId);
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