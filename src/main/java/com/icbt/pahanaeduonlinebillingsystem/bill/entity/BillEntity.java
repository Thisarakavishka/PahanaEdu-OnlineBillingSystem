package com.icbt.pahanaeduonlinebillingsystem.bill.entity;

import com.icbt.pahanaeduonlinebillingsystem.common.base.SuperEntity;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author Thisara Kavishka
 * @date 2025-08-11
 * @since 1.0
 */
public class BillEntity implements SuperEntity {
    private int id;
    private int customerId;
    private BigDecimal totalAmount;
    private List<BillDetailEntity> details;

    private Integer createdBy;
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<BillDetailEntity> getDetails() {
        return details;
    }

    public void setDetails(List<BillDetailEntity> details) {
        this.details = details;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
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
