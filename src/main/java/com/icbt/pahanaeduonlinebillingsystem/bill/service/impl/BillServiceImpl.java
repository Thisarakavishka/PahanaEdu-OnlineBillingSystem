package com.icbt.pahanaeduonlinebillingsystem.bill.service.impl;

import com.icbt.pahanaeduonlinebillingsystem.bill.dao.BillDAO;
import com.icbt.pahanaeduonlinebillingsystem.bill.dao.BillDetailDAO;
import com.icbt.pahanaeduonlinebillingsystem.bill.dao.impl.BillDAOImpl;
import com.icbt.pahanaeduonlinebillingsystem.bill.dao.impl.BillDetailDAOImpl;
import com.icbt.pahanaeduonlinebillingsystem.bill.dto.BillDTO;
import com.icbt.pahanaeduonlinebillingsystem.bill.dto.BillDetailDTO;
import com.icbt.pahanaeduonlinebillingsystem.bill.entity.BillDetailEntity;
import com.icbt.pahanaeduonlinebillingsystem.bill.entity.BillEntity;
import com.icbt.pahanaeduonlinebillingsystem.bill.mapper.BillMapper;
import com.icbt.pahanaeduonlinebillingsystem.bill.service.BillService;
import com.icbt.pahanaeduonlinebillingsystem.common.exception.ExceptionType;
import com.icbt.pahanaeduonlinebillingsystem.common.exception.PahanaEduOnlineBillingSystemException;
import com.icbt.pahanaeduonlinebillingsystem.common.util.DBUtil;
import com.icbt.pahanaeduonlinebillingsystem.common.util.LogUtil;
import com.icbt.pahanaeduonlinebillingsystem.customer.dao.CustomerDAO;
import com.icbt.pahanaeduonlinebillingsystem.customer.dao.impl.CustomerDAOImpl;
import com.icbt.pahanaeduonlinebillingsystem.customer.dto.CustomerDTO;
import com.icbt.pahanaeduonlinebillingsystem.customer.entity.CustomerEntity;
import com.icbt.pahanaeduonlinebillingsystem.customer.mapper.CustomerMapper;
import com.icbt.pahanaeduonlinebillingsystem.item.dao.ItemDAO;
import com.icbt.pahanaeduonlinebillingsystem.item.dao.impl.ItemDAOImpl;
import com.icbt.pahanaeduonlinebillingsystem.item.entity.ItemEntity;
import com.icbt.pahanaeduonlinebillingsystem.user.dto.UserDTO;
import com.icbt.pahanaeduonlinebillingsystem.user.service.UserService;
import com.icbt.pahanaeduonlinebillingsystem.user.service.impl.UserServiceImpl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Thisara Kavishka
 * @date 2025-08-11
 * @since 1.0
 */
public class BillServiceImpl implements BillService {

    private final BillDAO billDAO;
    private final BillDetailDAO billDetailDAO;
    private final ItemDAO itemDAO;
    private final CustomerDAO customerDAO;
    private final UserService userService;
    private static final Logger LOGGER = LogUtil.getLogger(BillServiceImpl.class);

    public BillServiceImpl() {
        this.billDAO = new BillDAOImpl();
        this.billDetailDAO = new BillDetailDAOImpl();
        this.itemDAO = new ItemDAOImpl();
        this.customerDAO = new CustomerDAOImpl();
        this.userService = new UserServiceImpl();
    }

    @Override
    public BillDTO generateBill(BillDTO billDTO, Integer generatedByUserId) throws ClassNotFoundException {
        Connection connection = null;
        try {
            connection = DBUtil.getConnection();
            connection.setAutoCommit(false);

            LOGGER.log(Level.INFO, "Attempting to generate bill for customer ID: " + billDTO.getCustomerId());

            CustomerEntity customerEntity = customerDAO.searchById(connection, billDTO.getCustomerId());
            if (customerEntity == null) {
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.CUSTOMER_NOT_FOUND);
            }

            if (billDTO.getDetails() == null || billDTO.getDetails().isEmpty()) {
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.INVALID_BILL_INPUTS);
            }

            int totalUnitsInBill = 0;

