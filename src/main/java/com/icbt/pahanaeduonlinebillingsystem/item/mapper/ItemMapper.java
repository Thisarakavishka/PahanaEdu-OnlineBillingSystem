package com.icbt.pahanaeduonlinebillingsystem.item.mapper;

import com.icbt.pahanaeduonlinebillingsystem.item.dto.ItemDTO;
import com.icbt.pahanaeduonlinebillingsystem.item.entity.ItemEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Thisara Kavishka
 * @date 2025-08-09
 * @since 1.0
 */
public class ItemMapper {

    private ItemMapper() {
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

    public static Map<String, Object> toMap(ItemDTO dto, String createdByUsername, String updatedByUsername, String deletedByUsername) {
        if (dto == null) return null;
        Map<String, Object> map = new HashMap<>();
        map.put("id", dto.getId());
        map.put("name", dto.getName());
        map.put("unitPrice", dto.getUnitPrice());
        map.put("stockQuantity", dto.getStockQuantity());
        map.put("createdBy", createdByUsername != null ? createdByUsername : (dto.getCreatedBy() != null ? String.valueOf(dto.getCreatedBy()) : "-"));
        map.put("createdAt", dto.getCreatedAt());
        map.put("updatedBy", updatedByUsername != null ? updatedByUsername : (dto.getUpdatedBy() != null ? String.valueOf(dto.getUpdatedBy()) : "-"));
        map.put("updatedAt", dto.getUpdatedAt());
        map.put("deletedBy", deletedByUsername != null ? deletedByUsername : (dto.getDeletedBy() != null ? String.valueOf(dto.getDeletedBy()) : "-"));
        map.put("deletedAt", dto.getDeletedAt());
        return map;
    }

    public static ItemEntity mapResultSetToItemEntity(ResultSet resultSet) throws SQLException {
        ItemEntity entity = new ItemEntity();
        entity.setId(resultSet.getInt("id"));
        entity.setName(resultSet.getString("name"));
        entity.setUnitPrice(resultSet.getBigDecimal("unit_price"));
        entity.setStockQuantity(resultSet.getInt("stock_quantity"));

        entity.setCreatedBy(resultSet.getObject("created_by", Integer.class));
        entity.setCreatedAt(resultSet.getTimestamp("created_at"));
        entity.setUpdatedBy(resultSet.getObject("updated_by", Integer.class));
        entity.setUpdatedAt(resultSet.getTimestamp("updated_at"));
        entity.setDeletedBy(resultSet.getObject("deleted_by", Integer.class));
        entity.setDeletedAt(resultSet.getTimestamp("deleted_at"));
        return entity;
    }
}
