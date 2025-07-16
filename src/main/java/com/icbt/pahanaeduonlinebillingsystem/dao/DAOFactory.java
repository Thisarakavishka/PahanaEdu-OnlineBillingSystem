package com.icbt.pahanaeduonlinebillingsystem.dao;

import com.icbt.pahanaeduonlinebillingsystem.dao.daos.impl.CustomerDAOImpl;
import com.icbt.pahanaeduonlinebillingsystem.dao.daos.impl.UserDAOImpl;

/**
 * @author Thisara Kavishka
 * @date 2025-07-17
 * @since 1.0
 */
public class DAOFactory {

    private static DAOFactory instance;

    private DAOFactory() {
    }

    public static DAOFactory getInstance() {
        return instance == null ? instance = new DAOFactory() : instance;
    }

    public <T extends SuperDAO> T getDAO(DAOTypes type) {
        switch (type) {
            case USER_DAO:
                return (T) new UserDAOImpl();
            case CUSTOMER_DAO:
                return (T) new CustomerDAOImpl();
            default:
                throw new IllegalArgumentException("Invalid DAO type: " + type);
        }
    }
}
