package com.salesmanager.shop.store.facade.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.catalog.product.ProductService;
import com.salesmanager.core.business.services.catalog.product.attribute.ProductAttributeService;
import com.salesmanager.core.business.services.catalog.product.file.DigitalProductService;
import com.salesmanager.core.business.services.customer.CustomerService;
import com.salesmanager.core.business.services.order.OrderService;
import com.salesmanager.core.business.services.payments.PaymentService;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.order.Order;
import com.salesmanager.core.model.order.OrderTotalSummary;
import com.salesmanager.core.model.order.orderproduct.OrderProduct;
import com.salesmanager.core.model.order.orderstatus.OrderStatus;
import com.salesmanager.core.model.payments.Payment;
import com.salesmanager.core.model.payments.PaymentType;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.mapper.order.ReadableOrderProductMapper;
import com.salesmanager.shop.model.order.PersistableOrderProduct;
import com.salesmanager.shop.model.order.ReadableOrderProduct;
import com.salesmanager.shop.model.order.v1.PersistableAdminOrder;
import com.salesmanager.shop.model.order.v1.ReadableOrder;
import com.salesmanager.shop.populator.order.OrderProductPopulator;
import com.salesmanager.shop.store.api.exception.ResourceNotFoundException;
import com.salesmanager.shop.store.api.exception.ServiceRuntimeException;

@Service
public class AdminOrderFacadeImpl implements AdminOrderFacade {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductAttributeService productAttributeService;

    @Autowired
    private DigitalProductService digitalProductService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ReadableOrderProductMapper readableOrderProductMapper;

    @Override
    public ReadableOrder createOrder(PersistableAdminOrder adminOrder, MerchantStore store, Language language) {

        Customer customer = customerService.getById(adminOrder.getCustomerId());
        if (customer == null) {
            throw new ResourceNotFoundException("Customer not found: " + adminOrder.getCustomerId());
        }

        try {
            Order modelOrder = new Order();
            modelOrder.setDatePurchased(new Date());
            modelOrder.setBilling(customer.getBilling());
            modelOrder.setDelivery(customer.getDelivery());
            modelOrder.setCustomerEmailAddress(customer.getEmailAddress());
            modelOrder.setCustomerId(customer.getId());
            modelOrder.setMerchant(store);
            modelOrder.setCurrency(store.getCurrency());
            modelOrder.setLocale(Locale.ENGLISH);

            OrderStatus status = OrderStatus.ORDERED;
            try {
                status = OrderStatus.valueOf(adminOrder.getStatus().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
            modelOrder.setStatus(status);

            String paymentType = adminOrder.getPayment() != null
                    ? adminOrder.getPayment().getPaymentType()
                    : PaymentType.FREE.name();

            modelOrder.setPaymentType(PaymentType.valueOf(paymentType.toUpperCase()));
            modelOrder.setPaymentModuleCode(paymentType.toLowerCase());

            OrderProductPopulator populator = new OrderProductPopulator();
            populator.setProductService(productService);
            populator.setDigitalProductService(digitalProductService);
            populator.setProductAttributeService(productAttributeService);

            Set<OrderProduct> orderProducts = new LinkedHashSet<>();
            BigDecimal total = BigDecimal.ZERO;

            for (PersistableOrderProduct item : adminOrder.getProducts()) {
                Product product = productService.getBySku(item.getSku(), store, language);
                if (product == null) {
                    throw new ResourceNotFoundException("Product not found: " + item.getSku());
                }
                BigDecimal itemPrice = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;

                com.salesmanager.core.model.shoppingcart.ShoppingCartItem cartItem =
                        new com.salesmanager.core.model.shoppingcart.ShoppingCartItem();
                cartItem.setSku(item.getSku());
                cartItem.setQuantity(item.getOrderedQuantity());
                cartItem.setItemPrice(itemPrice);
                cartItem.setProduct(product);

                com.salesmanager.core.model.catalog.product.price.FinalPrice finalPrice =
                        new com.salesmanager.core.model.catalog.product.price.FinalPrice();
                finalPrice.setFinalPrice(itemPrice);
                finalPrice.setOriginalPrice(itemPrice);

                com.salesmanager.core.model.catalog.product.price.ProductPrice productPrice =
                        new com.salesmanager.core.model.catalog.product.price.ProductPrice();
                productPrice.setDefaultPrice(true);
                productPrice.setProductPriceAmount(itemPrice);
                productPrice.setCode("DEFAULT");
                finalPrice.setProductPrice(productPrice);

                cartItem.setFinalPrice(finalPrice);

                OrderProduct orderProduct = populator.populate(cartItem, new OrderProduct(), store, language);
                orderProduct.setOrder(modelOrder);
                orderProducts.add(orderProduct);

                total = total.add(itemPrice.multiply(BigDecimal.valueOf(item.getOrderedQuantity())));
            }

            modelOrder.setOrderProducts(orderProducts);
            modelOrder.setTotal(total);

            Payment payment = new Payment();
            payment.setPaymentType(PaymentType.valueOf(paymentType.toUpperCase()));
            payment.setAmount(total);
            payment.setModuleName(paymentType.toLowerCase());
            payment.setCurrency(store.getCurrency());

            orderService.processOrder(modelOrder, customer, new ArrayList<>(orderProducts.stream()
                    .map(op -> {
                        com.salesmanager.core.model.shoppingcart.ShoppingCartItem i =
                                new com.salesmanager.core.model.shoppingcart.ShoppingCartItem();
                        i.setSku(op.getSku());
                        i.setQuantity(op.getProductQuantity());
                        return i;
                    }).collect(java.util.stream.Collectors.toList())),
                    buildSummary(total), payment, store);

            // map to readable
            ReadableOrder readable = new ReadableOrder();
            readable.setId(modelOrder.getId());
            readable.setStatus(modelOrder.getStatus() != null ? modelOrder.getStatus().name() : null);

            List<ReadableOrderProduct> readableProducts = new ArrayList<>();
            for (OrderProduct op : modelOrder.getOrderProducts()) {
                readableProducts.add(readableOrderProductMapper.convert(op, store, language));
            }
            readable.setProducts(readableProducts);

            return readable;

        } catch (ServiceException | com.salesmanager.core.business.exception.ConversionException e) {
            throw new ServiceRuntimeException("Error creating admin order: " + e.getMessage(), e);
        }
    }

    private OrderTotalSummary buildSummary(BigDecimal total) {
        OrderTotalSummary summary = new OrderTotalSummary();
        summary.setTotal(total);
        summary.setTotals(new ArrayList<>());
        return summary;
    }
}
