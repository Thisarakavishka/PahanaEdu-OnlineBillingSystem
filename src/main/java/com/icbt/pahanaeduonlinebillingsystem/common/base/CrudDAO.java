package com.icbt.pahanaeduonlinebillingsystem.common.base;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author Thisara Kavishka
 * @date 2025-07-15
 * @since 1.0
 */
public interface CrudDAO<T extends SuperEntity> extends SuperDAO {

    boolean add(Connection connection, T entity) throws SQLException, ClassNotFoundException;

    boolean update(Connection connection, T entity) throws SQLException, ClassNotFoundException;

    boolean delete(Connection connection, Object... args) throws SQLException, ClassNotFoundException;

    T searchById(Connection connection, Object... args) throws SQLException, ClassNotFoundException;

    List<T> getAll(Connection connection, Map<String, String> searchParams) throws SQLException, ClassNotFoundException;
}
