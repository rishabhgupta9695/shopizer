package com.salesmanager.shop.store.api.v1.order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.salesmanager.core.business.services.order.OrderService;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.order.Order;
import com.salesmanager.core.model.order.OrderCriteria;
import com.salesmanager.core.model.order.OrderList;
import com.salesmanager.core.model.order.orderstatus.OrderStatus;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.constants.Constants;
import com.salesmanager.shop.model.order.OrderStatsResponse;
import com.salesmanager.shop.utils.AuthorizationUtils;

import io.swagger.annotations.Api;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/api/v1")
@Api(tags = {"Order Stats"})
public class OrderStatsApi {

    @Inject
    private OrderService orderService;

    @Inject
    private AuthorizationUtils authorizationUtils;

    @GetMapping("/private/orders/stats")
    public OrderStatsResponse stats(
            @RequestParam(defaultValue = "7d") String period,
            @ApiIgnore MerchantStore merchantStore,
            @ApiIgnore Language language) {

        String user = authorizationUtils.authenticatedUser();
        authorizationUtils.authorizeUser(user,
                Stream.of(Constants.GROUP_SUPERADMIN, Constants.GROUP_ADMIN, Constants.GROUP_ADMIN_ORDER)
                        .collect(Collectors.toList()),
                merchantStore);

        int days = parseDays(period);
        Date from = Date.from(LocalDate.now().minusDays(days)
                .atStartOfDay(ZoneId.systemDefault()).toInstant());

        OrderCriteria criteria = new OrderCriteria();
        criteria.setStartPage(0);
        criteria.setPageSize(1000);
        OrderList orderList = orderService.getOrders(criteria, merchantStore);

        List<Order> orders = orderList.getOrders() == null ? new ArrayList<>() :
                orderList.getOrders().stream()
                        .filter(o -> o.getDatePurchased() != null &&
                                new java.sql.Timestamp(o.getDatePurchased().getTime()).after(from))
                        .collect(Collectors.toList());

        // aggregate
        int total = orders.size();
        int pending = (int) orders.stream()
                .filter(o -> OrderStatus.ORDERED.equals(o.getStatus()) || OrderStatus.PROCESSED.equals(o.getStatus()))
                .count();
        int completed = (int) orders.stream()
                .filter(o -> OrderStatus.DELIVERED.equals(o.getStatus()))
                .count();
        BigDecimal revenue = orders.stream()
                .filter(o -> !OrderStatus.CANCELED.equals(o.getStatus()))
                .map(o -> o.getTotal() != null ? o.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // revenue by day — build a map keyed by date string, fill missing days with 0
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, BigDecimal> byDay = new LinkedHashMap<>();
        for (int i = days - 1; i >= 0; i--) {
            byDay.put(LocalDate.now().minusDays(i).format(fmt), BigDecimal.ZERO);
        }
        orders.stream()
                .filter(o -> !OrderStatus.CANCELED.equals(o.getStatus()) && o.getDatePurchased() != null)
                .forEach(o -> {
                    String day = new java.sql.Timestamp(o.getDatePurchased().getTime())
                            .toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(fmt);
                    byDay.merge(day, o.getTotal() != null ? o.getTotal() : BigDecimal.ZERO, BigDecimal::add);
                });

        List<OrderStatsResponse.RevenueByDay> revenueByDay = byDay.entrySet().stream()
                .map(e -> new OrderStatsResponse.RevenueByDay(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        OrderStatsResponse response = new OrderStatsResponse();
        response.setTotalOrders(total);
        response.setTotalRevenue(revenue);
        response.setPendingOrders(pending);
        response.setCompletedOrders(completed);
        response.setRevenueByDay(revenueByDay);

        return response;
    }

    private int parseDays(String period) {
        switch (period) {
            case "30d": return 30;
            case "90d": return 90;
            default:    return 7;
        }
    }
}
