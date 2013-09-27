package org.xblackcat.android.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * 05.02.13 17:23
 *
 * @author xBlackCat
 */
public class TestParseSize {
    @Test
    public void correctSize() {
        Assert.assertEquals(144896, IOUtils.parseSize("141.5KB"));
        Assert.assertEquals(144896, IOUtils.parseSize("141.5 KB"));
        Assert.assertEquals(144896, IOUtils.parseSize("141.5k"));
        Assert.assertEquals(144896, IOUtils.parseSize("141.5 K"));

        Assert.assertEquals(148373504, IOUtils.parseSize("141.5MB"));
        Assert.assertEquals(148373504, IOUtils.parseSize("141.5 mB"));
        Assert.assertEquals(148373504, IOUtils.parseSize("141.5M"));
        Assert.assertEquals(148373504, IOUtils.parseSize("141.5 m"));

        Assert.assertEquals(141, IOUtils.parseSize("141B"));
        Assert.assertEquals(141, IOUtils.parseSize("141B"));
        Assert.assertEquals(141, IOUtils.parseSize("141"));
        Assert.assertEquals(141, IOUtils.parseSize("141"));
    }
}