            for (BillDetailDTO detail : billDTO.getDetails()) {
                ItemEntity item = itemDAO.searchById(connection, detail.getItemId());
                if (item == null || item.getDeletedAt() != null) {
                    throw new PahanaEduOnlineBillingSystemException(ExceptionType.ITEM_NOT_FOUND);
                }

                if (item.getStockQuantity() < detail.getUnits()) {
                    throw new PahanaEduOnlineBillingSystemException(ExceptionType.INSUFFICIENT_STOCK);
                }

                detail.setItemNameAtSale(item.getName());
                detail.setUnitPriceAtSale(item.getUnitPrice());
                detail.setTotal(item.getUnitPrice().multiply(BigDecimal.valueOf(detail.getUnits())));
                detail.setCreatedBy(generatedByUserId);

                item.setStockQuantity(item.getStockQuantity() - detail.getUnits());
                item.setUpdatedBy(generatedByUserId);
                if (!itemDAO.update(connection, item)) {
                    throw new PahanaEduOnlineBillingSystemException(ExceptionType.INTERNAL_SERVER_ERROR);
                }

                totalUnitsInBill += detail.getUnits();
            }

            LOGGER.log(Level.INFO, "Updating customer units. Old value: " + customerEntity.getUnitsConsumed() + ", Adding: " + totalUnitsInBill);
            customerEntity.setUnitsConsumed(customerEntity.getUnitsConsumed() + totalUnitsInBill);
            customerEntity.setUpdatedBy(generatedByUserId);
            if (!customerDAO.update(connection, customerEntity)) {
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.CUSTOMER_UPDATE_FAILED);
            }

            BigDecimal totalAmount = billDTO.getDetails().stream()
                    .map(BillDetailDTO::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            billDTO.setTotalAmount(totalAmount);
            billDTO.setCreatedBy(generatedByUserId);

            BillEntity billEntity = BillMapper.toEntity(billDTO);
            int addedBillId = billDAO.addBill(connection, billEntity);
            if (addedBillId == -1) {
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.INTERNAL_SERVER_ERROR);
            }
            billDTO.setId(addedBillId);

            List<BillDetailEntity> billDetailEntities = new ArrayList<>();
            for (BillDetailDTO detail : billDTO.getDetails()) {
                BillDetailEntity detailEntity = BillMapper.toEntity(detail);
                detailEntity.setBillId(addedBillId);
                billDetailEntities.add(detailEntity);
            }
            billDetailDAO.addBillDetails(connection, billDetailEntities);

            connection.commit();
            LOGGER.log(Level.INFO, "Bill ID " + addedBillId + " generated successfully.");
            return getBillById(addedBillId);
        } catch (SQLException e) {
            DBUtil.rollbackConnection(connection);
            LOGGER.log(Level.SEVERE, "Database error during bill generation: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        } catch (PahanaEduOnlineBillingSystemException e) {
            DBUtil.rollbackConnection(connection);
            LOGGER.log(Level.WARNING, "Business error during bill generation: " + e.getExceptionType().name() + " - " + e.getMessage());
            throw e;
        } finally {
            DBUtil.closeConnection(connection);
        }
    }

    @Override
    public boolean delete(Integer deletedByUserId, Integer billId) throws ClassNotFoundException {
        Connection connection = null;
        try {
            connection = DBUtil.getConnection();
            connection.setAutoCommit(false);

            LOGGER.log(Level.INFO, "Attempting to soft delete bill ID: " + billId + " by user ID: " + deletedByUserId);

            BillEntity billToDelete = billDAO.searchById(connection, billId);
            if (billToDelete == null) {
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.BILL_NOT_FOUND);
            }

            boolean isDeletedSuccess = billDAO.delete(connection, deletedByUserId, billId);

            if (isDeletedSuccess) {
                connection.commit();
                LOGGER.log(Level.INFO, "Bill ID " + billId + " soft deleted successfully.");
                return true;
            } else {
                connection.rollback();
                LOGGER.log(Level.WARNING, "Failed to soft delete bill ID " + billId + ", rolling back.");
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.BILL_DELETION_FAILED);
            }
        } catch (SQLException e) {
            DBUtil.rollbackConnection(connection);
            LOGGER.log(Level.SEVERE, "Database error during bill soft delete: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        } catch (PahanaEduOnlineBillingSystemException e) {
            DBUtil.rollbackConnection(connection);
            LOGGER.log(Level.WARNING, "Business error during bill soft delete: " + e.getExceptionType().name() + " - " + e.getMessage());
            throw e;
        } finally {
            DBUtil.closeConnection(connection);
        }
    }

    @Override
    public BillDTO getBillById(Integer billId) throws ClassNotFoundException {
        Connection connection = null;
        try {
            connection = DBUtil.getConnection();

            BillEntity billEntity = billDAO.searchById(connection, billId);
            if (billEntity == null) {
                throw new PahanaEduOnlineBillingSystemException(ExceptionType.BILL_NOT_FOUND);
            }

            BillDTO billDTO = BillMapper.toDto(billEntity);

            List<BillDetailEntity> detailEntities = billDetailDAO.getBillDetails(connection, billId);
            billDTO.setDetails(BillMapper.toDTOListFromDetails(detailEntities));

            try {
                CustomerDTO customer = CustomerMapper.toDto(customerDAO.searchById(connection, billEntity.getCustomerId()));
                if (customer != null) {
                    billDTO.setCustomerAccountNumber(customer.getAccountNumber());
                    billDTO.setCustomerName(customer.getName());
                } else {
                    billDTO.setCustomerAccountNumber("ID: " + billEntity.getCustomerId());
                    billDTO.setCustomerName("Unknown Customer");
                }
            } catch (PahanaEduOnlineBillingSystemException e) {
                LOGGER.log(Level.WARNING, "Customer not found for bill ID " + billId + ". Displaying ID instead.");
                billDTO.setCustomerAccountNumber("ID: " + billEntity.getCustomerId());
                billDTO.setCustomerName("Unknown Customer");
            }

            try {
                UserDTO createdByUser = userService.searchById(billEntity.getCreatedBy());
                if (createdByUser != null) {
                    billDTO.setCreateByUsername(createdByUser.getUsername());
                } else {
                    billDTO.setCreateByUsername("ID: " + billEntity.getCreatedBy());
                }
            } catch (PahanaEduOnlineBillingSystemException e) {
                LOGGER.log(Level.WARNING, "Generated By user not found for bill ID " + billId + ". Displaying ID instead.");
                billDTO.setCreateByUsername("ID: " + billEntity.getCreatedBy());
            }

            return billDTO;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during getBillById: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        } finally {
            DBUtil.closeConnection(connection);
        }
    }

    @Override
    public List<BillDTO> getAll(Map<String, String> searchParams) throws ClassNotFoundException {
        Connection connection = null;
        try {
            connection = DBUtil.getConnection();
            List<BillEntity> billEntities = billDAO.getAll(connection, searchParams);
            List<BillDTO> billDTOs = BillMapper.toDTOList(billEntities);

            for (BillDTO dto : billDTOs) {
                try {
                    CustomerDTO customer = CustomerMapper.toDto(customerDAO.searchById(connection, dto.getCustomerId()));
                    if (customer != null) {
                        dto.setCustomerName(customer.getName());
                    } else {
                        dto.setCustomerName("ID: " + dto.getCustomerId());
                    }
                } catch (PahanaEduOnlineBillingSystemException e) {
                    LOGGER.log(Level.WARNING, "Customer not found for bill ID " + dto.getId() + ". Displaying ID instead.");
                    dto.setCustomerName("ID: " + dto.getCustomerId());
                }
                try {
                    UserDTO generatedByUser = userService.searchById(dto.getCreatedBy());
                    if (generatedByUser != null) {
                        dto.setCreateByUsername(generatedByUser.getUsername());
                    } else {
                        dto.setCreateByUsername("ID: " + dto.getCreatedBy());
                    }
                } catch (PahanaEduOnlineBillingSystemException e) {
                    LOGGER.log(Level.WARNING, "Generated By user not found for bill ID " + dto.getId() + ". Displaying ID instead.");
                    dto.setCreateByUsername("ID: " + dto.getCreatedBy());
                }
            }

            return billDTOs;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during getAll bills: " + e.getMessage(), e);
            throw new PahanaEduOnlineBillingSystemException(ExceptionType.DATABASE_ERROR);
        } finally {
            DBUtil.closeConnection(connection);
        }
    }
}
