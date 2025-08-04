package com.icbt.pahanaeduonlinebillingsystem.common.listener;

import com.icbt.pahanaeduonlinebillingsystem.common.constant.Role;
import com.icbt.pahanaeduonlinebillingsystem.common.exception.PahanaEduOnlineBillingSystemException;
import com.icbt.pahanaeduonlinebillingsystem.user.dto.UserDTO;
import com.icbt.pahanaeduonlinebillingsystem.user.service.UserService;
import com.icbt.pahanaeduonlinebillingsystem.user.service.impl.UserServiceImpl;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Thisara Kavishka
 * @date 2025-08-04
 * @since 1.0
 */
@WebListener
public class AppInitializer implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(AppInitializer.class.getName());
    private UserService userService;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.log(Level.INFO, "Application starting up. Checking for initial admin user...");
        userService = new UserServiceImpl();

        try {
            // Check if any users exist in the database and passing null to getAll() to get non deleted users
            if (userService.getAll(null).isEmpty()) {
                LOGGER.log(Level.INFO, "No users found. Creating initial admin user...");

                UserDTO adminUser = new UserDTO();
                adminUser.setUsername("admin");
                adminUser.setPassword("admin123"); // Default password for initial setup
                adminUser.setRole(Role.ADMIN);
                adminUser.setCreatedBy(null); // No user created this initial admin

                boolean isAddAdminUser = userService.add(adminUser);
                if (isAddAdminUser) {
                    LOGGER.log(Level.INFO, "Initial admin user 'admin' created successfully!");
                } else {
                    LOGGER.log(Level.SEVERE, "Failed to create initial admin user 'admin'.");
                }
            } else {
                LOGGER.log(Level.INFO, "Users already exist. Skipping initial admin user creation.");
            }
        } catch (PahanaEduOnlineBillingSystemException e) {
            LOGGER.log(Level.SEVERE, "Error during application startup initialization: " + e.getExceptionType().name() + " - " + e.getMessage(), e);
            throw new RuntimeException("Application initialization failed due to user creation error.", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An unexpected error occurred during application startup: " + e.getMessage(), e);
            throw new RuntimeException("Application initialization failed due to unexpected error.", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.log(Level.INFO, "Application shutting down.");
    }
}
