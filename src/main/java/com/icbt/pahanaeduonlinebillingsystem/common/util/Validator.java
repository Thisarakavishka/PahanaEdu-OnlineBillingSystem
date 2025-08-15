package com.icbt.pahanaeduonlinebillingsystem.common.util;

import com.icbt.pahanaeduonlinebillingsystem.customer.dto.CustomerDTO;

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
}
