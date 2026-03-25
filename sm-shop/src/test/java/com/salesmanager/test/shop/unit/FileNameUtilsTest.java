package com.salesmanager.test.shop.unit;

import com.salesmanager.shop.utils.FileNameUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class FileNameUtilsTest {

    private FileNameUtils fileNameUtils;

    @Before
    public void setUp() {
        fileNameUtils = new FileNameUtils();
    }

    @Test
    public void testValidFileName_withExtension() {
        assertTrue(fileNameUtils.validFileName("image.jpg"));
    }

    @Test
    public void testValidFileName_noExtension() {
        assertFalse(fileNameUtils.validFileName("imagejpg"));
    }

    @Test
    public void testValidFileName_noBaseName() {
        assertFalse(fileNameUtils.validFileName(".jpg"));
    }

    @Test
    public void testValidFileName_emptyString() {
        assertFalse(fileNameUtils.validFileName(""));
    }

    @Test
    public void testValidFileName_withPath() {
        assertTrue(fileNameUtils.validFileName("folder/file.png"));
    }

    @Test
    public void testValidFileName_multipleExtensions() {
        assertTrue(fileNameUtils.validFileName("archive.tar.gz"));
    }
}
