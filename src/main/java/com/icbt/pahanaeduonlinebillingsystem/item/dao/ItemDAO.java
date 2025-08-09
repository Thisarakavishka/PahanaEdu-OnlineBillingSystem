package com.icbt.pahanaeduonlinebillingsystem.item.dao;

import com.icbt.pahanaeduonlinebillingsystem.common.base.CrudDAO;
import com.icbt.pahanaeduonlinebillingsystem.item.entity.ItemEntity;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Thisara Kavishka
 * @date 2025-08-09
 * @since 1.0
 */
public interface ItemDAO extends CrudDAO<ItemEntity> {

    boolean existsByName(Connection connection, String name) throws SQLException, ClassNotFoundException;

    boolean existsById(Connection connection, Integer id) throws SQLException, ClassNotFoundException;

    ItemEntity searchByName(Connection connection, String name) throws SQLException, ClassNotFoundException;

    int getItemsCount(Connection connection) throws SQLException, ClassNotFoundException;
}
