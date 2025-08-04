package com.icbt.pahanaeduonlinebillingsystem.user.servlet;

import com.icbt.pahanaeduonlinebillingsystem.common.exception.PahanaEduOnlineBillingSystemException;
import com.icbt.pahanaeduonlinebillingsystem.common.util.LogUtil;
import com.icbt.pahanaeduonlinebillingsystem.user.dto.UserDTO;
import com.icbt.pahanaeduonlinebillingsystem.user.service.UserService;
import com.icbt.pahanaeduonlinebillingsystem.user.service.impl.UserServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Thisara Kavishka
 * @date 2025-07-20
 * @since 1.0
 */
@WebServlet(name = "UserServlet", urlPatterns = "/users")
public class UserServlet extends HttpServlet {

    private UserService userService;
    private static final Logger LOGGER = LogUtil.getLogger(UserServlet.class);

    @Override
    public void init() {
        userService = new UserServiceImpl();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
            switch (e.getExceptionType()) {
                case INVALID_CREDENTIALS:
                    errorMessage = "Invalid username or password.";
                    break;
                case USER_NOT_FOUND:
                    errorMessage = "User not found.";
                    break;
                case DATABASE_ERROR:
                    errorMessage = "A database error occurred. Please try again later.";
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("logout".equals(action)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                String username = (String) session.getAttribute("username");
                session.invalidate(); // clear current session
                LOGGER.log(Level.INFO, "User '" + username + "' logged out successfully.");
            }
            response.sendRedirect(request.getContextPath() + "index.jsp");
        }
    }

}
