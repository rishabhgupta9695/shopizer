package com.salesmanager.test.shop.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.catalog.product.ProductService;
import com.salesmanager.core.business.services.customer.CustomerService;
import com.salesmanager.core.business.services.order.OrderService;
import com.salesmanager.core.business.services.payments.PaymentService;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.common.Billing;
import com.salesmanager.core.model.common.Delivery;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.order.Order;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.mapper.order.ReadableOrderProductMapper;
import com.salesmanager.shop.model.order.PersistableOrderProduct;
import com.salesmanager.shop.model.order.transaction.PersistablePayment;
import com.salesmanager.shop.model.order.v1.PersistableAdminOrder;
import com.salesmanager.shop.model.order.v1.ReadableOrder;
import com.salesmanager.shop.store.api.exception.ResourceNotFoundException;
import com.salesmanager.shop.store.facade.order.AdminOrderFacadeImpl;

@ExtendWith(MockitoExtension.class)
public class AdminOrderFacadeTest {

    @Mock private CustomerService customerService;
    @Mock private ProductService productService;
    @Mock private OrderService orderService;
    @Mock private PaymentService paymentService;
    @Mock private ReadableOrderProductMapper readableOrderProductMapper;

    @InjectMocks
    private AdminOrderFacadeImpl adminOrderFacade;

    private MerchantStore store;
    private Language language;
    private Customer customer;
    private Product product;

    @BeforeEach
    void setUp() {
        store = new MerchantStore();
        store.setCode("DEFAULT");

        language = new Language();
        language.setCode("en");

        customer = new Customer();
        customer.setId(1L);
        customer.setEmailAddress("test@test.com");
        customer.setBilling(new Billing());
        customer.setDelivery(new Delivery());

        product = new Product();
        product.setSku("SKU-001");
    }

    @Test
    void createOrder_throwsResourceNotFoundException_whenCustomerNotFound() {
        when(customerService.getById(99L)).thenReturn(null);

        PersistableAdminOrder order = buildOrder(99L, "SKU-001", 1, BigDecimal.TEN);

        assertThrows(ResourceNotFoundException.class,
                () -> adminOrderFacade.createOrder(order, store, language));
    }

    @Test
    void createOrder_throwsResourceNotFoundException_whenProductNotFound() throws ServiceException {
        when(customerService.getById(1L)).thenReturn(customer);
        when(productService.getBySku(eq("MISSING"), any(), any())).thenReturn(null);

        PersistableAdminOrder order = buildOrder(1L, "MISSING", 1, BigDecimal.TEN);

        assertThrows(ResourceNotFoundException.class,
                () -> adminOrderFacade.createOrder(order, store, language));
    }

    @Test
    void createOrder_returnsReadableOrder_onSuccess() throws ServiceException {
        when(customerService.getById(1L)).thenReturn(customer);
        when(productService.getBySku(eq("SKU-001"), any(), any())).thenReturn(product);

        // orderService.processOrder sets the id on the modelOrder via side effect
        doAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(42L);
            return null;
        }).when(orderService).processOrder(any(), any(), anyList(), any(), any(), any());

        PersistableAdminOrder order = buildOrder(1L, "SKU-001", 2, new BigDecimal("25.00"));

        ReadableOrder result = adminOrderFacade.createOrder(order, store, language);

        assertNotNull(result);
        assertEquals(42L, result.getId());
    }

    @Test
    void createOrder_usesOrderedStatus_whenStatusIsValid() throws ServiceException {
        when(customerService.getById(1L)).thenReturn(customer);
        when(productService.getBySku(any(), any(), any())).thenReturn(product);
        doAnswer(inv -> { ((Order) inv.getArgument(0)).setId(1L); return null; })
                .when(orderService).processOrder(any(), any(), anyList(), any(), any(), any());

        PersistableAdminOrder order = buildOrder(1L, "SKU-001", 1, BigDecimal.TEN);
        order.setStatus("PROCESSED");

        ReadableOrder result = adminOrderFacade.createOrder(order, store, language);
        assertNotNull(result);
        assertEquals("PROCESSED", result.getStatus());
    }

    @Test
    void createOrder_fallsBackToOrdered_whenStatusIsInvalid() throws ServiceException {
        when(customerService.getById(1L)).thenReturn(customer);
        when(productService.getBySku(any(), any(), any())).thenReturn(product);
        doAnswer(inv -> { ((Order) inv.getArgument(0)).setId(1L); return null; })
                .when(orderService).processOrder(any(), any(), anyList(), any(), any(), any());

        PersistableAdminOrder order = buildOrder(1L, "SKU-001", 1, BigDecimal.TEN);
        order.setStatus("INVALID_STATUS");

        // should not throw — falls back to ORDERED
        ReadableOrder result = adminOrderFacade.createOrder(order, store, language);
        assertNotNull(result);
    }

    // --- helpers ---

    private PersistableAdminOrder buildOrder(Long customerId, String sku, int qty, BigDecimal price) {
        PersistableAdminOrder order = new PersistableAdminOrder();
        order.setCustomerId(customerId);

        PersistableOrderProduct item = new PersistableOrderProduct();
        item.setSku(sku);
        item.setOrderedQuantity(qty);
        item.setPrice(price);
        order.setProducts(List.of(item));

        PersistablePayment payment = new PersistablePayment();
        payment.setPaymentType("FREE");
        order.setPayment(payment);

        return order;
    }
}
