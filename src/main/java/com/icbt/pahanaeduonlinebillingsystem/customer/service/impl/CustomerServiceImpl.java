package com.icbt.pahanaeduonlinebillingsystem.customer.service.impl;

import com.icbt.pahanaeduonlinebillingsystem.common.exception.ExceptionType;
import com.icbt.pahanaeduonlinebillingsystem.common.exception.PahanaEduOnlineBillingSystemException;
import com.icbt.pahanaeduonlinebillingsystem.common.util.LogUtil;
import com.icbt.pahanaeduonlinebillingsystem.customer.dao.impl.CustomerDAOImpl;
import com.icbt.pahanaeduonlinebillingsystem.customer.dao.CustomerDAO;
import com.icbt.pahanaeduonlinebillingsystem.customer.dto.CustomerDTO;
import com.icbt.pahanaeduonlinebillingsystem.customer.service.CustomerService;
import com.icbt.pahanaeduonlinebillingsystem.common.util.DBUtil;
import com.icbt.pahanaeduonlinebillingsystem.customer.converter.CustomerConverter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Thisara Kavishka
 * @date 2025-07-18
 * @since 1.0
 */
public class CustomerServiceImpl implements CustomerService {

    private final Connection connection = DBUtil.getConnection();
    private final CustomerDAO customerDAO;
    private static final Logger LOGGER = LogUtil.getLogger(CustomerServiceImpl.class);

    public CustomerServiceImpl() {
        customerDAO = new CustomerDAOImpl();
    }

    @Override
    public boolean add(CustomerDTO dto) throws SQLException, ClassNotFoundException {

        LOGGER.info("Adding customer (Account Number) : " + dto.getAccountNumber());

        if (customerDAO.existsById(connection, dto.getId())) {
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.CUSTOMER_ALREADY_EXISTS);
        }

        if (customerDAO.existsByAccountNumber(connection, dto.getAccountNumber())) {
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.CUSTOMER_ALREADY_EXISTS);
        }

        if (customerDAO.existsByPhoneNumber(connection, dto.getPhone())) {
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.CUSTOMER_ALREADY_EXISTS);
        }

        boolean isSuccess = customerDAO.add(connection, CustomerConverter.toEntity(dto));
        if (!isSuccess) {
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.INTERNAL_SERVER_ERROR);
        }

        return true;
    }

    @Override
    public boolean update(CustomerDTO dto) throws SQLException, ClassNotFoundException {
        LOGGER.info("Updating customer (Account Number) : " + dto.getAccountNumber());

        if (!customerDAO.existsById(connection, dto.getId())) {
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.CUSTOMER_NOT_FOUND);
        }

        if (!customerDAO.existsByAccountNumber(connection, dto.getAccountNumber())) {
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.CUSTOMER_NOT_FOUND);
        }

        boolean updateSuccess = customerDAO.update(connection, CustomerConverter.toEntity(dto));
        if (!updateSuccess) {
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.INTERNAL_SERVER_ERROR);
        }

        return true;
    }

    @Override
    public boolean delete(Object... args) throws SQLException, ClassNotFoundException {
        LOGGER.info("Deleting customer (soft): " + args[1]);

        if (!customerDAO.existsById(connection, args[1])) {
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.CUSTOMER_NOT_FOUND);
        }

        if (!customerDAO.existsByAccountNumber(connection, args[1])) {
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.CUSTOMER_NOT_FOUND);
        }

        return customerDAO.delete(connection, args);
    }

    @Override
    public CustomerDTO searchById(Object... args) throws SQLException, ClassNotFoundException {
        LOGGER.info("Searching customer by id: " + args[0]);

        CustomerDTO dto = CustomerConverter.toDto(customerDAO.searchById(connection, args[0]));
        if (dto == null) {
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.CUSTOMER_NOT_FOUND);
        }

        return dto;
    }

    @Override
    public List<CustomerDTO> getAll(Map<String, String> searchParams) throws SQLException, ClassNotFoundException {
        LOGGER.info("Fetching all customers...");

        return CustomerConverter.toDTOList(customerDAO.getAll(connection, searchParams));
    }
}
