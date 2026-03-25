-- Seed data for Order Stats Dashboard
-- Run against SALESMANAGER database
-- Assumes merchant store id=1 (DEFAULT store), currency id=1 (USD)
-- Adjust MERCHANTID and CURRENCY_ID if different in your instance

USE SALESMANAGER;

-- Helper: check your merchant and currency ids
-- SELECT MERCHANT_ID FROM MERCHANT_STORE WHERE STORE_CODE='DEFAULT';
-- SELECT CURRENCY_ID FROM CURRENCY WHERE CODE='USD';

-- resolve IDs dynamically
SET @country_id = (SELECT COUNTRY_ID FROM COUNTRY WHERE COUNTRY_ISOCODE='US' LIMIT 1);
SET @merchant_id = (SELECT MERCHANT_ID FROM MERCHANT_STORE WHERE STORE_CODE='DEFAULT' LIMIT 1);
SET @currency_id = (SELECT CURRENCY_ID FROM CURRENCY WHERE CURRENCY_CODE='USD' LIMIT 1);

INSERT INTO ORDERS (
    ORDER_ID, ORDER_STATUS, DATE_PURCHASED, ORDER_TOTAL,
    PAYMENT_TYPE, PAYMENT_MODULE_CODE,
    CUSTOMER_ID, CUSTOMER_EMAIL_ADDRESS,
    MERCHANTID, CURRENCY_ID,
    LOCALE, CUSTOMER_AGREED, CONFIRMED_ADDRESS, ORDER_TYPE,
    BILLING_FIRST_NAME, BILLING_LAST_NAME, BILLING_COUNTRY_ID
) VALUES
-- Last 7 days - mix of statuses
(9001, 'ORDERED',    DATE_SUB(NOW(), INTERVAL 1 DAY),  120.00, 'FREE', 'free', 1, 'seed@test.com', @merchant_id, @currency_id, 'en', 1, 1, 'ORDER', 'Seed', 'User', @country_id),
(9002, 'ORDERED',    DATE_SUB(NOW(), INTERVAL 1 DAY),   85.50, 'FREE', 'free', 1, 'seed@test.com', @merchant_id, @currency_id, 'en', 1, 1, 'ORDER', 'Seed', 'User', @country_id),
(9003, 'PROCESSED',  DATE_SUB(NOW(), INTERVAL 2 DAY),  200.00, 'FREE', 'free', 1, 'seed@test.com', @merchant_id, @currency_id, 'en', 1, 1, 'ORDER', 'Seed', 'User', @country_id),
(9004, 'DELIVERED',  DATE_SUB(NOW(), INTERVAL 2 DAY),   55.00, 'FREE', 'free', 1, 'seed@test.com', @merchant_id, @currency_id, 'en', 1, 1, 'ORDER', 'Seed', 'User', @country_id),
(9005, 'DELIVERED',  DATE_SUB(NOW(), INTERVAL 3 DAY),  310.00, 'FREE', 'free', 1, 'seed@test.com', @merchant_id, @currency_id, 'en', 1, 1, 'ORDER', 'Seed', 'User', @country_id),
(9006, 'ORDERED',    DATE_SUB(NOW(), INTERVAL 3 DAY),   45.00, 'FREE', 'free', 1, 'seed@test.com', @merchant_id, @currency_id, 'en', 1, 1, 'ORDER', 'Seed', 'User', @country_id),
(9007, 'DELIVERED',  DATE_SUB(NOW(), INTERVAL 4 DAY),  175.00, 'FREE', 'free', 1, 'seed@test.com', @merchant_id, @currency_id, 'en', 1, 1, 'ORDER', 'Seed', 'User', @country_id),
(9008, 'CANCELED',   DATE_SUB(NOW(), INTERVAL 4 DAY),   90.00, 'FREE', 'free', 1, 'seed@test.com', @merchant_id, @currency_id, 'en', 1, 1, 'ORDER', 'Seed', 'User', @country_id),
(9009, 'PROCESSED',  DATE_SUB(NOW(), INTERVAL 5 DAY),  430.00, 'FREE', 'free', 1, 'seed@test.com', @merchant_id, @currency_id, 'en', 1, 1, 'ORDER', 'Seed', 'User', @country_id),
(9010, 'DELIVERED',  DATE_SUB(NOW(), INTERVAL 6 DAY),   60.00, 'FREE', 'free', 1, 'seed@test.com', @merchant_id, @currency_id, 'en', 1, 1, 'ORDER', 'Seed', 'User', @country_id),

