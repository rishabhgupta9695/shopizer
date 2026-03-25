package com.salesmanager.test.shop.unit;

import com.salesmanager.shop.utils.BeanUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class BeanUtilsTest {

    public static class SampleBean {
        private String name;
        private int value;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }

    @Test
    public void testGetPropertyValue_string() throws Exception {
        SampleBean bean = new SampleBean();
        bean.setName("test");
        assertEquals("test", BeanUtils.newInstance().getPropertyValue(bean, "name"));
    }

    @Test
    public void testGetPropertyValue_int() throws Exception {
        SampleBean bean = new SampleBean();
        bean.setValue(42);
        assertEquals(42, BeanUtils.newInstance().getPropertyValue(bean, "value"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPropertyValue_nullBean() throws Exception {
        BeanUtils.newInstance().getPropertyValue(null, "name");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPropertyValue_nullProperty() throws Exception {
        BeanUtils.newInstance().getPropertyValue(new SampleBean(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPropertyValue_unknownProperty() throws Exception {
        BeanUtils.newInstance().getPropertyValue(new SampleBean(), "nonExistent");
    }
}
