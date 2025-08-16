package com.icbt.pahanaeduonlinebillingsystem.bill.dao;

import com.icbt.pahanaeduonlinebillingsystem.bill.entity.BillDetailEntity;
import com.icbt.pahanaeduonlinebillingsystem.common.base.CrudDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Thisara Kavishka
 * @date 2025-08-11
 * @since 1.0
 */
public interface BillDetailDAO extends CrudDAO<BillDetailEntity> {

    void addBillDetails(Connection connection, List<BillDetailEntity> details) throws SQLException;

    List<BillDetailEntity> getBillDetails(Connection connection, Integer billId) throws SQLException;

}
