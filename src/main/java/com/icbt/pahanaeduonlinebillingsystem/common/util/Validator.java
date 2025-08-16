package com.icbt.pahanaeduonlinebillingsystem.common.util;

import com.icbt.pahanaeduonlinebillingsystem.bill.dto.BillDTO;
import com.icbt.pahanaeduonlinebillingsystem.bill.dto.BillDetailDTO;
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
}
