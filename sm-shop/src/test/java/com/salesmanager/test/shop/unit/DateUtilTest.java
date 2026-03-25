package com.salesmanager.test.shop.unit;

import com.salesmanager.shop.utils.DateUtil;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class DateUtilTest {

    @Test
    public void testGenerateTimeStamp_notNull() {
        assertNotNull(DateUtil.generateTimeStamp());
    }

    @Test
    public void testGenerateTimeStamp_length() {
        // yyyyMMddHHmmSS where SS = milliseconds (up to 3 digits) = 15 chars
        assertTrue(DateUtil.generateTimeStamp().length() >= 14);
    }

    @Test
    public void testFormatDate_nullReturnsToday() {
        // null input should return today's date formatted, not null
        assertNotNull(DateUtil.formatDate(null));
    }

    @Test
    public void testFormatDate_format() {
        // yyyy-MM-dd format: length 10, contains dashes
        String formatted = DateUtil.formatDate(new Date());
        assertEquals(10, formatted.length());
        assertEquals('-', formatted.charAt(4));
        assertEquals('-', formatted.charAt(7));
    }

    @Test
    public void testFormatYear_null() {
        assertNull(DateUtil.formatYear(null));
    }

    @Test
    public void testFormatYear_length() {
        // DEFAULT_DATE_FORMAT_YEAR = "yyyy" = 4 chars
        assertEquals(4, DateUtil.formatYear(new Date()).length());
    }

    @Test
    public void testFormatLongDate_null() {
        assertNull(DateUtil.formatLongDate(null));
    }

    @Test
    public void testFormatLongDate_notNull() {
        assertNotNull(DateUtil.formatLongDate(new Date()));
    }

    @Test
    public void testGetDate_parsesCorrectly() throws Exception {
        Date d = DateUtil.getDate("2024-01-15");
        assertNotNull(d);
    }

    @Test(expected = Exception.class)
    public void testGetDate_invalidFormat() throws Exception {
        DateUtil.getDate("15/01/2024");
    }

    @Test
    public void testAddDaysToCurrentDate_positive() {
        Date future = DateUtil.addDaysToCurrentDate(5);
        assertTrue(future.after(new Date()));
    }

    @Test
    public void testAddDaysToCurrentDate_negative() {
        Date past = DateUtil.addDaysToCurrentDate(-5);
        assertTrue(past.before(new Date()));
    }

    @Test
    public void testGetPresentDate_notNull() {
        assertNotNull(DateUtil.getPresentDate());
    }

    @Test
    public void testGetPresentYear_fourDigits() {
        String year = DateUtil.getPresentYear();
        assertNotNull(year);
        assertEquals(4, year.length());
    }

    @Test
    public void testDateBeforeEqualsDate_nullInputs() {
        assertTrue(DateUtil.dateBeforeEqualsDate(null, new Date()));
        assertTrue(DateUtil.dateBeforeEqualsDate(new Date(), null));
        assertTrue(DateUtil.dateBeforeEqualsDate(null, null));
    }

    @Test
    public void testDateBeforeEqualsDate_before() {
        Date past = DateUtil.addDaysToCurrentDate(-1);
        assertTrue(DateUtil.dateBeforeEqualsDate(past, new Date()));
    }

    @Test
    public void testDateBeforeEqualsDate_after() {
        Date future = DateUtil.addDaysToCurrentDate(1);
        assertFalse(DateUtil.dateBeforeEqualsDate(future, new Date()));
    }

    @Test
    public void testDateBeforeEqualsDate_equal() {
        Date d = new Date(1000000L);
        assertTrue(DateUtil.dateBeforeEqualsDate(d, d));
    }
}
