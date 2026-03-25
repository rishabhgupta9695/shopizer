package com.salesmanager.test.shop.integration.order;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.salesmanager.shop.model.order.OrderStatsResponse;
import com.salesmanager.test.shop.common.ServicesTestSupport;

public class OrderStatsApiIntegrationTest extends ServicesTestSupport {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    void getStats_withValidToken_returns200AndShape() {
        HttpEntity<String> entity = new HttpEntity<>(getHeader());

        ResponseEntity<OrderStatsResponse> response = testRestTemplate.exchange(
                "/api/v1/private/orders/stats?period=7d",
                HttpMethod.GET, entity, OrderStatsResponse.class);

        assertEquals(OK, response.getStatusCode());
        OrderStatsResponse body = response.getBody();
        assertNotNull(body);
        assertNotNull(body.getRevenueByDay());
        assertEquals(7, body.getRevenueByDay().size()); // 7 days = 7 data points
        assertNotNull(body.getTotalRevenue());
        assertTrue(body.getTotalOrders() >= 0);
        assertTrue(body.getPendingOrders() >= 0);
        assertTrue(body.getCompletedOrders() >= 0);
    }

    @Test
    void getStats_30dPeriod_returns30DataPoints() {
        HttpEntity<String> entity = new HttpEntity<>(getHeader());

        ResponseEntity<OrderStatsResponse> response = testRestTemplate.exchange(
                "/api/v1/private/orders/stats?period=30d",
                HttpMethod.GET, entity, OrderStatsResponse.class);

        assertEquals(OK, response.getStatusCode());
        assertEquals(30, response.getBody().getRevenueByDay().size());
    }

    @Test
    void getStats_90dPeriod_returns90DataPoints() {
        HttpEntity<String> entity = new HttpEntity<>(getHeader());

        ResponseEntity<OrderStatsResponse> response = testRestTemplate.exchange(
                "/api/v1/private/orders/stats?period=90d",
                HttpMethod.GET, entity, OrderStatsResponse.class);

        assertEquals(OK, response.getStatusCode());
        assertEquals(90, response.getBody().getRevenueByDay().size());
    }

    @Test
    void getStats_defaultPeriod_fallsBackTo7d() {
        HttpEntity<String> entity = new HttpEntity<>(getHeader());

        ResponseEntity<OrderStatsResponse> response = testRestTemplate.exchange(
                "/api/v1/private/orders/stats",
                HttpMethod.GET, entity, OrderStatsResponse.class);

        assertEquals(OK, response.getStatusCode());
        assertEquals(7, response.getBody().getRevenueByDay().size());
    }

    @Test
    void getStats_withoutAuth_returns401() {
        ResponseEntity<String> response = testRestTemplate.exchange(
                "/api/v1/private/orders/stats?period=7d",
                HttpMethod.GET, HttpEntity.EMPTY, String.class);

        assertEquals(UNAUTHORIZED, response.getStatusCode());
    }
}
