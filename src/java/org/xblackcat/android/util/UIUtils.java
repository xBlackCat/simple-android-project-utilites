package org.xblackcat.android.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.Shape;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ViewSwitcher;
import org.xblackcat.android.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 27.09.13 10:42
 *
 * @author xBlackCat
 */
public class UIUtils {
    public static final Pattern LINKS_PATTERN = Pattern.compile("http://\\S+[\\w/]");

    public static int dpToPx(Context context, int pixelsValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) (pixelsValue * metrics.density);
    }

    public static int spToPx(Context context, int pixelsValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) (pixelsValue * metrics.density * metrics.scaledDensity);
    }

    public static LinearLayout buildWaitingComponent(Context ctx) {
        LinearLayout waitingContainer = new LinearLayout(ctx);
        waitingContainer.setGravity(Gravity.CENTER);

        ProgressBar waiting = new ProgressBar(ctx);
        waiting.setIndeterminate(true);
        waitingContainer.addView(
                waiting,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );
        return waitingContainer;
    }

    public static void setLinks(Spannable spannable, final UrlClickableSpan onUrlClick) {
        List<LinkSpec> links = new ArrayList<>();
        Matcher m = LINKS_PATTERN.matcher(spannable);

        while (m.find()) {
            int start = m.start();
            int end = m.end();

            if (start == 0 || spannable.charAt(start - 1) != '@') {
                links.add(new LinkSpec(m.group(0), start, end));
            }
        }

        for (LinkSpec link : links) {
            final String url = link.url;
            spannable.setSpan(
                    new ClickableSpan() {
                        @Override
                        public void onClick(View widget) {
                            onUrlClick.onUrlClick(widget, url);
                        }
                    },
                    link.start,
                    link.end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
    }

    /**
     * Scale bitmap to grow/shrink lower size to specified size.
     *
     * @param bitmap    source bitmap
     * @param sizeBound target size for lowest side
     * @return scaled bitmap
     */
    public static Bitmap scaleBitmap(Bitmap bitmap, int sizeBound) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        // Thumbnail is square
        float ratio = (float) height / width;
        int h;
        int w;
        if (ratio > 1) {
            w = sizeBound;
            h = (int) (sizeBound * ratio);
        } else {
            h = sizeBound;
            w = (int) (sizeBound / ratio);
        }

        return Bitmap.createScaledBitmap(
                bitmap,
                w,
                h,
                false
        );
    }

    public static View buildProgressView(Context context, ProgressBarStyle progressBarStyle) {
        ProgressBar progressBar = buildWaitingProgressBar(context, progressBarStyle);

        RelativeLayout prFr = new RelativeLayout(context);
        prFr.setGravity(Gravity.CENTER);
        prFr.addView(
                progressBar,
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );
        return prFr;
    }

    public static ProgressBar buildWaitingProgressBar(Context context) {
        return buildWaitingProgressBar(context, null);
    }

    public static ProgressBar buildWaitingProgressBar(Context context, ProgressBarStyle progressBarStyle) {
        ProgressBar progressBar = new ProgressBar(
                context,
                null,
                progressBarStyle == null ? 0 : progressBarStyle.styleResId
        );
        progressBar.setIndeterminate(true);
        progressBar.setBackgroundDrawable(null);
        return progressBar;
    }

    public static String getDensityString(Context ctx) {
        return getDensity(ctx).getAbbr();
    }

    public static Density findNearestDensity(Set<Density> set, Density targetDensity) {
        int deltaOffset = 1;
        int offset = targetDensity.ordinal();
        Density[] values = Density.values();
        int delta = offset == values.length - 1 ? -1 : 1;

        do {
            if (set.contains(values[offset])) {
                return values[offset];
            }

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

        return null;
    }

    public static Density getDensity(Context ctx) {
        return Density.valueOf(ctx.getResources().getDisplayMetrics().densityDpi);
    }

    @SuppressWarnings("unchecked")
    public static <T extends View> T getParent(View view, Class<T> parentClass) {
        ViewParent parent = view.getParent();
        while (parent != null && !parentClass.isAssignableFrom(parent.getClass())) {
            parent = parent.getParent();
        }

        return (T) parent;
    }

    public static ViewSwitcher coverWithProgress(Context activity, View pageView) {
        return coverWithProgress(activity, pageView, buildProgressView(activity, ProgressBarStyle.Large));
    }

    public static ViewSwitcher coverWithProgress(Context activity, View pageView, View loadingView) {
        ViewSwitcher flipper = new ViewSwitcher(activity);

        flipper.addView(
                loadingView,
                new ViewSwitcher.LayoutParams(
                        ViewGroup.LayoutParams.FILL_PARENT,
                        ViewGroup.LayoutParams.FILL_PARENT
                )
        );

        flipper.addView(
                pageView,
                new ViewSwitcher.LayoutParams(
                        ViewGroup.LayoutParams.FILL_PARENT,
                        ViewGroup.LayoutParams.FILL_PARENT
                )
        );

        flipper.setInAnimation(activity, R.anim.gallery_show_item);
        flipper.setOutAnimation(activity, R.anim.gallery_hide_item);
        return flipper;
    }

    public static void showLoader(ViewSwitcher switcher) {
        showLoader(switcher, false);
    }

    public static void showLoader(ViewSwitcher switcher, boolean showAnimation) {
        showChild(switcher, 0, showAnimation);
    }

    public static void showContent(ViewSwitcher switcher) {
        showChild(switcher, 1, true);
    }

    public static void showContent(ViewSwitcher switcher, boolean showAnimation) {
        showChild(switcher, 1, showAnimation);
    }

    public static void showChild(ViewSwitcher switcher, int childIdx, boolean showAnimation) {
        if (switcher.getDisplayedChild() == childIdx) {
            return;
        }

        if (showAnimation) {
            switcher.setDisplayedChild(childIdx);
        } else {
            Animation in = switcher.getInAnimation();
            Animation out = switcher.getOutAnimation();
            switcher.setInAnimation(null);
            switcher.setOutAnimation(null);

            switcher.setDisplayedChild(childIdx);

            switcher.setInAnimation(in);
            switcher.setOutAnimation(out);

        }
    }

    public static Drawable buildBorder(int color) {
        ShapeDrawable border = new ShapeDrawable();
        Shape rectShape = new RectShape();
        border.setShape(rectShape);
        border.getPaint().setStrokeWidth(1);
        border.getPaint().setStyle(Paint.Style.STROKE);
        border.getPaint().setColor(color);

        return border;
    }

    public static class LinkSpec {
        private final String url;
        private final int start;
        private final int end;

        private LinkSpec(String url, int start, int end) {
            this.url = url;
            this.start = start;
            this.end = end;
        }
    }
}
