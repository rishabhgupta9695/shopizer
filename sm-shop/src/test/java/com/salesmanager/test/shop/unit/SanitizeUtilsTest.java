package com.salesmanager.test.shop.unit;

import com.salesmanager.shop.utils.SanitizeUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class SanitizeUtilsTest {

    @Test
    public void testGetSafeRequestParamString_clean() {
        String result = SanitizeUtils.getSafeRequestParamString("hello");
        assertEquals("hello", result);
    }

    @Test
    public void testGetSafeRequestParamString_empty() {
        String result = SanitizeUtils.getSafeRequestParamString("");
        assertEquals("", result);
    }

    @Test
    public void testGetSafeRequestParamString_null() {
        String result = SanitizeUtils.getSafeRequestParamString(null);
        assertEquals("", result);
    }

    @Test
    public void testGetSafeRequestParamString_stripsBlacklisted() {
        // semicolons, percent signs, etc. should be stripped
        String result = SanitizeUtils.getSafeRequestParamString("hello;world");
        assertFalse(result.contains(";"));
    }

    @Test
    public void testGetSafeRequestParamString_stripsPercent() {
        String result = SanitizeUtils.getSafeRequestParamString("val%20ue");
        assertFalse(result.contains("%"));
    }

    @Test
    public void testGetSafeRequestParamString_allowsAlphanumeric() {
        String result = SanitizeUtils.getSafeRequestParamString("abc123");
        assertTrue(result.contains("abc123"));
    }

    @Test
    public void testGetSafeRequestParamString_allowsDash() {
        String result = SanitizeUtils.getSafeRequestParamString("my-value");
        assertTrue(result.contains("my-value"));
    }
}
