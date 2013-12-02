package org.xblackcat.android.service.image;

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

    @SuppressWarnings({"SimplifiableIfStatement", "RedundantIfStatement"})
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ImageUrl)) {
            return false;
        }

        ImageUrl imageUrl = (ImageUrl) o;

        if (density != imageUrl.density) {
            return false;
        }

        if (url != null ? !url.equals(imageUrl.url) : imageUrl.url != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = url != null ? url.hashCode() : 0;
        result = 31 * result + (density != null ? density.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Image [url='" + url + "' " + density + ']';
    }
}
