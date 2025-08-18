package com.icbt.pahanaeduonlinebillingsystem.item.dao.impl;

import com.icbt.pahanaeduonlinebillingsystem.common.exception.ExceptionType;
import com.icbt.pahanaeduonlinebillingsystem.common.exception.PahanaEduOnlineBillingSystemException;
import com.icbt.pahanaeduonlinebillingsystem.common.util.DAOUtil;
import com.icbt.pahanaeduonlinebillingsystem.common.util.DBUtil;
import com.icbt.pahanaeduonlinebillingsystem.common.util.LogUtil;
import com.icbt.pahanaeduonlinebillingsystem.item.dao.ItemDAO;
import com.icbt.pahanaeduonlinebillingsystem.item.entity.ItemEntity;
import com.icbt.pahanaeduonlinebillingsystem.item.mapper.ItemMapper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Thisara Kavishka
 * @date 2025-08-09
 * @since 1.0
 */
public class ItemDAOImpl implements ItemDAO {

    private static final Logger LOGGER = LogUtil.getLogger(ItemDAOImpl.class);

    @Override
    public boolean existsByName(Connection connection, String name) throws SQLException, ClassNotFoundException {
        String sql = "SELECT 1 FROM items WHERE name = ? AND deleted_at IS NULL";
        ResultSet resultSet = null;
        try {
            resultSet = DAOUtil.executeQuery(connection, sql, name);
            return resultSet.next();
        } finally {
            DBUtil.closeResultSet(resultSet);
        }
    }

    @Override
    public boolean existsById(Connection connection, Integer id) throws SQLException, ClassNotFoundException {
        String sql = "SELECT 1 FROM items WHERE id = ? AND deleted_at IS NULL";
        ResultSet resultSet = null;
        try {
            resultSet = DAOUtil.executeQuery(connection, sql, id);
            return resultSet.next();
        } finally {
            DBUtil.closeResultSet(resultSet);
        }
    }

    @Override
    public ItemEntity searchByName(Connection connection, String name) throws SQLException, ClassNotFoundException {
        String sql = "SELECT * FROM items WHERE name = ? AND deleted_at IS NULL";
        ResultSet resultSet = null;
        try {
            resultSet = DAOUtil.executeQuery(connection, sql, name);
            if (resultSet.next()) {
                return ItemMapper.mapResultSetToItemEntity(resultSet);
            }
            return null;
        } finally {
            DBUtil.closeResultSet(resultSet);
        }

    }

    @Override
    public int getItemsCount(Connection connection) throws SQLException, ClassNotFoundException {
        String sql = "SELECT COUNT(id) FROM items WHERE deleted_at IS NULL";
        ResultSet resultSet = null;
        try {
            resultSet = DAOUtil.executeQuery(connection, sql);
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;
        } finally {
            DBUtil.closeResultSet(resultSet);
        }
    }

    @Override
    public boolean add(Connection connection, ItemEntity entity) throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO items (name, unit_price, stock_quantity, created_by, created_at) VALUES (?, ?, ?, ?, ?)";
        try {
            return DAOUtil.executeUpdate(connection, sql,
                    entity.getName(),
                    entity.getUnitPrice(),
                    entity.getStockQuantity(),
                    entity.getCreatedBy(),
                    new Timestamp(System.currentTimeMillis())
            );
        } catch (PahanaEduOnlineBillingSystemException e) {
            LOGGER.log(Level.SEVERE, "Failed to add item in DAO: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        }
    }

    @Override
    public boolean update(Connection connection, ItemEntity entity) throws SQLException, ClassNotFoundException {
        String sql = "UPDATE items SET name = ?, unit_price = ?, stock_quantity = ?, updated_by = ?, updated_at = ? WHERE id = ? AND deleted_at IS NULL";
        try {
            return DAOUtil.executeUpdate(connection, sql,
                    entity.getName(),
                    entity.getUnitPrice(),
                    entity.getStockQuantity(),
                    entity.getUpdatedBy(),
                    new Timestamp(System.currentTimeMillis()),
                    entity.getId()
            );
        } catch (PahanaEduOnlineBillingSystemException e) {
            LOGGER.log(Level.SEVERE, "Failed to update item in DAO: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        }
    }

    @Override
    public boolean delete(Connection connection, Object... args) throws SQLException, ClassNotFoundException {
        if (args.length < 2 || !(args[0] instanceof Integer) || !(args[1] instanceof Integer)) {
            throw new IllegalArgumentException("Delete requires deleter ID (Integer) and item ID (Integer).");
        }
        Integer deletedBy = (Integer) args[0];
        Integer itemId = (Integer) args[1];

        String sql = "UPDATE items SET deleted_by = ?, deleted_at = ? WHERE id = ? AND deleted_at IS NULL";
        try {
            return DAOUtil.executeUpdate(connection, sql,
                    deletedBy,
                    new Timestamp(System.currentTimeMillis()),
                    itemId
            );
        } catch (PahanaEduOnlineBillingSystemException e) {
            LOGGER.log(Level.SEVERE, "Failed to soft delete item in DAO: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        }
    }

    @Override
    public ItemEntity searchById(Connection connection, Object... args) throws SQLException, ClassNotFoundException {
        String sql = "SELECT * FROM items WHERE id = ? AND deleted_at IS NULL";
        Integer itemId = (Integer) args[0];
        ResultSet resultSet = null;
        try {
            resultSet = DAOUtil.executeQuery(connection, sql, itemId);
            if (resultSet.next()) {
                return ItemMapper.mapResultSetToItemEntity(resultSet);
            }
            return null;
        } finally {
            DBUtil.closeResultSet(resultSet);
        }
    }

    @Override
    public List<ItemEntity> getAll(Connection connection, Map<String, String> searchParams) throws SQLException, ClassNotFoundException {
        List<ItemEntity> items = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM items WHERE deleted_at IS NULL");
        List<Object> params = new ArrayList<>();

        if (searchParams != null) {
            if (searchParams.containsKey("search")) {
                String searchTerm = "%" + searchParams.get("search") + "%";
                sqlBuilder.append(" AND (name LIKE ?)"); // Search only by name for items
                params.add(searchTerm);
            } else {
                if (searchParams.containsKey("name")) {
                    sqlBuilder.append(" AND name LIKE ?");
                    params.add("%" + searchParams.get("name") + "%");
                }
                // Add other specific search parameters if needed in the future
            }
        }
        sqlBuilder.append(" ORDER BY name ASC");

        ResultSet resultSet = null;
        try {
            resultSet = DAOUtil.executeQuery(connection, sqlBuilder.toString(), params.toArray());
            while (resultSet.next()) {
                items.add(ItemMapper.mapResultSetToItemEntity(resultSet));
            }
        } finally {
            DBUtil.closeResultSet(resultSet);
        }
        return items;
    }


}
