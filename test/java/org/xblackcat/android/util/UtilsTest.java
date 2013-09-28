package org.xblackcat.android.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * 01.07.13 16:44
 *
 * @author xBlackCat
 */
public class UtilsTest {
    @Test
    public void simpleStrip() {
        assertEquals("", TextUtils.stripHtmlTags(null));
        assertEquals("", TextUtils.stripHtmlTags(""));
        assertEquals("", TextUtils.stripHtmlTags("              "));
        assertEquals("", TextUtils.stripHtmlTags("<p/>"));
        assertEquals("", TextUtils.stripHtmlTags("<span>    </span>"));
    }

    @Test
    public void stripTag() {
        assertEquals("Hello, world", TextUtils.stripHtmlTags("Hello,<br/>world"));
        assertEquals("Hello, world", TextUtils.stripHtmlTags("<p>Hello,<br/>world</p>"));
        assertEquals(
                "Hello, world",
                TextUtils.stripHtmlTags(
                        "Hello, <img src=\"http://www.europeanpokertour.com/data/feeds/PSTV3/thumbnails/NonRetina/LOGO_EPT.png\" /><br/>world"
                )
        );
        assertEquals("Hello, world", TextUtils.stripHtmlTags("<span style='10>5'>Hello,<br/>world</span>"));
        assertEquals("Hello, world", TextUtils.stripHtmlTags("<span style=\"10>5\">Hello,<br/>world</span>"));
    }
}
