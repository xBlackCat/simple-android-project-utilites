package org.xblackcat.android.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.EnumSet;

/**
 * 27.09.13 16:42
 *
 * @author xBlackCat
 */
public class TestDensity {
    @Test
    public void densityFinder() {
        Assert.assertEquals(
                Density.XXHigh,
                UIUtils.findNearestDensity(
                        EnumSet.of(Density.XXHigh, Density.XXXHigh),
                        Density.High
                )
        );

        Assert.assertEquals(
                Density.Medium,
                UIUtils.findNearestDensity(
                        EnumSet.of(Density.Medium, Density.XXXHigh),
                        Density.High
                )
        );

        Assert.assertEquals(
                Density.High,
                UIUtils.findNearestDensity(
                        EnumSet.allOf(Density.class),
                        Density.High
                )
        );

        Assert.assertEquals(
                Density.XXHigh,
                UIUtils.findNearestDensity(
                        EnumSet.of(Density.XXHigh, Density.XXXHigh),
                        Density.Low
                )
        );

    }

    @Test
    public void walkTest() {
        walkingOnArray(
                new Density[]{Density.High, Density.XHigh, Density.TV, Density.XXHigh, Density.Medium, Density.XXXHigh, Density.Low, Density.Unknown},
                Density.High
        );
        walkingOnArray(
                new Density[]{Density.Unknown, Density.Low, Density.Medium, Density.TV, Density.High, Density.XHigh, Density.XXHigh, Density.XXXHigh},
                Density.Unknown
        );
        walkingOnArray(
                new Density[]{Density.XXXHigh, Density.XXHigh, Density.XHigh, Density.High, Density.TV, Density.Medium, Density.Low, Density.Unknown},
                Density.XXXHigh
        );
        walkingOnArray(
                new Density[]{Density.XHigh, Density.XXHigh, Density.High, Density.XXXHigh, Density.TV, Density.Medium, Density.Low, Density.Unknown},
                Density.XHigh
        );
    }

    public void walkingOnArray(Density[] order, Density start) {
        System.out.println("Walk from " + start);

        int idx = 0;

        int deltaOffset = 1;
        int offset = start.ordinal();
        Density[] values = Density.values();
        int delta = offset == values.length - 1 ? -1 : 1;

        do {
            Assert.assertEquals(order[idx++], values[offset]);
            System.out.println(values[offset]);
            offset += delta;
            deltaOffset++;

            if (delta > 0) {
                delta = -deltaOffset;

                if (offset + delta < 0) {
                    delta = 1;
                }
            } else {
                delta = deltaOffset;

                if (offset + delta >= values.length) {
                    delta = -1;
                }
            }
        } while (deltaOffset <= values.length);

    }


}
