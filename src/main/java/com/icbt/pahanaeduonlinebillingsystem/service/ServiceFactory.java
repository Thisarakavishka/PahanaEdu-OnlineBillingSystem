package com.icbt.pahanaeduonlinebillingsystem.service;

import com.icbt.pahanaeduonlinebillingsystem.service.services.impl.UserServiceImpl;

/**
 * @author Thisara Kavishka
 * @date 2025-07-17
 * @since 1.0
 */
public class ServiceFactory {

    private static ServiceFactory instance;

    private ServiceFactory() {
    }

    public static ServiceFactory getInstance() {
        return instance == null ? new ServiceFactory() : instance;
    }

    public <T extends SuperService> T getService(ServiceType type) {
        switch (type) {
            case USER_SERVICE:
                return (T) new UserServiceImpl();
            default:
                throw new IllegalArgumentException("Invalid service type: " + type);
        }
    }
}
