package com.salesmanager.shop.store.facade.order;

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.order.v1.PersistableAdminOrder;
import com.salesmanager.shop.model.order.v1.ReadableOrder;

public interface AdminOrderFacade {

    /**
     * Creates an order directly from the admin panel without requiring a shopping cart.
     */
    ReadableOrder createOrder(PersistableAdminOrder order, MerchantStore store, Language language);
}
