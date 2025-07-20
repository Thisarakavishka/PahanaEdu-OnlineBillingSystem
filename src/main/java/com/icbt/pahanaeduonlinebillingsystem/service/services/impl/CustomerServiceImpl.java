package com.icbt.pahanaeduonlinebillingsystem.service.services.impl;

import com.icbt.pahanaeduonlinebillingsystem.dao.DAOFactory;
import com.icbt.pahanaeduonlinebillingsystem.dao.DAOTypes;
import com.icbt.pahanaeduonlinebillingsystem.dao.daos.CustomerDAO;
import com.icbt.pahanaeduonlinebillingsystem.dto.CustomerDTO;
import com.icbt.pahanaeduonlinebillingsystem.service.services.CustomerService;
import com.icbt.pahanaeduonlinebillingsystem.util.DBUtil;
import com.icbt.pahanaeduonlinebillingsystem.util.converter.CustomerConverter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author Thisara Kavishka
 * @date 2025-07-18
 * @since 1.0
 */
public class CustomerServiceImpl implements CustomerService {

    private final Connection connection = DBUtil.getConnection();
    private final CustomerDAO customerDAO = DAOFactory.getInstance().getDAO(DAOTypes.CUSTOMER_DAO);


    @Override
    public boolean add(CustomerDTO dto) throws SQLException, ClassNotFoundException {
        return customerDAO.add(connection, CustomerConverter.toEntity(dto));
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
