package com.salesmanager.test.shop.unit;

import com.salesmanager.shop.utils.GeoLocationUtils;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.*;

public class GeoLocationUtilsTest {

    @Test
    public void testGetClientIpAddress_xForwardedFor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "192.168.1.1");
        assertEquals("192.168.1.1", GeoLocationUtils.getClientIpAddress(request));
    }

    @Test
    public void testGetClientIpAddress_fallsBackToRemoteAddr() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        assertEquals("10.0.0.1", GeoLocationUtils.getClientIpAddress(request));
    }

    @Test
    public void testGetClientIpAddress_skipsUnknown() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "unknown");
        request.addHeader("Proxy-Client-IP", "172.16.0.5");
        assertEquals("172.16.0.5", GeoLocationUtils.getClientIpAddress(request));
    }

    @Test
    public void testGetClientIpAddress_skipsEmpty() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "");
        request.setRemoteAddr("127.0.0.1");
        assertEquals("127.0.0.1", GeoLocationUtils.getClientIpAddress(request));
    }
}
