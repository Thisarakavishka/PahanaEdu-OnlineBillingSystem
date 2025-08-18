package com.icbt.pahanaeduonlinebillingsystem.common.listener;

import com.icbt.pahanaeduonlinebillingsystem.common.constant.Role;
import com.icbt.pahanaeduonlinebillingsystem.common.exception.PahanaEduOnlineBillingSystemException;
import com.icbt.pahanaeduonlinebillingsystem.common.util.DBUtil;
import com.icbt.pahanaeduonlinebillingsystem.user.dto.UserDTO;
import com.icbt.pahanaeduonlinebillingsystem.user.service.UserService;
import com.icbt.pahanaeduonlinebillingsystem.user.service.impl.UserServiceImpl;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.sql.Connection;
import java.sql.Statement;
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
            if (userService.getAll(null).isEmpty()) {
                LOGGER.log(Level.INFO, "No users found. Preparing to create initial admin user...");

                try (Connection connection = DBUtil.getConnection();
                     Statement statement = connection.createStatement()) {

                    String resetSQL = "ALTER TABLE users AUTO_INCREMENT = 1";
                    statement.executeUpdate(resetSQL);
                    LOGGER.log(Level.INFO, "AUTO_INCREMENT for 'users' table has been reset to 1.");

                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to reset AUTO_INCREMENT for users table.", e);
                    throw new RuntimeException("Failed to prepare database for initial user.", e);
                }

                UserDTO adminUser = new UserDTO();
                adminUser.setUsername("super admin");
                adminUser.setPassword("superadmin123");
                adminUser.setRole(Role.ADMIN);
                adminUser.setCreatedBy(null);

                boolean isAddAdminUser = userService.add(adminUser);
                if (isAddAdminUser) {
                    LOGGER.log(Level.INFO, "Initial admin user 'super admin' created successfully with ID=1!");
                } else {
                    LOGGER.log(Level.SEVERE, "Failed to create initial admin user.");
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
