package com.icbt.pahanaeduonlinebillingsystem.item.service.impl;

import com.icbt.pahanaeduonlinebillingsystem.common.exception.ExceptionType;
import com.icbt.pahanaeduonlinebillingsystem.common.exception.PahanaEduOnlineBillingSystemException;
import com.icbt.pahanaeduonlinebillingsystem.common.util.DBUtil;
import com.icbt.pahanaeduonlinebillingsystem.common.util.LogUtil;
import com.icbt.pahanaeduonlinebillingsystem.item.mapper.ItemMapper;
import com.icbt.pahanaeduonlinebillingsystem.item.dao.ItemDAO;
import com.icbt.pahanaeduonlinebillingsystem.item.dao.impl.ItemDAOImpl;
import com.icbt.pahanaeduonlinebillingsystem.item.dto.ItemDTO;
import com.icbt.pahanaeduonlinebillingsystem.item.entity.ItemEntity;
import com.icbt.pahanaeduonlinebillingsystem.item.service.ItemService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Thisara Kavishka
 * @date 2025-08-09
 * @since 1.0
 */
public class ItemServiceImpl implements ItemService {

    private final ItemDAO itemDAO;
    private static final Logger LOGGER = LogUtil.getLogger(ItemServiceImpl.class);

    public ItemServiceImpl() {
        this.itemDAO = new ItemDAOImpl();
    }

