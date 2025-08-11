package com.icbt.pahanaeduonlinebillingsystem.bill.dto;

import com.icbt.pahanaeduonlinebillingsystem.common.base.SuperDTO;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author Thisara Kavishka
 * @date 2025-08-11
 * @since 1.0
 */
public class BillDTO implements SuperDTO {
    private int id;
    private int customerId;
    private String customerAccountNumber;
    private String customerName;
    private BigDecimal totalAmount;
    private List<BillDetailDTO> details;

    private Integer createdBy;
    private String createByUsername;
    private Timestamp createdAt;
    private Integer deletedBy;
    private Timestamp deletedAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomerAccountNumber() {
        return customerAccountNumber;
    }

    public void setCustomerAccountNumber(String customerAccountNumber) {
        this.customerAccountNumber = customerAccountNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<BillDetailDTO> getDetails() {
        return details;
    }

    public void setDetails(List<BillDetailDTO> details) {
        this.details = details;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreateByUsername() {
        return createByUsername;
    }

    public void setCreateByUsername(String createByUsername) {
        this.createByUsername = createByUsername;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(Integer deletedBy) {
        this.deletedBy = deletedBy;
    }

    public Timestamp getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Timestamp deletedAt) {
        this.deletedAt = deletedAt;
    }
}
