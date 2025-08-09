package com.icbt.pahanaeduonlinebillingsystem.item.converter;

import com.icbt.pahanaeduonlinebillingsystem.item.dto.ItemDTO;
import com.icbt.pahanaeduonlinebillingsystem.item.entity.ItemEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Thisara Kavishka
 * @date 2025-08-09
 * @since 1.0
 */
public class ItemConverter {

    private ItemConverter() {
    }

    public static ItemEntity toEntity(ItemDTO dto) {
        if (dto == null) {
            return null;
        }

        ItemEntity entity = new ItemEntity();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setUnitPrice(dto.getUnitPrice());
        entity.setStockQuantity(dto.getStockQuantity());

        // Audit fields
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedAt(dto.getUpdatedAt());
        entity.setDeletedBy(dto.getDeletedBy());
        entity.setDeletedAt(dto.getDeletedAt());

        return entity;
    }

    public static ItemDTO toDto(ItemEntity entity) {
        if (entity == null) {
            return null;
        }

        ItemDTO dto = new ItemDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setUnitPrice(entity.getUnitPrice());
        dto.setStockQuantity(entity.getStockQuantity());

        // Audit fields
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setDeletedBy(entity.getDeletedBy());
        dto.setDeletedAt(entity.getDeletedAt());

        return dto;
    }

    public static List<ItemDTO> toDTOList(List<ItemEntity> entities) {
        List<ItemDTO> dtos = new ArrayList<>();
        if (entities != null) {
            for (ItemEntity entity : entities) {
                dtos.add(toDto(entity));
            }
        }
        return dtos;
    }
}
