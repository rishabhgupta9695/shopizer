package com.salesmanager.shop.model.order;

import java.math.BigDecimal;
import java.util.List;

public class OrderStatsResponse {

    private int totalOrders;
    private BigDecimal totalRevenue;
    private int pendingOrders;
    private int completedOrders;
    private List<RevenueByDay> revenueByDay;

    public static class RevenueByDay {
        private String date;
        private BigDecimal amount;

        public RevenueByDay() {}

        public RevenueByDay(String date, BigDecimal amount) {
            this.date = date;
            this.amount = amount;
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }

    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public int getPendingOrders() { return pendingOrders; }
    public void setPendingOrders(int pendingOrders) { this.pendingOrders = pendingOrders; }

    public int getCompletedOrders() { return completedOrders; }
    public void setCompletedOrders(int completedOrders) { this.completedOrders = completedOrders; }

    public List<RevenueByDay> getRevenueByDay() { return revenueByDay; }
    public void setRevenueByDay(List<RevenueByDay> revenueByDay) { this.revenueByDay = revenueByDay; }
}
