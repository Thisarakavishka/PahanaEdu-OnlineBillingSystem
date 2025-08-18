package com.icbt.pahanaeduonlinebillingsystem.common.util;

import com.icbt.pahanaeduonlinebillingsystem.bill.dto.BillDTO;
import com.icbt.pahanaeduonlinebillingsystem.bill.dto.BillDetailDTO;
import com.icbt.pahanaeduonlinebillingsystem.customer.dto.CustomerDTO;
import com.icbt.pahanaeduonlinebillingsystem.item.dto.ItemDTO;
import com.icbt.pahanaeduonlinebillingsystem.user.dto.UserDTO;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Thisara Kavishka
 * @date 2025-08-15
 * @since 1.0
 */
public class Validator {

    public static Map<String, String> customerValidate(CustomerDTO dto) {
        Map<String, String> errors = new HashMap<>();

        // Validate Account Number: Cannot be null or empty.
        if (dto.getAccountNumber() == null || dto.getAccountNumber().trim().isEmpty()) {
            errors.put("accountNumber", "Account number cannot be empty.");
        }

        // Validate Name: Cannot be null or empty.
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            errors.put("name", "Name cannot be empty.");
        }

        // Validate Phone Number: Must be exactly 10 digits.
        if (dto.getPhone() == null || dto.getPhone().trim().isEmpty()) {
            errors.put("phone", "Phone number cannot be empty.");
        } else if (!dto.getPhone().matches("^\\d{10}$")) {
            errors.put("phone", "Phone number must be exactly 10 digits.");
        }

        // Validate Units Consumed: Must be a non-negative number.
        if (dto.getUnitsConsumed() < 0) {
            errors.put("unitsConsumed", "Units consumed cannot be negative.");
        }

        return errors;
    }

    public static Map<String, String> billValidate(BillDTO dto) {
        Map<String, String> errors = new HashMap<>();

        // 1. Validate Customer ID: Must be a positive number.
        if (dto.getCustomerId() <= 0) {
            errors.put("customerId", "A valid customer must be selected.");
        }

        // 2. Validate Item List: Must not be null or empty.
        if (dto.getDetails() == null || dto.getDetails().isEmpty()) {
            errors.put("items", "A bill must contain at least one item.");
        } else {
            // 3. Validate Each Item in the List
            for (int i = 0; i < dto.getDetails().size(); i++) {
                BillDetailDTO detail = dto.getDetails().get(i);
                if (detail.getItemId() <= 0) {
                    errors.put("item_" + i + "_id", "An invalid item was included in the bill.");
                }
                if (detail.getUnits() <= 0) {
                    errors.put("item_" + i + "_units", "Item quantity must be at least 1.");
                }
            }
        }

        return errors;
    }

    public static Map<String, String> userValidate(UserDTO dto) {
        Map<String, String> errors = new HashMap<>();

        // 1. Validate Username
        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
            errors.put("username", "Username cannot be empty.");
        } else if (dto.getUsername().trim().length() < 3) {
            errors.put("username", "Username must be at least 3 characters long.");
        }

        // 2. Validate Password (only for new users, so we check if an ID is present)
        if (dto.getId() == 0) { // Assuming ID is 0 for a new, unsaved user
            if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
                errors.put("password", "Password is required for a new user.");
            } else if (dto.getPassword().length() < 6) {
                errors.put("password", "Password must be at least 6 characters long.");
            }
        }

        // 3. Validate Role
        if (dto.getRole() == null) {
            errors.put("role", "A user role must be selected.");
        }

        return errors;
    }

    public static Map<String, String> itemValidate(ItemDTO dto) {
        Map<String, String> errors = new HashMap<>();

        // 1. Validate Name
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            errors.put("name", "Item name cannot be empty.");
        } else if (dto.getName().trim().length() < 3) {
            errors.put("name", "Item name must be at least 3 characters long.");
        }

        // 2. Validate Unit Price
        if (dto.getUnitPrice() == null) {
            errors.put("unitPrice", "Unit price cannot be empty.");
        } else if (dto.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            errors.put("unitPrice", "Unit price cannot be negative.");
        }

        // 3. Validate Stock Quantity
        if (dto.getStockQuantity() < 0) {
            errors.put("stockQuantity", "Stock quantity cannot be negative.");
        }

        return errors;
    }

    public static Map<String, String> restockValidate(int quantityToAdd) {
        Map<String, String> errors = new HashMap<>();
        if (quantityToAdd <= 0) {
            errors.put("quantityToAdd", "Quantity to add must be a positive number.");
        }
        return errors;
    }
}
