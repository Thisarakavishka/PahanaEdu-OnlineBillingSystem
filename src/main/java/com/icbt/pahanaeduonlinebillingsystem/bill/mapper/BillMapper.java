package com.icbt.pahanaeduonlinebillingsystem.bill.mapper;

import com.icbt.pahanaeduonlinebillingsystem.bill.dto.BillDTO;
import com.icbt.pahanaeduonlinebillingsystem.bill.dto.BillDetailDTO;
import com.icbt.pahanaeduonlinebillingsystem.bill.entity.BillDetailEntity;
import com.icbt.pahanaeduonlinebillingsystem.bill.entity.BillEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Thisara Kavishka
 * @date 2025-08-11
 * @since 1.0
 */
public class BillMapper {

    private BillMapper() {
    }

    public static BillEntity toEntity(BillDTO dto) {
        if (dto == null) {
            return null;
        }

        BillEntity entity = new BillEntity();
        entity.setId(dto.getId());
        entity.setCustomerId(dto.getCustomerId());
        entity.setTotalAmount(dto.getTotalAmount());

        if (dto.getDetails() != null) {
            List<BillDetailEntity> detailEntities = new ArrayList<>();
            for (BillDetailDTO detailDto : dto.getDetails()) {
                detailEntities.add(toEntity(detailDto));
            }
            entity.setDetails(detailEntities);
        }

        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setDeletedBy(dto.getDeletedBy());
        entity.setDeletedAt(dto.getDeletedAt());

        return entity;
    }

    public static BillDTO toDto(BillEntity entity) {
        if (entity == null) {
            return null;
        }

        BillDTO dto = new BillDTO();
        dto.setId(entity.getId());
        dto.setCustomerId(entity.getCustomerId());
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setDeletedBy(entity.getDeletedBy());
        dto.setDeletedAt(entity.getDeletedAt());

        return dto;
    }

    public static BillDetailEntity toEntity(BillDetailDTO dto) {
        if (dto == null) {
            return null;
        }
        BillDetailEntity entity = new BillDetailEntity();
        entity.setId(dto.getId());
        entity.setBillId(dto.getBillId());
        entity.setItemId(dto.getItemId());
        entity.setItemNameAtSale(dto.getItemNameAtSale());
        entity.setUnitPriceAtSale(dto.getUnitPriceAtSale());
        entity.setUnits(dto.getUnits());
        entity.setTotal(dto.getTotal());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setDeletedBy(dto.getDeletedBy());
        entity.setDeletedAt(dto.getDeletedAt());
        return entity;
    }

    public static BillDetailDTO toDto(BillDetailEntity entity) {
        if (entity == null) {
            return null;
        }
        BillDetailDTO dto = new BillDetailDTO();
        dto.setId(entity.getId());
        dto.setBillId(entity.getBillId());
        dto.setItemId(entity.getItemId());
        dto.setItemNameAtSale(entity.getItemNameAtSale());
        dto.setUnitPriceAtSale(entity.getUnitPriceAtSale());
        dto.setUnits(entity.getUnits());
        dto.setTotal(entity.getTotal());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setDeletedBy(entity.getDeletedBy());
        dto.setDeletedAt(entity.getDeletedAt());
        return dto;
    }

    public static List<BillDTO> toDTOList(List<BillEntity> entities) {
        List<BillDTO> dtos = new ArrayList<>();
        if (entities != null) {
            for (BillEntity entity : entities) {
                dtos.add(toDto(entity));
            }
        }
        return dtos;
    }

    public static List<BillDetailDTO> toDTOListFromDetails(List<BillDetailEntity> entities) {
        List<BillDetailDTO> dtos = new ArrayList<>();
        if (entities != null) {
            for (BillDetailEntity entity : entities) {
                dtos.add(toDto(entity));
            }
        }
        return dtos;
    }

    public static Map<String, Object> detailToMap(BillDetailDTO dto) {
        if (dto == null) return null;
        Map<String, Object> map = new HashMap<>();
        map.put("id", dto.getId());
        map.put("billId", dto.getBillId());
        map.put("itemId", dto.getItemId());
        map.put("itemNameAtSale", dto.getItemNameAtSale());
        map.put("unitPriceAtSale", dto.getUnitPriceAtSale());
        map.put("units", dto.getUnits());
        map.put("total", dto.getTotal());
        return map;
    }

    public static Map<String, Object> toToMap(BillDTO dto) {
        if (dto == null) return null;
        Map<String, Object> map = new HashMap<>();
        map.put("id", dto.getId());
        map.put("customerId", dto.getCustomerId());
        map.put("customerAccountNumber", dto.getCustomerAccountNumber());
        map.put("customerName", dto.getCustomerName());
        map.put("generatedByUsername", dto.getCreateByUsername());
        map.put("totalAmount", dto.getTotalAmount());
        map.put("generatedAt", dto.getCreatedAt());

        // Convert the list of detail DTOs into a list of maps
        if (dto.getDetails() != null) {
            List<Map<String, Object>> detailMaps = dto.getDetails().stream()
                    .map(BillMapper::detailToMap)
                    .collect(Collectors.toList());
            map.put("details", detailMaps);
        } else {
            map.put("details", new ArrayList<>());
        }
        return map;
    }
}
