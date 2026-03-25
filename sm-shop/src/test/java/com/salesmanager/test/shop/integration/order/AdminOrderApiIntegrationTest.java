package com.salesmanager.test.shop.integration.order;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import com.salesmanager.shop.model.order.PersistableOrderProduct;
import com.salesmanager.shop.model.order.transaction.PersistablePayment;
import com.salesmanager.shop.model.order.v1.PersistableAdminOrder;
import com.salesmanager.shop.model.order.v1.ReadableOrder;
import com.salesmanager.shop.populator.customer.ReadableCustomerList;
import com.salesmanager.test.shop.common.ServicesTestSupport;

public class AdminOrderApiIntegrationTest extends ServicesTestSupport {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void createOrder_asAdmin_returnsCreated() throws Exception {
        // create a product to order
        sampleProduct("admin-order-test-sku");

        // get a customer id
        ReadableCustomerList customers = fetchCustomers();
        assertNotNull(customers);
        Long customerId = customers.getCustomers().get(0).getId();

        PersistableAdminOrder order = buildAdminOrder(customerId, "admin-order-test-sku");

        HttpEntity<PersistableAdminOrder> entity = new HttpEntity<>(order, getHeader());
        ResponseEntity<ReadableOrder> response = testRestTemplate.postForEntity(
                "/api/v1/private/orders", entity, ReadableOrder.class);

        assertThat(response.getStatusCode(), is(CREATED));
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
    }

    @Test
    public void createOrder_withoutAuth_returnsForbidden() {
        PersistableAdminOrder order = buildAdminOrder(1L, "some-sku");

        HttpEntity<PersistableAdminOrder> entity = new HttpEntity<>(order);
        ResponseEntity<String> response = testRestTemplate.postForEntity(
                "/api/v1/private/orders", entity, String.class);

        assertThat(response.getStatusCode(), is(FORBIDDEN));
    }

    // --- helper ---

    private PersistableAdminOrder buildAdminOrder(Long customerId, String sku) {
        PersistableAdminOrder order = new PersistableAdminOrder();
        order.setCustomerId(customerId);

        PersistableOrderProduct item = new PersistableOrderProduct();
        item.setSku(sku);
        item.setOrderedQuantity(1);
        item.setPrice(new BigDecimal("10.00"));
        order.setProducts(List.of(item));

        PersistablePayment payment = new PersistablePayment();
        payment.setPaymentType("FREE");
        order.setPayment(payment);

        return order;
    }
}
