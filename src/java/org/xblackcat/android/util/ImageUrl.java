package org.xblackcat.android.util;

import java.io.Serializable;

/**
 * 27.09.13 17:50
 *
 * @author xBlackCat
 */
public class ImageUrl implements Serializable {
    private static final int serialVersionUID = 1;

    private final String url;
    private final Density density;

    public ImageUrl(String url, Density density) {
        this.url = url;
        this.density = density;
    }

    public ImageUrl(String url) {
        this(url, null);
    }

    public String getUrl() {
        return url;
    }

    public Density getDensity() {
        return density;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImageUrl imageUrl = (ImageUrl) o;

        if (density != imageUrl.density) return false;
        if (!url.equals(imageUrl.url)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = url.hashCode();
        result = 31 * result + (density != null ? density.hashCode() : 0);
        return result;
    }
}
