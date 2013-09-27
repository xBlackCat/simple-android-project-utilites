package org.xblackcat.android.ui;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * 25.09.13 9:53
 *
 * @author xBlackCat
 */
public class InfiniteGalleryTest {
    private InfiniteGallery.InfiniteSpinnerAdapter adapter;

    @Before
    public void initializeAdapter() {
        adapter = new InfiniteGallery.InfiniteSpinnerAdapter(
                new FixedSizeAdapter(5),
                Short.MAX_VALUE,
                Short.MAX_VALUE << 1
        );
    }

    @Test
    public void correctionMethods() {
        Assert.assertEquals(32767, adapter.toInfinitePosition(0));
        Assert.assertEquals(32768, adapter.toInfinitePosition(1));
        Assert.assertEquals(32769, adapter.toInfinitePosition(2));
        Assert.assertEquals(32770, adapter.toInfinitePosition(3));
        Assert.assertEquals(32771, adapter.toInfinitePosition(4));
        try {
            adapter.toInfinitePosition(5);
            Assert.fail("Expecting an IndexOutOfBoundsException exception");
        } catch (IndexOutOfBoundsException e) {
            //
        }
        try {
            adapter.toInfinitePosition(-1);
            Assert.fail("Expecting an IndexOutOfBoundsException exception");
        } catch (IndexOutOfBoundsException e) {
            //
        }
    }

    @Test
    public void adjustForSelected() {
        Assert.assertEquals(32767, adapter.adjust(32762));
        Assert.assertEquals(32768, adapter.adjust(32763));
        Assert.assertEquals(32769, adapter.adjust(32764));
        Assert.assertEquals(32770, adapter.adjust(32765));
        Assert.assertEquals(32771, adapter.adjust(32766));
        Assert.assertEquals(32767, adapter.adjust(32767));
        Assert.assertEquals(32768, adapter.adjust(32768));
        Assert.assertEquals(32769, adapter.adjust(32769));
        Assert.assertEquals(32770, adapter.adjust(32770));
        Assert.assertEquals(32771, adapter.adjust(32771));
        Assert.assertEquals(32767, adapter.adjust(32772));
        Assert.assertEquals(32768, adapter.adjust(32773));
        Assert.assertEquals(32769, adapter.adjust(32774));
        Assert.assertEquals(32770, adapter.adjust(32775));
        Assert.assertEquals(32771, adapter.adjust(32776));
    }

    @Test
    public void toRealSelection() {
        Assert.assertEquals(-1, adapter.toRealPosition(-1));

        Assert.assertEquals(0, adapter.toRealPosition(32762));
        Assert.assertEquals(1, adapter.toRealPosition(32763));
        Assert.assertEquals(2, adapter.toRealPosition(32764));
        Assert.assertEquals(3, adapter.toRealPosition(32765));
        Assert.assertEquals(4, adapter.toRealPosition(32766));
        Assert.assertEquals(0, adapter.toRealPosition(32767));
        Assert.assertEquals(1, adapter.toRealPosition(32768));
        Assert.assertEquals(2, adapter.toRealPosition(32769));
        Assert.assertEquals(3, adapter.toRealPosition(32770));
        Assert.assertEquals(4, adapter.toRealPosition(32771));
        Assert.assertEquals(0, adapter.toRealPosition(32772));
        Assert.assertEquals(1, adapter.toRealPosition(32773));
        Assert.assertEquals(2, adapter.toRealPosition(32774));
        Assert.assertEquals(3, adapter.toRealPosition(32775));
        Assert.assertEquals(4, adapter.toRealPosition(32776));
    }
}
