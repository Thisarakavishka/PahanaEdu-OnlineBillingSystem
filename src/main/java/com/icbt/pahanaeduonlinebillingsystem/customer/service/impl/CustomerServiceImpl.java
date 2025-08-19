package com.icbt.pahanaeduonlinebillingsystem.customer.service.impl;

import com.icbt.pahanaeduonlinebillingsystem.common.exception.ExceptionType;
import com.icbt.pahanaeduonlinebillingsystem.common.exception.PahanaEduOnlineBillingSystemException;
import com.icbt.pahanaeduonlinebillingsystem.common.util.LogUtil;
import com.icbt.pahanaeduonlinebillingsystem.customer.dao.impl.CustomerDAOImpl;
import com.icbt.pahanaeduonlinebillingsystem.customer.dao.CustomerDAO;
import com.icbt.pahanaeduonlinebillingsystem.customer.dto.CustomerDTO;
import com.icbt.pahanaeduonlinebillingsystem.customer.entity.CustomerEntity;
import com.icbt.pahanaeduonlinebillingsystem.customer.service.CustomerService;
import com.icbt.pahanaeduonlinebillingsystem.common.util.DBUtil;
import com.icbt.pahanaeduonlinebillingsystem.customer.mapper.CustomerMapper;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Thisara Kavishka
 * @date 2025-07-18
 * @since 1.0
 */
public class CustomerServiceImpl implements CustomerService {

    private final CustomerDAO customerDAO;
    private static final Logger LOGGER = LogUtil.getLogger(CustomerServiceImpl.class);

    public CustomerServiceImpl() {
        customerDAO = new CustomerDAOImpl();
    }

    @Override
    public boolean add(CustomerDTO dto) throws SQLException, ClassNotFoundException {
        Connection connection = null;
        try {
            connection = DBUtil.getConnection();
            connection.setAutoCommit(false);

            LOGGER.log(Level.INFO, "Attempting to add customer with Account Number: " + dto.getAccountNumber());

            if (customerDAO.existsByAccountNumber(connection, dto.getAccountNumber())) {
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.CUSTOMER_ACCOUNT_NUMBER_ALREADY_EXISTS);
            }
            if (customerDAO.existsByPhoneNumber(connection, dto.getPhone())) {
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.CUSTOMER_PHONE_NUMBER_ALREADY_EXISTS);
            }

            CustomerEntity customerEntity = CustomerMapper.toEntity(dto);
            boolean isCustomerAddedSuccess = customerDAO.add(connection, customerEntity);

