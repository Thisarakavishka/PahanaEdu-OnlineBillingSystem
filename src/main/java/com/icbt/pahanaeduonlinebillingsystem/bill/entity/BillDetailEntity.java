package com.icbt.pahanaeduonlinebillingsystem.bill.entity;

import com.icbt.pahanaeduonlinebillingsystem.common.base.SuperEntity;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * @author Thisara Kavishka
 * @date 2025-08-11
 * @since 1.0
 */
public class BillDetailEntity implements SuperEntity {
    private int id;
    private int billId;
    private int itemId;
    private String itemNameAtSale;
    private BigDecimal unitPriceAtSale;
    private int units;
    private BigDecimal total;

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

    public int getBillId() {
        return billId;
    }

    public void setBillId(int billId) {
        this.billId = billId;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getItemNameAtSale() {
        return itemNameAtSale;
    }

    public void setItemNameAtSale(String itemNameAtSale) {
        this.itemNameAtSale = itemNameAtSale;
    }

    public BigDecimal getUnitPriceAtSale() {
        return unitPriceAtSale;
    }

    public void setUnitPriceAtSale(BigDecimal unitPriceAtSale) {
        this.unitPriceAtSale = unitPriceAtSale;
    }

    public int getUnits() {
        return units;
    }

    public void setUnits(int units) {
        this.units = units;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
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
