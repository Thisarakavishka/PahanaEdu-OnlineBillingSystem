package com.icbt.pahanaeduonlinebillingsystem.customer.service;

import com.icbt.pahanaeduonlinebillingsystem.customer.dto.CustomerDTO;
import com.icbt.pahanaeduonlinebillingsystem.common.base.CrudService;

import java.sql.SQLException;

/**
 * @author Thisara Kavishka
 * @date 2025-07-18
 * @since 1.0
 */
public interface CustomerService extends CrudService<CustomerDTO> {

    CustomerDTO searchByAccountNumber(Object... args) throws SQLException, ClassNotFoundException;

    CustomerDTO searchByPhone(String phone) throws SQLException, ClassNotFoundException;

    int getCustomersCount() throws SQLException, ClassNotFoundException;
}