            if (isCustomerAddedSuccess) {
                connection.commit();
                LOGGER.log(Level.INFO, "Customer added successfully: " + dto.getName());
            } else {
                connection.rollback();
                LOGGER.log(Level.WARNING, "Failed to add customer: " + dto.getName() + ", rolling back.");
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.CUSTOMER_CREATION_FAILED);
            }
            return isCustomerAddedSuccess;
        } catch (SQLException e) {
            DBUtil.rollbackConnection(connection);
            LOGGER.log(Level.SEVERE, "Database error during customer add: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        } finally {
            DBUtil.closeConnection(connection);
        }
    }

    @Override
    public boolean update(CustomerDTO dto) throws SQLException, ClassNotFoundException {
        Connection connection = null;
        try {
            connection = DBUtil.getConnection();
            connection.setAutoCommit(false);

            LOGGER.log(Level.INFO, "Attempting to update customer with Account Number: " + dto.getAccountNumber());

            CustomerEntity existingCustomer = customerDAO.searchByAccountNumber(connection, dto.getAccountNumber());
            if (existingCustomer == null) {
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.CUSTOMER_NOT_FOUND);
            }
            ;

            // If phone number is changed, ensure it's unique (excluding the current customer)
            if (!existingCustomer.getPhone().equals(dto.getPhone())) {
                if (customerDAO.existsByPhoneNumber(connection, dto.getPhone())) {
                    throw new PahanaEduOnlineBillingSystemException(ExceptionType.CUSTOMER_PHONE_NUMBER_ALREADY_EXISTS);
                }
            }

            // Ensure the correct record is updated
            dto.setId(existingCustomer.getId());

            CustomerEntity customerEntity = CustomerMapper.toEntity(dto);
            boolean isCustomerUpdatedSuccess = customerDAO.update(connection, customerEntity);
            if (isCustomerUpdatedSuccess) {
                connection.commit();
                LOGGER.log(Level.INFO, "Customer updated successfully: " + dto.getName());
            } else {
                connection.rollback();
                LOGGER.log(Level.WARNING, "Failed to update customer: " + dto.getName() + ", rolling back.");
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.CUSTOMER_UPDATE_FAILED);
            }
            return isCustomerUpdatedSuccess;
        } catch (SQLException e) {
            DBUtil.rollbackConnection(connection);
            LOGGER.log(Level.SEVERE, "Database error during customer update: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        } finally {
            DBUtil.closeConnection(connection);
        }
    }

    @Override
    public boolean delete(Object... args) throws SQLException, ClassNotFoundException {
        if (args.length < 2 || !(args[0] instanceof Integer) || !(args[1] instanceof String)) {
            throw new IllegalArgumentException("Delete requires deleter ID (Integer) and account number (String).");
        }

        Integer deleteByUserId = (Integer) args[0];
        String accountNumber = (String) args[1];

        Connection connection = null;
        try {
            connection = DBUtil.getConnection();
            connection.setAutoCommit(false);

            LOGGER.log(Level.INFO, "Attempting to soft delete customer with Account Number: " + accountNumber);

            if (!customerDAO.existsByAccountNumber(connection, accountNumber)) {
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.CUSTOMER_NOT_FOUND);
            }

            boolean isCustomerDeletedSuccess = customerDAO.delete(connection, deleteByUserId, accountNumber);
            if (isCustomerDeletedSuccess) {
                connection.commit();
                LOGGER.log(Level.INFO, "Customer soft deleted successfully: " + accountNumber);
            } else {
                connection.rollback();
                LOGGER.log(Level.WARNING, "Failed to soft delete customer: " + accountNumber + ", rolling back.");
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.CUSTOMER_DELETION_FAILED);
            }
            return isCustomerDeletedSuccess;
        } catch (SQLException e) {
            DBUtil.rollbackConnection(connection);
            LOGGER.log(Level.SEVERE, "Database error during customer soft delete: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        } finally {
            DBUtil.closeConnection(connection);
        }
    }

    @Override
    public CustomerDTO searchById(Object... args) throws SQLException, ClassNotFoundException {
        if (args.length < 1 || !(args[0] instanceof Integer)) {
            throw new IllegalArgumentException("Search by ID requires customer ID (Integer).");
        }

        Integer id = (Integer) args[0];
        Connection connection = null;

        try {
            connection = DBUtil.getConnection();
            CustomerEntity customerEntity = customerDAO.searchById(connection, id);
            if (customerEntity == null) {
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.CUSTOMER_NOT_FOUND);
            }
            return CustomerMapper.toDto(customerEntity);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during customer search by ID: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        } finally {
            DBUtil.closeConnection(connection);
        }

    }

    @Override
    public List<CustomerDTO> getAll(Map<String, String> searchParams) throws SQLException, ClassNotFoundException {
        Connection connection = null;
        try {
            connection = DBUtil.getConnection();
            List<CustomerEntity> customerEntityList = customerDAO.getAll(connection, searchParams);
            return CustomerMapper.toDTOList(customerEntityList);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during getAll customers: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        } finally {
            DBUtil.closeConnection(connection);
        }
    }

    @Override
    public CustomerDTO searchByAccountNumber(Object... args) throws SQLException, ClassNotFoundException {
        if (args.length < 1 || !(args[0] instanceof String)) {
            throw new IllegalArgumentException("Search by AccountNumber requires customer AccountNumber (String).");
        }

        String accountNumber = (String) args[0];
        Connection connection = null;

        try {
            connection = DBUtil.getConnection();
            CustomerEntity customerEntity = customerDAO.searchByAccountNumber(connection, accountNumber);

            if (customerEntity == null) {
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.CUSTOMER_NOT_FOUND);
            }
            return CustomerMapper.toDto(customerEntity);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during customer search by AccountNumber: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        } finally {
            DBUtil.closeConnection(connection);
        }
    }

    @Override
    public CustomerDTO searchByPhone(String phone) throws SQLException, ClassNotFoundException {
        Connection connection = null;
        try {
            connection = DBUtil.getConnection();
            CustomerEntity customerEntity = customerDAO.searchByPhone(connection, phone);

            if (customerEntity == null) {
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.CUSTOMER_NOT_FOUND);
            }
            return CustomerMapper.toDto(customerEntity);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during customer search by phone: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        } finally {
            DBUtil.closeConnection(connection);
        }
    }

    @Override
    public int getCustomersCount() throws SQLException, ClassNotFoundException {
        Connection connection = null;
        try {
            connection = DBUtil.getConnection();
            return customerDAO.getCustomersCount(connection);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during getCustomersCount: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        } finally {
            DBUtil.closeConnection(connection);
        }
    }
}
