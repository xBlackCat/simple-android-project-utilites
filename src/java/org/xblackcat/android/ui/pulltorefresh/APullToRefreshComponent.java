package org.xblackcat.android.ui.pulltorefresh;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import org.xblackcat.android.R;

/**
 * 31.10.12 14:26
 *
 * @author xBlackCat
 */
public abstract class APullToRefreshComponent<C extends View> extends LinearLayout {
    protected static final int PULL_TO_REFRESH = 0;
    protected static final int RELEASE_TO_REFRESH = PULL_TO_REFRESH + 1;
    protected static final int REFRESHING = RELEASE_TO_REFRESH + 1;
    protected static final int EVENT_COUNT = 3;

    protected int state = PULL_TO_REFRESH;
    protected C view;
    protected int loadingText = R.string.pull_to_refresh_refreshing_label;
    protected int pullToRefreshText = R.string.pull_to_refresh_pull_label;
    protected int releaseToRefreshText = R.string.pull_to_refresh_release_label;
    private RelativeLayout header;
    private TextView headerTextUpdated;
    private TextView headerText;
    private ImageView headerImage;
    private Animation flipAnimation;
    private Animation reverseAnimation;
    private int headerHeight;
    private float startY = -1;
    private Handler handler = new Handler();
    private OnTouchListener onTouchListener;
    private OnRefreshListener onRefreshListener;
    private float[] lastYs = new float[EVENT_COUNT];
    private boolean canPullDownToRefresh = true;
    private OnRefreshStateListener onRefreshStateListener;
    private OnTouchListener listViewOnTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View arg0, MotionEvent arg1) {
            return onListViewTouch(arg0, arg1);
        }

    };
    private Runnable hideHeaderRunnable = new Runnable() {

        @Override
        public void run() {
            hideHeader();
        }

    };


    public APullToRefreshComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void onRefreshComplete() {
        resetHeader();

        if (onRefreshStateListener != null) {
            onRefreshStateListener.onRefreshComplete(this);
        }
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        onRefreshListener = listener;
    }

    public void setRefreshing() {
        state = REFRESHING;

        int topMargin = getHeaderScroll();
        if (topMargin != 0) {
            setHeaderScroll(topMargin - headerHeight);
        }

        header.setVisibility(View.INVISIBLE);

        setRefreshingImpl();

        if (onRefreshStateListener != null) {
            onRefreshStateListener.onRefreshStarted(this);
        }
    }

    protected abstract void setRefreshingImpl();

    public boolean isListViewShown() {
        return view.isShown();
    }

    @Override
    public void setOnTouchListener(OnTouchListener listener) {
        onTouchListener = listener;
    }

    public int getPullToRefreshText() {
        return pullToRefreshText;
    }

    public void setPullToRefreshText(int pullToRefreshText) {
        this.pullToRefreshText = pullToRefreshText;
    }

    public int getLoadingText() {
        return loadingText;
    }

    public void setLoadingText(int loadingText) {
        this.loadingText = loadingText;
    }

    public int getReleaseToRefreshText() {
        return releaseToRefreshText;
    }

    public void setReleaseToRefreshText(int releaseToRefreshText) {
        this.releaseToRefreshText = releaseToRefreshText;
    }

    protected void init(Context context, AttributeSet attrs) {
        setOrientation(LinearLayout.VERTICAL);

        header = (RelativeLayout) LayoutInflater.from(context).inflate(
                R.layout.pull_to_refresh_header, this, false
        );

        headerTextUpdated = (TextView) header.findViewById(R.id.pull_to_refresh_updated_at);
        headerText = (TextView) header.findViewById(R.id.pull_to_refresh_text);
        headerImage = (ImageView) header.findViewById(R.id.pull_to_refresh_image);

        LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);

        addView(header, lp);

        measureView(header);
        headerHeight = header.getMeasuredHeight();

        // ListView

        view = buildComponent(context, attrs);
        view.setId(View.NO_ID);
        view.setOnTouchListener(listViewOnTouchListener);

        lp = new LayoutParams(
                LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT
        );

        addView(view, lp);

        flipAnimation = new RotateAnimation(
                0, -180,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f
        );
        flipAnimation.setInterpolator(new LinearInterpolator());
        flipAnimation.setDuration(250);
        flipAnimation.setFillAfter(true);
        reverseAnimation = new RotateAnimation(
                -180, 0,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f
        );
        reverseAnimation.setInterpolator(new LinearInterpolator());
        reverseAnimation.setDuration(250);
        reverseAnimation.setFillAfter(true);

        setPadding(
                getPaddingLeft(), -headerHeight, getPaddingRight(),
                getPaddingBottom()
        );
    }

    protected abstract C buildComponent(Context context, AttributeSet attrs);

    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(
                    lpHeight,
                    MeasureSpec.EXACTLY
            );
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(
                    0,
                    MeasureSpec.UNSPECIFIED
            );
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    private boolean onListViewTouch(View view, MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                updateEventStates(event);

                if (isPullingDownToRefresh() && startY == -1) {
                    startY = event.getY();
                    return false;
                }

                if (startY != -1 && !this.view.isPressed()) {
                    pullDown(event, startY);
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                initializeYsHistory();
                startY = -1;

                if (state == RELEASE_TO_REFRESH) {
                    setRefreshing();
                    if (onRefreshListener != null) {
                        onRefreshListener.onRefresh();
                    }
                }

                ensureHeaderPosition();
                break;
        }

        if (onTouchListener != null) {
            return onTouchListener.onTouch(view, event);
        }
        return false;
    }

    protected void resetHeader() {
        state = PULL_TO_REFRESH;
        initializeYsHistory();
        startY = -1;
        header.setVisibility(View.VISIBLE);
        headerText.setText(pullToRefreshText);
        headerImage.clearAnimation();

        headerHeight = header.getMeasuredHeight();

        setPadding(
                getPaddingLeft(), -headerHeight, getPaddingRight(),
                getPaddingBottom()
        );

        setHeaderScroll(0);

        resetHeaderImpl();
    }

    protected abstract void resetHeaderImpl();

    public void setUpdatedAt(CharSequence info) {
        if (info == null || info.length() == 0) {
            headerTextUpdated.setText(null);
            headerTextUpdated.setVisibility(INVISIBLE);
        } else {
            headerTextUpdated.setText(info);
            headerTextUpdated.setVisibility(VISIBLE);
        }
    }

    private void pullDown(MotionEvent event, float firstY) {
        float averageY = average(lastYs);

        int height = (int) (Math.max(averageY - firstY, 0));

        setHeaderScroll(height);

        if (state == PULL_TO_REFRESH && height - headerHeight > 0) {
            state = RELEASE_TO_REFRESH;
            headerText.setText(releaseToRefreshText);
            headerImage.clearAnimation();
            headerImage.startAnimation(flipAnimation);
        }
        if (state == RELEASE_TO_REFRESH && height - headerHeight <= 0) {
            state = PULL_TO_REFRESH;
            headerText.setText(pullToRefreshText);
            headerImage.clearAnimation();
            headerImage.startAnimation(reverseAnimation);
        }
    }

    private void setHeaderScroll(int y) {
        if (headerHeight < y) {
            y = headerHeight;
        }
        scrollTo(0, -y);
    }

    private int getHeaderScroll() {
        return -getScrollY();
    }

    private float average(float[] ysArray) {
        float avg = 0;
        for (int i = 0; i < EVENT_COUNT; i++) {
            avg += ysArray[i];
        }
        return avg / EVENT_COUNT;
    }

    private void initializeYsHistory() {
        for (int i = 0; i < EVENT_COUNT; i++) {
            lastYs[i] = 0;
        }
    }

    private void updateEventStates(MotionEvent event) {
        System.arraycopy(lastYs, 1, lastYs, 0, EVENT_COUNT - 1);

        float y = event.getY();
        int top = view.getTop();
        lastYs[EVENT_COUNT - 1] = y + top;
    }

    private boolean isPullingDownToRefresh() {
        return canPullDownToRefresh && state != REFRESHING && isIncremental()
                && isAbleToPushToRefresh();
    }

    protected abstract boolean isAbleToPushToRefresh();

    private boolean isIncremental() {
        return this.isIncremental(0, EVENT_COUNT - 1);
    }

    private boolean isIncremental(int from, int to) {
        return lastYs[from] != 0 && lastYs[to] != 0
                && Math.abs(lastYs[from] - lastYs[to]) > 10
                && lastYs[from] < lastYs[to];
    }

    private void ensureHeaderPosition() {
        handler.post(hideHeaderRunnable);
    }

    private void hideHeader() {
        int padding = getHeaderScroll();
        if (padding != 0) {
            int top = padding - padding / 2;
            if (top < 2) {
                top = 0;
            }

            setHeaderScroll(top);

            handler.postDelayed(hideHeaderRunnable, 20);
        }
    }

    public C getView() {
        return view;
    }

    public void onDestroy() {
        view = null;
        onRefreshListener = null;
    }

    public OnRefreshStateListener getOnRefreshStateListener() {
        return onRefreshStateListener;
    }

    public void setOnRefreshStateListener(OnRefreshStateListener onRefreshStateListener) {
        this.onRefreshStateListener = onRefreshStateListener;
    }

    public interface OnRefreshListener {

        public void onRefresh();

    }

}
