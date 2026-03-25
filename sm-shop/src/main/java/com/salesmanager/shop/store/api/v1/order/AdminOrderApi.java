package com.salesmanager.shop.store.api.v1.order;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.constants.Constants;
import com.salesmanager.shop.model.order.v1.PersistableAdminOrder;
import com.salesmanager.shop.model.order.v1.ReadableOrder;
import com.salesmanager.shop.store.facade.order.AdminOrderFacade;
import com.salesmanager.shop.utils.AuthorizationUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/api/v1")
@Api(tags = {"Admin Order Management"})
@SwaggerDefinition(tags = {@Tag(name = "Admin order resource", description = "Create orders from admin panel")})
public class AdminOrderApi {

    @Inject
    private AdminOrderFacade adminOrderFacade;

    @Inject
    private AuthorizationUtils authorizationUtils;

    @PostMapping("/private/orders")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    @ApiOperation(httpMethod = "POST", value = "Create order from admin",
            notes = "Creates an order directly from the admin panel without a shopping cart",
            produces = "application/json", response = ReadableOrder.class)
    public ReadableOrder create(
            @Valid @RequestBody PersistableAdminOrder order,
            @ApiIgnore MerchantStore merchantStore,
            @ApiIgnore Language language) {

        String user = authorizationUtils.authenticatedUser();
        authorizationUtils.authorizeUser(user,
                Stream.of(Constants.GROUP_SUPERADMIN, Constants.GROUP_ADMIN, Constants.GROUP_ADMIN_ORDER)
                        .collect(Collectors.toList()),
                merchantStore);

        return adminOrderFacade.createOrder(order, merchantStore, language);
    }
}
