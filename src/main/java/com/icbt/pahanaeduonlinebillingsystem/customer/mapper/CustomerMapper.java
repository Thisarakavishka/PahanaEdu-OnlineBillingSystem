package com.icbt.pahanaeduonlinebillingsystem.customer.mapper;

import com.icbt.pahanaeduonlinebillingsystem.customer.dto.CustomerDTO;
import com.icbt.pahanaeduonlinebillingsystem.customer.entity.CustomerEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Thisara Kavishka
 * @date 2025-07-19
 * @since 1.0
 */
public class CustomerMapper {

    public static CustomerEntity toEntity(CustomerDTO dto) {
        if (dto == null) {
            return null;
        }

        CustomerEntity entity = new CustomerEntity();
        entity.setId(dto.getId());
        entity.setAccountNumber(dto.getAccountNumber());
        entity.setName(dto.getName());
        entity.setAddress(dto.getAddress());
        entity.setPhone(dto.getPhone());
        entity.setUnitsConsumed(dto.getUnitsConsumed());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedAt(dto.getUpdatedAt());
        entity.setDeletedBy(dto.getDeletedBy());
        entity.setDeletedAt(dto.getDeletedAt());

        return entity;
    }

    public static CustomerDTO toDto(CustomerEntity entity) {
        if (entity == null) {
            return null;
        }

        CustomerDTO dto = new CustomerDTO();
        dto.setId(entity.getId());
        dto.setAccountNumber(entity.getAccountNumber());
        dto.setName(entity.getName());
        dto.setAddress(entity.getAddress());
        dto.setPhone(entity.getPhone());
        dto.setUnitsConsumed(entity.getUnitsConsumed());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setDeletedBy(entity.getDeletedBy());
        dto.setDeletedAt(entity.getDeletedAt());

        return dto;
    }

    public static List<CustomerDTO> toDTOList(List<CustomerEntity> entities) {
        List<CustomerDTO> dtos = new ArrayList<>();
        if (entities != null) {
            for (CustomerEntity entity : entities) {
                dtos.add(toDto(entity));
            }
        }
        return dtos;
    }

    public static Map<String, Object> toMap(CustomerDTO dto, String createdByUsername, String updatedByUsername, String deletedByUsername) {
        if (dto == null) {
            return null;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("id", dto.getId());
        map.put("accountNumber", dto.getAccountNumber());
        map.put("name", dto.getName());
        map.put("address", dto.getAddress());
        map.put("phone", dto.getPhone());
        map.put("unitsConsumed", dto.getUnitsConsumed());
        map.put("createdAt", dto.getCreatedAt());
        map.put("updatedAt", dto.getUpdatedAt());
        map.put("deletedAt", dto.getDeletedAt());

        // Use the provided username if available, otherwise fallback to the ID from the DTO
        map.put("createdBy", createdByUsername != null ? createdByUsername : (dto.getCreatedBy() != null ? String.valueOf(dto.getCreatedBy()) : "-"));
        map.put("updatedBy", updatedByUsername != null ? updatedByUsername : (dto.getUpdatedBy() != null ? String.valueOf(dto.getUpdatedBy()) : "-"));
        map.put("deletedBy", deletedByUsername != null ? deletedByUsername : (dto.getDeletedBy() != null ? String.valueOf(dto.getDeletedBy()) : "-"));

        return map;
    }
}
