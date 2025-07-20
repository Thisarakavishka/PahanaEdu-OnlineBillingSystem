package com.icbt.pahanaeduonlinebillingsystem.util.converter;

import com.icbt.pahanaeduonlinebillingsystem.dto.CustomerDTO;
import com.icbt.pahanaeduonlinebillingsystem.entity.CustomerEntity;

/**
 * @author Thisara Kavishka
 * @date 2025-07-19
 * @since 1.0
 */
public class CustomerConverter {

    public static CustomerEntity toEntity(CustomerDTO dto) {
        if (dto == null) {
            return null;
        }

        CustomerEntity entity = new CustomerEntity();
        entity.setId(dto.getId());
        entity.setAccountNumber(dto.getAccountNumber());
        entity.setName(dto.getName());
        entity.setPhone(dto.getPhone());
        entity.setUnitsConsumed(dto.getUnitsConsumed());
        entity.setCreatedBy(dto.getCreatedBy());

        return entity;
    }
}