-- 8-30 days ago
(9011, 'DELIVERED',  DATE_SUB(NOW(), INTERVAL 8  DAY),  220.00, 'FREE', 'free', 1, 'seed@test.com', @merchant_id, @currency_id, 'en', 1, 1, 'ORDER', 'Seed', 'User', @country_id),
(9012, 'DELIVERED',  DATE_SUB(NOW(), INTERVAL 10 DAY),  150.00, 'FREE', 'free', 1, 'seed@test.com', @merchant_id, @currency_id, 'en', 1, 1, 'ORDER', 'Seed', 'User', @country_id),
(9013, 'ORDERED',    DATE_SUB(NOW(), INTERVAL 12 DAY),   75.00, 'FREE', 'free', 1, 'seed@test.com', @merchant_id, @currency_id, 'en', 1, 1, 'ORDER', 'Seed', 'User', @country_id),
(9014, 'DELIVERED',  DATE_SUB(NOW(), INTERVAL 15 DAY),  500.00, 'FREE', 'free', 1, 'seed@test.com', @merchant_id, @currency_id, 'en', 1, 1, 'ORDER', 'Seed', 'User', @country_id),
(9015, 'PROCESSED',  DATE_SUB(NOW(), INTERVAL 18 DAY),  340.00, 'FREE', 'free', 1, 'seed@test.com', @merchant_id, @currency_id, 'en', 1, 1, 'ORDER', 'Seed', 'User', @country_id),
(9016, 'DELIVERED',  DATE_SUB(NOW(), INTERVAL 20 DAY),  110.00, 'FREE', 'free', 1, 'seed@test.com', @merchant_id, @currency_id, 'en', 1, 1, 'ORDER', 'Seed', 'User', @country_id),
(9017, 'CANCELED',   DATE_SUB(NOW(), INTERVAL 22 DAY),   95.00, 'FREE', 'free', 1, 'seed@test.com', @merchant_id, @currency_id, 'en', 1, 1, 'ORDER', 'Seed', 'User', @country_id),
(9018, 'DELIVERED',  DATE_SUB(NOW(), INTERVAL 25 DAY),  280.00, 'FREE', 'free', 1, 'seed@test.com', @merchant_id, @currency_id, 'en', 1, 1, 'ORDER', 'Seed', 'User', @country_id),
(9019, 'DELIVERED',  DATE_SUB(NOW(), INTERVAL 28 DAY),  190.00, 'FREE', 'free', 1, 'seed@test.com', @merchant_id, @currency_id, 'en', 1, 1, 'ORDER', 'Seed', 'User', @country_id),
(9020, 'ORDERED',    DATE_SUB(NOW(), INTERVAL 30 DAY),   65.00, 'FREE', 'free', 1, 'seed@test.com', @merchant_id, @currency_id, 'en', 1, 1, 'ORDER', 'Seed', 'User', @country_id),

-- 31-90 days ago
(9021, 'DELIVERED',  DATE_SUB(NOW(), INTERVAL 35 DAY),  400.00, 'FREE', 'free', 1, 'seed@test.com', @merchant_id, @currency_id, 'en', 1, 1, 'ORDER', 'Seed', 'User', @country_id),
(9022, 'DELIVERED',  DATE_SUB(NOW(), INTERVAL 40 DAY),  250.00, 'FREE', 'free', 1, 'seed@test.com', @merchant_id, @currency_id, 'en', 1, 1, 'ORDER', 'Seed', 'User', @country_id),
(9023, 'PROCESSED',  DATE_SUB(NOW(), INTERVAL 50 DAY),  320.00, 'FREE', 'free', 1, 'seed@test.com', @merchant_id, @currency_id, 'en', 1, 1, 'ORDER', 'Seed', 'User', @country_id),
(9024, 'DELIVERED',  DATE_SUB(NOW(), INTERVAL 60 DAY),  180.00, 'FREE', 'free', 1, 'seed@test.com', @merchant_id, @currency_id, 'en', 1, 1, 'ORDER', 'Seed', 'User', @country_id),
(9025, 'DELIVERED',  DATE_SUB(NOW(), INTERVAL 75 DAY),  560.00, 'FREE', 'free', 1, 'seed@test.com', @merchant_id, @currency_id, 'en', 1, 1, 'ORDER', 'Seed', 'User', @country_id),
(9026, 'CANCELED',   DATE_SUB(NOW(), INTERVAL 80 DAY),  130.00, 'FREE', 'free', 1, 'seed@test.com', @merchant_id, @currency_id, 'en', 1, 1, 'ORDER', 'Seed', 'User', @country_id),
(9027, 'DELIVERED',  DATE_SUB(NOW(), INTERVAL 88 DAY),  210.00, 'FREE', 'free', 1, 'seed@test.com', @merchant_id, @currency_id, 'en', 1, 1, 'ORDER', 'Seed', 'User', @country_id);
