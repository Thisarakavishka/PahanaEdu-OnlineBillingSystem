package com.icbt.pahanaeduonlinebillingsystem.item.service;

import com.icbt.pahanaeduonlinebillingsystem.common.base.CrudService;
import com.icbt.pahanaeduonlinebillingsystem.item.dto.ItemDTO;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author Thisara Kavishka
 * @date 2025-08-09
 * @since 1.0
 */
public interface ItemService extends CrudService<ItemDTO> {

    ItemDTO searchByName(String name) throws SQLException, ClassNotFoundException;

    List<ItemDTO> getAll(Map<String, String> searchParams) throws SQLException, ClassNotFoundException;

    int getItemsCount() throws SQLException, ClassNotFoundException;

    boolean restockItem(Integer itemId, int quantityToAdd, Integer updatedBy) throws SQLException, ClassNotFoundException;
}
