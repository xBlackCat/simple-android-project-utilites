package org.xblackcat.android.util;


import org.junit.Assert;
import org.junit.Test;

/**
 * 24.10.12 9:05
 *
 * @author xBlackCat
 */
public class VersionTest {
    @Test
    public void comparativeTest() {
        Assert.assertEquals(0, new Version("0.0.0").compareTo(new Version("0.0.0")));
        Assert.assertEquals(0, new Version("1").compareTo(new Version("1")));
        Assert.assertEquals(0, new Version("0.1.0").compareTo(new Version("0.1.0-debug")));
        Assert.assertEquals(0, new Version("1.0.1").compareTo(new Version("1release.0.1")));

        Assert.assertEquals(1, new Version("1.0.0").compareTo(new Version("0.0.0")));
        Assert.assertEquals(1, new Version("0.1.0").compareTo(new Version("0.0.0")));
        Assert.assertEquals(1, new Version("0.0.1").compareTo(new Version("0.0.0")));
        Assert.assertEquals(1, new Version("1.0.1").compareTo(new Version("1")));
        Assert.assertEquals(1, new Version("1.0.1").compareTo(new Version("1.0")));
        Assert.assertEquals(1, new Version("1.0.1").compareTo(new Version("1.0.0")));

        Assert.assertEquals(-1, new Version("0.0.0").compareTo(new Version("1.0.0")));
        Assert.assertEquals(-1, new Version("0.0.0").compareTo(new Version("0.1.0")));
        Assert.assertEquals(-1, new Version("0.0.0").compareTo(new Version("0.0.1")));
        Assert.assertEquals(-1, new Version("1").compareTo(new Version("1.0.1")));
        Assert.assertEquals(-1, new Version("1.0").compareTo(new Version("1.0.1")));
        Assert.assertEquals(-1, new Version("1.0.0").compareTo(new Version("1.0.1")));
    }
}
