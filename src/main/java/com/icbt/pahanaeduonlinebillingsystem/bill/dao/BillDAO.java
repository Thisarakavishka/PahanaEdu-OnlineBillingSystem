package com.icbt.pahanaeduonlinebillingsystem.bill.dao;

import com.icbt.pahanaeduonlinebillingsystem.bill.entity.BillEntity;
import com.icbt.pahanaeduonlinebillingsystem.common.base.CrudDAO;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Thisara Kavishka
 * @date 2025-08-11
 * @since 1.0
 */
public interface BillDAO extends CrudDAO<BillEntity> {

    int addBill(Connection connection, BillEntity entity) throws SQLException, ClassNotFoundException;

}