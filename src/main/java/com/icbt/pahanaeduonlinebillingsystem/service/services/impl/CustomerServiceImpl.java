package com.icbt.pahanaeduonlinebillingsystem.service.services.impl;

import com.icbt.pahanaeduonlinebillingsystem.dto.CustomerDTO;
import com.icbt.pahanaeduonlinebillingsystem.service.services.CustomerService;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author Thisara Kavishka
 * @date 2025-07-18
 * @since 1.0
 */
public class CustomerServiceImpl implements CustomerService {

    @Override
    public boolean add(CustomerDTO dto) throws SQLException, ClassNotFoundException {
        return false;
    }

    @Override
    public boolean update(CustomerDTO dto) throws SQLException, ClassNotFoundException {
        return false;
    }

    @Override
    public boolean delete(Object... args) throws SQLException, ClassNotFoundException {
        return false;
    }

    @Override
    public CustomerDTO searchById(Object... args) throws SQLException, ClassNotFoundException {
        return null;
    }

    @Override
    public List<CustomerDTO> getAll(Map<String, String> searchParams) throws SQLException, ClassNotFoundException {
        return List.of();
    }
}
