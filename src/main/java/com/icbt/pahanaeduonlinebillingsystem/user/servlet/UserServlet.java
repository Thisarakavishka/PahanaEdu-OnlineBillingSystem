package com.icbt.pahanaeduonlinebillingsystem.user.servlet;

import com.icbt.pahanaeduonlinebillingsystem.common.constant.Role;
import com.icbt.pahanaeduonlinebillingsystem.common.exception.PahanaEduOnlineBillingSystemException;
import com.icbt.pahanaeduonlinebillingsystem.common.util.LogUtil;
import com.icbt.pahanaeduonlinebillingsystem.common.util.SendResponse;
import com.icbt.pahanaeduonlinebillingsystem.user.dto.UserDTO;
import com.icbt.pahanaeduonlinebillingsystem.user.service.UserService;
import com.icbt.pahanaeduonlinebillingsystem.user.service.impl.UserServiceImpl;
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
 * @date 2025-07-20
 * @since 1.0
 */
@WebServlet(name = "UserServlet", urlPatterns = "/users")
public class UserServlet extends HttpServlet {

    private UserService userService;
    private static final Logger LOGGER = LogUtil.getLogger(UserServlet.class);
    private static final int INITIAL_ADMIN_ID = 1; // Constant for the initial admin's ID

    @Override
    public void init() {
        userService = new UserServiceImpl();
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

    // Helper to convert UserDTO to a Map for JSON serialization
    // Now accepts optional username parameters for audit fields
    private Map<String, Object> userDtoToMap(UserDTO dto, String createdByUsername, String updatedByUsername, String deletedByUsername) {
        if (dto == null) return null;
        Map<String, Object> map = new HashMap<>();
        map.put("id", dto.getId());
        map.put("username", dto.getUsername());
        map.put("role", dto.getRole().name()); // Send role as String
        // Use provided usernames, fallback to ID if username not found, or '-' if null
        map.put("createdBy", createdByUsername != null ? createdByUsername : (dto.getCreatedBy() != null ? String.valueOf(dto.getCreatedBy()) : "-"));
        map.put("createdAt", dto.getCreatedAt());
        map.put("updatedBy", updatedByUsername != null ? updatedByUsername : (dto.getUpdatedBy() != null ? String.valueOf(dto.getUpdatedBy()) : "-"));
        map.put("updatedAt", dto.getUpdatedAt());
        map.put("deletedBy", deletedByUsername != null ? deletedByUsername : (dto.getDeletedBy() != null ? String.valueOf(dto.getDeletedBy()) : "-"));
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
        // For POST, parameters are usually from form submission (req.getParameter)
        // We'll rely on the 'action' parameter to distinguish login from add user.
        String action = req.getParameter("action");

        if ("add".equals(action)) {
            // Handle Add User
            Integer currentUserId = getUserIdFromSession(req);
            String currentUserRole = getUserRoleFromSession(req);

            if (currentUserId == null || !"ADMIN".equals(currentUserRole)) {
                SendResponse.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, Map.of("message", "Unauthorized: Only admins can add users."));
                return;
            }

            try {
                UserDTO dto = new UserDTO();
                dto.setUsername(req.getParameter("username"));
                dto.setPassword(req.getParameter("password")); // This is plain password from form
                dto.setRole(Role.valueOf(req.getParameter("role").toUpperCase()));
                dto.setCreatedBy(currentUserId);

                // Basic validation (more comprehensive validation is in frontend and service)
                if (dto.getUsername() == null || dto.getUsername().trim().isEmpty() ||
                        dto.getPassword() == null || dto.getPassword().trim().isEmpty() || // Password is required for add
                        dto.getRole() == null) {
                    SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Missing required user fields (username, password, role)."));
                    return;
                }

                boolean isAdded = userService.add(dto);
                if (isAdded) {
                    // Fetch the newly added user to get its ID and audit timestamps
                    UserDTO addedUser = userService.searchByUsername(dto.getUsername());
                    SendResponse.sendJson(resp, HttpServletResponse.SC_CREATED, Map.of("message", "User added successfully", "user", userDtoToMap(addedUser, null, null, null)));
                } else {
                    SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "User addition failed unexpectedly."));
                }
            } catch (PahanaEduOnlineBillingSystemException e) {
                LOGGER.log(Level.WARNING, "Business error during user add: " + e.getExceptionType().name() + " - " + e.getMessage());
                String errorMessage;
                int statusCode = HttpServletResponse.SC_BAD_REQUEST; // Default for business errors
                switch (e.getExceptionType()) {
                    case USER_ALREADY_EXISTS:
                        errorMessage = "User with this username already exists.";
                        break;
                    case USER_CREATION_FAILED:
                        errorMessage = "Failed to create user due to a system error.";
                        statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                        break;
                    case DATABASE_ERROR:
                        errorMessage = "A database error occurred during user addition.";
                        statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                        break;
                    default:
                        errorMessage = "An unexpected business error occurred: " + e.getExceptionType().name();
                        break;
                }
                SendResponse.sendJson(resp, statusCode, Map.of("message", errorMessage));
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Unexpected error during POST /users (add user): " + e.getMessage(), e);
                SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Internal server error during user addition."));
            }
        } else {
            // Handle Login (original doPost logic)
            String username = req.getParameter("username");
            String password = req.getParameter("password");

            try {
                UserDTO authenticatedUser = userService.authenticate(username, password);

                if (authenticatedUser != null) {
                    HttpSession session = req.getSession();
                    session.setAttribute("username", authenticatedUser.getUsername());
                    session.setAttribute("role", authenticatedUser.getRole().name());
                    session.setAttribute("userId", authenticatedUser.getId());

                    LOGGER.log(Level.INFO, "User '" + username + "' logged in successfully with role: " + authenticatedUser.getRole().name());
                    resp.sendRedirect("dashboard.jsp");
                } else {
                    req.setAttribute("error", "Invalid credentials. Please try again.");
                    LOGGER.log(Level.WARNING, "Login attempt failed for username: " + username + " - Authentication returned null.");
                    req.getRequestDispatcher("index.jsp").forward(req, resp);
                }
            } catch (PahanaEduOnlineBillingSystemException e) {
                LOGGER.log(Level.WARNING, "Login attempt failed for username: " + username + " - " + e.getExceptionType().name() + ": " + e.getMessage());
                String errorMessage;
                int statusCode = HttpServletResponse.SC_BAD_REQUEST; // Default for business errors
                switch (e.getExceptionType()) {
                    case INVALID_CREDENTIALS:
                        errorMessage = "Invalid username or password.";
                        break;
                    case USER_NOT_FOUND:
                        errorMessage = "User not found.";
                        statusCode = HttpServletResponse.SC_NOT_FOUND; // More appropriate for not found
                        break;
                    case DATABASE_ERROR:
                        errorMessage = "A database error occurred. Please try again later.";
                        statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                        break;
                    default:
                        errorMessage = "An unexpected error occurred during login.";
                        break;
                }
                req.setAttribute("error", errorMessage);
                req.getRequestDispatcher("index.jsp").forward(req, resp);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "An unexpected server error occurred during login for username: " + username + " - " + e.getMessage(), e);
                req.setAttribute("error", "An unexpected server error occurred. Please try again.");
                req.getRequestDispatcher("index.jsp").forward(req, resp);
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        String currentUserRole = getUserRoleFromSession(req);
        Integer currentUserId = getUserIdFromSession(req); // FIX: Initialize currentUserId here

        if (action == null) {
            SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Action parameter is missing."));
            return;
        }

        switch (action) {
            case "logout":
                HttpSession session = req.getSession(false);
                if (session != null) {
                    String username = (String) session.getAttribute("username");
                    session.invalidate(); // clear current session
                    LOGGER.log(Level.INFO, "User '" + username + "' logged out successfully.");
                }
                resp.sendRedirect(req.getContextPath() + "/index.jsp"); // Corrected context path usage
                break;

            case "list":
                // Users with role "USER" should only see their own row, not the full list.
                // This is handled in the frontend's renderUsers function.
                // Backend still provides all data for ADMIN to filter.
                if (!"ADMIN".equals(currentUserRole) && !"USER".equals(currentUserRole)) { // Only ADMIN and USER roles can list (USER will filter on frontend)
                    SendResponse.sendJson(resp, HttpServletResponse.SC_FORBIDDEN, Map.of("message", "Forbidden: You do not have permission to view users."));
                    return;
                }
                try {
                    Map<String, String> searchParams = new HashMap<>();
                    String searchTerm = req.getParameter("search");
                    if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                        searchParams.put("search", searchTerm);
                    }
                    List<UserDTO> users = userService.getAll(searchParams);
                    List<Map<String, Object>> userMaps = users.stream()
                            .map(user -> {
                                // For list view, we don't fetch createdBy/updatedBy usernames for every user
                                // to avoid N+1 query problem. Frontend can display IDs or '-'
                                return userDtoToMap(user, null, null, null);
                            })
                            .collect(Collectors.toList());
                    SendResponse.sendJson(resp, HttpServletResponse.SC_OK, userMaps);
                } catch (PahanaEduOnlineBillingSystemException e) {
                    LOGGER.log(Level.WARNING, "Business error during GET /users (list): " + e.getExceptionType().name() + " - " + e.getMessage());
                    SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Error fetching users: " + e.getMessage()));
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Unexpected error during GET /users (list): " + e.getMessage(), e);
                    SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Internal server error while fetching users."));
                }
                break;

            case "searchById":
                // All roles can view their own details. Admins can view any.
                // Backend needs to verify if the requested ID matches current user ID if role is USER.
                if (currentUserId == null) {
                    SendResponse.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, Map.of("message", "Unauthorized: Please log in."));
                    return;
                }
                try {
                    String idStr = req.getParameter("id");
                    if (idStr == null || idStr.isEmpty()) {
                        SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "User ID is required."));
                        return;
                    }
                    Integer userIdToSearch = Integer.parseInt(idStr);

                    // Authorization check for searchById
                    if ("USER".equals(currentUserRole) && !userIdToSearch.equals(currentUserId)) {
                        SendResponse.sendJson(resp, HttpServletResponse.SC_FORBIDDEN, Map.of("message", "Forbidden: Users can only view their own profile."));
                        return;
                    }

                    UserDTO user = userService.searchById(userIdToSearch);
                    if (user != null) {
                        // Fetch usernames for audit fields for single view
                        String createdByUsername = null;
                        String updatedByUsername = null;
                        String deletedByUsername = null;

                        if (user.getCreatedBy() != null) {
                            UserDTO creator = userService.searchById(user.getCreatedBy());
                            if (creator != null) createdByUsername = creator.getUsername();
                        }
                        if (user.getUpdatedBy() != null) {
                            UserDTO updater = userService.searchById(user.getUpdatedBy());
                            if (updater != null) updatedByUsername = updater.getUsername();
                        }
                        if (user.getDeletedBy() != null) {
                            UserDTO deleter = userService.searchById(user.getDeletedBy());
                            if (deleter != null) deletedByUsername = deleter.getUsername();
                        }

                        SendResponse.sendJson(resp, HttpServletResponse.SC_OK, userDtoToMap(user, createdByUsername, updatedByUsername, deletedByUsername));
                    } else {
                        SendResponse.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, Map.of("message", "User not found."));
                    }
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.WARNING, "Invalid user ID format: " + e.getMessage());
                    SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Invalid user ID format."));
                } catch (PahanaEduOnlineBillingSystemException e) {
                    LOGGER.log(Level.WARNING, "Business error during GET /users (searchById): " + e.getExceptionType().name() + " - " + e.getMessage());
                    SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Error searching user: " + e.getMessage()));
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Unexpected error during GET /users (searchById): " + e.getMessage(), e);
                    SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Internal server error while searching user."));
                }
                break;

            default:
                SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Invalid action specified."));
                break;
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, String> requestBodyParams = parseUrlEncodedBody(req);
        String action = requestBodyParams.get("action");

        String currentUserRole = getUserRoleFromSession(req);
        Integer currentUserId = getUserIdFromSession(req);

        if (currentUserId == null) {
            SendResponse.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, Map.of("message", "Unauthorized: Please log in."));
            return;
        }

        if ("update".equals(action)) {
            try {
                UserDTO dto = new UserDTO();
                String idStr = requestBodyParams.get("id");
                if (idStr != null && !idStr.isEmpty()) {
                    dto.setId(Integer.parseInt(idStr));
                } else {
                    SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "User ID is required for update."));
                    return;
                }

                // Server-side Authorization for Edit
                // Fetch the user being edited to check their role and ID
                UserDTO userToEdit = userService.searchById(dto.getId());
                if (userToEdit == null) {
                    SendResponse.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, Map.of("message", "User to update not found."));
                    return;
                }

                if ("ADMIN".equals(currentUserRole)) {
                    if (userToEdit.getId() == INITIAL_ADMIN_ID) { // If target is initial admin (primitive int comparison)
                        if (currentUserId != INITIAL_ADMIN_ID) { // And current user is NOT initial admin (primitive int comparison)
                            SendResponse.sendJson(resp, HttpServletResponse.SC_FORBIDDEN, Map.of("message", "Forbidden: Only the Initial Admin can edit themselves. Other admins cannot edit the Initial Admin."));
                            return;
                        }
                    }
                } else if ("USER".equals(currentUserRole)) { // If current user is a regular user
                    if (userToEdit.getId() != currentUserId) { // And target is NOT themselves (primitive int comparison)
                        SendResponse.sendJson(resp, HttpServletResponse.SC_FORBIDDEN, Map.of("message", "Forbidden: Users can only edit their own profile."));
                        return;
                    }
                } else { // Unknown role or no role
                    SendResponse.sendJson(resp, HttpServletResponse.SC_FORBIDDEN, Map.of("message", "Forbidden: You do not have permission to edit users."));
                    return;
                }


                dto.setUsername(requestBodyParams.get("username"));
                String password = requestBodyParams.get("password");
                if (password != null && !password.isEmpty()) {
                    dto.setPassword(password);
                }
                dto.setRole(Role.valueOf(requestBodyParams.get("role").toUpperCase()));
                dto.setUpdatedBy(currentUserId);

                if (dto.getUsername() == null || dto.getUsername().trim().isEmpty() || dto.getRole() == null) {
                    SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Missing required user fields for update."));
                    return;
                }

                boolean isUpdated = userService.update(dto);
                if (isUpdated) {
                    UserDTO updatedUser = userService.searchById(dto.getId());
                    SendResponse.sendJson(resp, HttpServletResponse.SC_OK, Map.of("message", "User updated successfully", "user", userDtoToMap(updatedUser, null, null, null)));
                } else {
                    SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "User update failed unexpectedly."));
                }
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Invalid ID or number format during user update: " + e.getMessage());
                SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Invalid ID or number format."));
            } catch (PahanaEduOnlineBillingSystemException e) {
                LOGGER.log(Level.WARNING, "Business error during user update: " + e.getExceptionType().name() + " - " + e.getMessage());
                String errorMessage;
                int statusCode = HttpServletResponse.SC_BAD_REQUEST;
                switch (e.getExceptionType()) {
                    case USER_NOT_FOUND:
                        errorMessage = "User not found for update.";
                        statusCode = HttpServletResponse.SC_NOT_FOUND;
                        break;
                    case USER_ALREADY_EXISTS: // If username changed to an existing one
                        errorMessage = "Username already exists.";
                        break;
                    case USER_UPDATE_FAILED:
                        errorMessage = "Failed to update user due to a system error.";
                        statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                        break;
                    case DATABASE_ERROR:
                        errorMessage = "A database error occurred during user update.";
                        statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                        break;
                    case UNAUTHORIZED_ACCESS: // For specific access denied by service layer
                        errorMessage = e.getMessage(); // Use message from service layer
                        statusCode = HttpServletResponse.SC_FORBIDDEN;
                        break;
                    default:
                        errorMessage = "An unexpected business error occurred: " + e.getExceptionType().name();
                        break;
                }
                SendResponse.sendJson(resp, statusCode, Map.of("message", errorMessage));
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Unexpected error during PUT /users (update user): " + e.getMessage(), e);
                SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Internal server error during user update."));
            }
        } else {
            SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Invalid action specified for PUT request."));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        String currentUserRole = getUserRoleFromSession(req);
        Integer currentUserId = getUserIdFromSession(req);

        if (currentUserId == null) {
            SendResponse.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED, Map.of("message", "Unauthorized: Please log in."));
            return;
        }

        if (!"ADMIN".equals(currentUserRole)) { // Only admins can delete
            SendResponse.sendJson(resp, HttpServletResponse.SC_FORBIDDEN, Map.of("message", "Forbidden: Only admins can delete users."));
            return;
        }

        if ("delete".equals(action)) {
            String idStr = req.getParameter("id");
            String deletedByStr = req.getParameter("deletedBy"); // The ID of the user performing the delete

            if (idStr == null || idStr.trim().isEmpty() || deletedByStr == null || deletedByStr.trim().isEmpty()) {
                SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "User ID and deleter ID are required for deletion."));
                return;
            }

            try {
                Integer userIdToDelete = Integer.parseInt(idStr);
                Integer deletedByUserId = Integer.parseInt(deletedByStr);

                // Server-side Authorization for Delete
                UserDTO userToDelete = userService.searchById(userIdToDelete);
                if (userToDelete == null) {
                    SendResponse.sendJson(resp, HttpServletResponse.SC_NOT_FOUND, Map.of("message", "User to delete not found."));
                    return;
                }

                if (userToDelete.getId() == INITIAL_ADMIN_ID) { // If target is initial admin (primitive int comparison)
                    SendResponse.sendJson(resp, HttpServletResponse.SC_FORBIDDEN, Map.of("message", "Forbidden: The Initial Admin cannot be deleted."));
                    return;
                } else if ("ADMIN".equals(userToDelete.getRole().name())) { // If target is another admin
                    if (currentUserId != INITIAL_ADMIN_ID) { // And current user is NOT initial admin (primitive int comparison)
                        SendResponse.sendJson(resp, HttpServletResponse.SC_FORBIDDEN, Map.of("message", "Forbidden: Only the Initial Admin can delete other admins."));
                        return;
                    }
                }
                // If target is USER, any admin can delete (no further check needed here)


                boolean isDeleted = userService.delete(userIdToDelete, deletedByUserId);
                if (isDeleted) {
                    SendResponse.sendJson(resp, HttpServletResponse.SC_OK, Map.of("message", "User deleted successfully."));
                } else {
                    SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "User deletion failed unexpectedly."));
                }
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Invalid ID format for user deletion: " + e.getMessage());
                SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Invalid user ID format."));
            } catch (PahanaEduOnlineBillingSystemException e) {
                LOGGER.log(Level.WARNING, "Business error during user delete: " + e.getExceptionType().name() + " - " + e.getMessage());
                String errorMessage;
                int statusCode = HttpServletResponse.SC_BAD_REQUEST;
                switch (e.getExceptionType()) {
                    case USER_NOT_FOUND:
                        errorMessage = "User not found for deletion.";
                        statusCode = HttpServletResponse.SC_NOT_FOUND;
                        break;
                    case UNAUTHORIZED_ACCESS: // Specific for initial admin deletion block
                        errorMessage = e.getMessage(); // Use message from service layer
                        statusCode = HttpServletResponse.SC_FORBIDDEN;
                        break;
                    case USER_DELETION_FAILED:
                        errorMessage = "Failed to delete user due to a system error.";
                        statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                        break;
                    case DATABASE_ERROR:
                        errorMessage = "A database error occurred during user deletion.";
                        statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                        break;
                    default:
                        errorMessage = "An unexpected business error occurred: " + e.getExceptionType().name();
                        break;
                }
                SendResponse.sendJson(resp, statusCode, Map.of("message", errorMessage));
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Unexpected error during DELETE /users: " + e.getMessage(), e);
                SendResponse.sendJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("message", "Internal server error during user deletion."));
            }
        } else {
            SendResponse.sendJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of("message", "Invalid action specified for DELETE request."));
        }
    }
}