package com.icbt.pahanaeduonlinebillingsystem.service;

import com.icbt.pahanaeduonlinebillingsystem.dto.SuperDTO;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author Thisara Kavishka
 * @date 2025-07-17
 * @since 1.0
 */
public interface CrudService<T extends SuperDTO> extends SuperService {

    boolean add(T dto) throws SQLException, ClassNotFoundException;

    boolean update(T dto) throws SQLException, ClassNotFoundException;

    boolean delete(Object... args) throws SQLException, ClassNotFoundException;

    T searchById(Object... args) throws SQLException, ClassNotFoundException;

    List<T> getAll(Map<String, String> searchParams) throws SQLException, ClassNotFoundException;
}
