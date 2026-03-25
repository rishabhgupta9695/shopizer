package com.salesmanager.shop.model.order.v1;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.salesmanager.shop.model.order.PersistableOrderProduct;
import com.salesmanager.shop.model.order.transaction.PersistablePayment;

/**
 * Payload for admin-initiated order creation (no cart required).
 */
public class PersistableAdminOrder extends Order {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "customerId is required")
    private Long customerId;

    @NotEmpty(message = "At least one product is required")
    private List<PersistableOrderProduct> products = new ArrayList<>();

    @NotNull(message = "payment is required")
    private PersistablePayment payment;

    private String status = "ORDERED";

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public List<PersistableOrderProduct> getProducts() { return products; }
    public void setProducts(List<PersistableOrderProduct> products) { this.products = products; }

    public PersistablePayment getPayment() { return payment; }
    public void setPayment(PersistablePayment payment) { this.payment = payment; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