    @Override
    public boolean add(ItemDTO dto) throws SQLException, ClassNotFoundException {
        Connection connection = null;
        try {
            connection = DBUtil.getConnection();
            connection.setAutoCommit(false);

            LOGGER.log(Level.INFO, "Attempting to add item: " + dto.getName());

            if (itemDAO.existsByName(connection, dto.getName())) {
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.ITEM_ALREADY_EXISTS);
            }

            ItemEntity itemEntity = ItemMapper.toEntity(dto);
            boolean isAdded = itemDAO.add(connection, itemEntity);

            if (isAdded) {
                connection.commit();
                LOGGER.log(Level.INFO, "Item added successfully: " + dto.getName());
                return true;
            } else {
                connection.rollback();
                LOGGER.log(Level.WARNING, "Failed to add item: " + dto.getName() + ", rolling back.");
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.ITEM_CREATION_FAILED);
            }
        } catch (SQLException e) {
            DBUtil.rollbackConnection(connection);
            LOGGER.log(Level.SEVERE, "Database error during item add: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        } finally {
            DBUtil.closeConnection(connection);
        }
    }

    @Override
    public boolean update(ItemDTO dto) throws SQLException, ClassNotFoundException {
        Connection connection = null;
        try {
            connection = DBUtil.getConnection();
            connection.setAutoCommit(false);

            LOGGER.log(Level.INFO, "Attempting to update item: " + dto.getName() + " (ID: " + dto.getId() + ")");

            ItemEntity existingItem = itemDAO.searchById(connection, dto.getId());
            if (existingItem == null) {
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.ITEM_NOT_FOUND);
            }

            if (!existingItem.getName().equals(dto.getName())) {
                if (itemDAO.existsByName(connection, dto.getName())) {
                    throw new PahanaEduOnlineBillingSystemException(ExceptionType.ITEM_ALREADY_EXISTS);
                }
            }

            ItemEntity itemEntity = ItemMapper.toEntity(dto);
            boolean isUpdated = itemDAO.update(connection, itemEntity);

            if (isUpdated) {
                connection.commit();
                LOGGER.log(Level.INFO, "Item updated successfully: " + dto.getName());
                return true;
            } else {
                connection.rollback();
                LOGGER.log(Level.WARNING, "Failed to update item: " + dto.getName() + ", rolling back.");
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.ITEM_UPDATE_FAILED);
            }
        } catch (SQLException e) {
            DBUtil.rollbackConnection(connection);
            LOGGER.log(Level.SEVERE, "Database error during item update: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        } finally {
            DBUtil.closeConnection(connection);
        }
    }

    @Override
    public boolean delete(Object... args) throws SQLException, ClassNotFoundException {
        if (args.length < 2 || !(args[0] instanceof Integer) || !(args[1] instanceof Integer)) {
            throw new IllegalArgumentException("Delete requires deleter ID (Integer) and item ID (Integer).");
        }
        Integer deletedByUserId = (Integer) args[0];
        Integer itemId = (Integer) args[1];

        Connection connection = null;
        try {
            connection = DBUtil.getConnection();
            connection.setAutoCommit(false);

            LOGGER.log(Level.INFO, "Attempting to soft delete item ID: " + itemId + " by user ID: " + deletedByUserId);

            if (!itemDAO.existsById(connection, itemId)) {
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.ITEM_NOT_FOUND);
            }

            boolean isDeleted = itemDAO.delete(connection, deletedByUserId, itemId);

            if (isDeleted) {
                connection.commit();
                LOGGER.log(Level.INFO, "Item soft deleted successfully: ID " + itemId);
                return true;
            } else {
                connection.rollback();
                LOGGER.log(Level.WARNING, "Failed to soft delete item: ID " + itemId + ", rolling back.");
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.ITEM_DELETION_FAILED);
            }
        } catch (SQLException e) {
            DBUtil.rollbackConnection(connection);
            LOGGER.log(Level.SEVERE, "Database error during item soft delete: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        } finally {
            DBUtil.closeConnection(connection);
        }
    }

    @Override
    public ItemDTO searchById(Object... args) throws SQLException, ClassNotFoundException {
        if (args.length < 1 || !(args[0] instanceof Integer)) {
            throw new IllegalArgumentException("Search by ID requires item ID (Integer).");
        }
        Integer id = (Integer) args[0];
        Connection connection = null;
        try {
            connection = DBUtil.getConnection();
            ItemEntity entity = itemDAO.searchById(connection, id);
            if (entity == null) {
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.ITEM_NOT_FOUND);
            }
            return ItemMapper.toDto(entity);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during item search by ID: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        } finally {
            DBUtil.closeConnection(connection);
        }
    }

    @Override
    public ItemDTO searchByName(String name) throws SQLException, ClassNotFoundException {
        Connection connection = null;
        try {
            connection = DBUtil.getConnection();
            ItemEntity entity = itemDAO.searchByName(connection, name);
            if (entity == null) {
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.ITEM_NOT_FOUND);
            }
            return ItemMapper.toDto(entity);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during item search by name: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        } finally {
            DBUtil.closeConnection(connection);
        }
    }

    @Override
    public List<ItemDTO> getAll(Map<String, String> searchParams) throws SQLException, ClassNotFoundException {
        Connection connection = null;
        try {
            connection = DBUtil.getConnection();
            List<ItemEntity> entities = itemDAO.getAll(connection, searchParams);
            return ItemMapper.toDTOList(entities);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during getAll items: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        } finally {
            DBUtil.closeConnection(connection);
        }
    }

    @Override
    public int getItemsCount() throws SQLException, ClassNotFoundException {
        Connection connection = null;
        try {
            connection = DBUtil.getConnection();
            return itemDAO.getItemsCount(connection);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during getItemsCount: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        } finally {
            DBUtil.closeConnection(connection);
        }
    }

    @Override
    public boolean restockItem(Integer itemId, int quantityToAdd, Integer updatedBy) throws SQLException, ClassNotFoundException {
        Connection connection = null;
        try {
            connection = DBUtil.getConnection();
            connection.setAutoCommit(false);

            LOGGER.log(Level.INFO, "Attempting to restock item ID: " + itemId + " by " + quantityToAdd + " units.");

            ItemEntity existingItem = itemDAO.searchById(connection, itemId);
            if (existingItem == null) {
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.ITEM_NOT_FOUND);
            }
            if (quantityToAdd <= 0) {
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.INVALID_ITEM_INPUTS);
            }

            int newStockQuantity = existingItem.getStockQuantity() + quantityToAdd;

            ItemDTO updateDto = new ItemDTO();
            updateDto.setId(itemId);
            updateDto.setName(existingItem.getName());
            updateDto.setUnitPrice(existingItem.getUnitPrice());
            updateDto.setStockQuantity(newStockQuantity);
            updateDto.setUpdatedBy(updatedBy);

            boolean isRestocked = itemDAO.update(connection, ItemMapper.toEntity(updateDto));

            if (isRestocked) {
                connection.commit();
                LOGGER.log(Level.INFO, "Item ID " + itemId + " restocked successfully. New stock: " + newStockQuantity);
                return true;
            } else {
                connection.rollback();
                LOGGER.log(Level.WARNING, "Failed to restock item ID " + itemId + ", rolling back.");
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.ITEM_UPDATE_FAILED);
            }
        } catch (SQLException e) {
            DBUtil.rollbackConnection(connection);
            LOGGER.log(Level.SEVERE, "Database error during item restock: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        } finally {
            DBUtil.closeConnection(connection);
        }
    }

    @Override
    public List<ItemDTO> getAllDeletedItems() throws ClassNotFoundException {
        try (Connection connection = DBUtil.getConnection()) {
            List<ItemEntity> itemEntities = itemDAO.getAllDeletedItems(connection);
            return ItemMapper.toDTOList(itemEntities);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during get all deleted items: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        }
    }

    @Override
    public boolean restoreItem(int id) throws ClassNotFoundException {
        try (Connection connection = DBUtil.getConnection()) {
            connection.setAutoCommit(false); // Use a transaction

            // 1. Find the record of the item that was deleted.
            ItemEntity deletedItemRecord = itemDAO.searchDeletedById(connection, id);
            if (deletedItemRecord == null) {
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.ITEM_NOT_FOUND);
            }

            // 2. Business logic: Check if another active item has taken its name.
            if (itemDAO.existsByName(connection, deletedItemRecord.getName())) {
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.ITEM_ALREADY_EXISTS);
            }

            // 3. If everything is clear, perform the restore.
            boolean isRestored = itemDAO.restoreItem(connection, id);
            if (isRestored) {
                connection.commit();
                LOGGER.info("Item with ID " + id + " restored successfully.");
            } else {
                connection.rollback();
            }
            return isRestored;

        } catch (SQLException e) {
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        }

    }
}