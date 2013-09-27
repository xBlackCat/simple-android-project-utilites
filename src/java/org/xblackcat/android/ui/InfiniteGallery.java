package org.xblackcat.android.ui;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.SpinnerAdapter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 20.09.13 14:03
 *
 * @author xBlackCat
 */
public class InfiniteGallery extends Gallery {
    private boolean internalEvent = false;
    private Method moveNext;
    private boolean canUseHack = true;

    public InfiniteGallery(Context context) {
        super(context);

        setOnItemSelectedListener(new SelectionCorrector());
    }

    public InfiniteGallery(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOnItemSelectedListener(null);
    }

    public InfiniteGallery(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setOnItemSelectedListener(null);
    }

    @Override
    public void setAdapter(SpinnerAdapter adapter) {
        super.setAdapter(new InfiniteSpinnerAdapter(adapter));
    }

    @Override
    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        super.setOnItemSelectedListener(new SelectionCorrector(listener));
    }

    @Override
    public void setOnItemClickListener(OnItemClickListener listener) {
        super.setOnItemClickListener(new OnItemClickCorrector(listener));
    }

    @Override
    public void setSelection(int position, boolean animate) {
        super.setSelection(((InfiniteSpinnerAdapter) getAdapter()).adjust(position), animate);
    }

    @Override
    public void setSelection(int position) {
        super.setSelection(((InfiniteSpinnerAdapter) getAdapter()).adjust(position));
    }


    public int getRealSelectedItemPosition() {
        final int position = super.getSelectedItemPosition();

        if (position >= 0) {
            return ((InfiniteSpinnerAdapter) getAdapter()).toRealPosition(position);
        } else {
            return -1;
        }
    }

    public void moveNext() {
        if (canUseHack) {
            try {
                if (moveNext == null) {
                    moveNext = Gallery.class.getDeclaredMethod("moveNext");
                    if (!moveNext.isAccessible()) {
                        moveNext.setAccessible(true);
                    }
                }
                moveNext.invoke(this);
                return;
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                Log.w("Infinite gallery", "Workaround is not working :(", e);
                canUseHack = false;
            }
        }

        internalEvent = true;
        onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT));
        internalEvent = false;
    }

    @Override
    public void playSoundEffect(int soundConstant) {
        if (internalEvent) {
            // Mute...
            if (soundConstant == SoundEffectConstants.NAVIGATION_LEFT ||
                    soundConstant == SoundEffectConstants.NAVIGATION_RIGHT) {
                return;
            }
        }

        super.playSoundEffect(soundConstant);
    }

    public void setRealSelection(int pos) {
        super.setSelection(((InfiniteSpinnerAdapter) getAdapter()).toInfinitePosition(pos));
    }

    private static class OnItemClickCorrector implements OnItemClickListener {
        private final OnItemClickListener listener;

        public OnItemClickCorrector(OnItemClickListener listener) {
            this.listener = listener;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final InfiniteSpinnerAdapter adapter = (InfiniteSpinnerAdapter) parent.getAdapter();

            if (listener != null) {
                listener.onItemClick(parent, view, adapter.toRealPosition(position), id);
            }
        }
    }

    private class SelectionCorrector implements OnItemSelectedListener {
        private final OnItemSelectedListener listener;

        private SelectionCorrector(OnItemSelectedListener listener) {
            this.listener = listener;
        }

        public SelectionCorrector() {
            this(null);
        }

        @Override
        public void onItemSelected(
                AdapterView<?> parent,
                View view,
                int position,
                long id
        ) {
            final InfiniteSpinnerAdapter adapter = (InfiniteSpinnerAdapter) getAdapter();

            if (!adapter.isPositionInBound(position)) {
                setSelection(position);
            } else if (listener != null) {
                final int pos = adapter.toRealPosition(position);
                listener.onItemSelected(parent, view, pos, id);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            if (listener != null) {
                listener.onNothingSelected(parent);
            }
        }
    }

    /**
     * 20.09.13 13:50
     *
     * @author xBlackCat
     */
    protected static class InfiniteSpinnerAdapter implements SpinnerAdapter {
        private final int amount;
        private final int shift;

        private final SpinnerAdapter adapter;

        protected InfiniteSpinnerAdapter(SpinnerAdapter adapter) {
            this(adapter, 50);
        }

        protected InfiniteSpinnerAdapter(SpinnerAdapter adapter, int shift) {
            this(adapter, shift, (shift << 1) + shift + adapter.getCount());
        }

        protected InfiniteSpinnerAdapter(SpinnerAdapter adapter, int shift, int amount) {
            this.adapter = adapter;
            this.shift = shift;
            this.amount = amount;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return adapter.getDropDownView(toRealPosition(position), convertView, parent);
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
            adapter.registerDataSetObserver(observer);
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            adapter.unregisterDataSetObserver(observer);
        }

        @Override
        public int getCount() {
            return amount;
        }

        protected int toInfinitePosition(int pos) throws IndexOutOfBoundsException {
            if (pos < 0 || pos >= adapter.getCount()) {
                throw new IndexOutOfBoundsException("Invalid position: " + pos + ". Size: " + adapter.getCount());
            }

            return shift + pos;
        }

        protected boolean isPositionInBound(int pos) {
            final int amount = adapter.getCount();

            return pos >= shift && pos < (shift + amount);
        }

        protected int adjust(int position) {
            final int amount = adapter.getCount();

            if (position >= shift && position < (shift + amount)) {
                return position;
            }

            int realPos = (position % amount) - (shift % amount);
            if (realPos < 0) {
                realPos += amount;
            }

            return realPos + shift;
        }

        protected int toRealPosition(int pos) {
            if (pos == -1) {
                return -1;
            } else {

                final int realAmount = adapter.getCount();

                int realPos = (pos % realAmount) - (shift % realAmount);
                if (realPos < 0) {
                    realPos += realAmount;
                }

                assert realPos >= 0 && realPos < realAmount;

                return realPos;
            }
        }

        @Override
        public Object getItem(int position) {
            return adapter.getItem(toRealPosition(position));
        }

        @Override
        public long getItemId(int position) {
            return adapter.getItemId(toRealPosition(position));
        }

        @Override
        public boolean hasStableIds() {
            return adapter.hasStableIds();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return adapter.getView(toRealPosition(position), convertView, parent);
        }

        @Override
        public int getItemViewType(int position) {
            return adapter.getItemViewType(toRealPosition(position));
        }

        @Override
        public int getViewTypeCount() {
            return adapter.getViewTypeCount();
        }

        @Override
        public boolean isEmpty() {
            return adapter.isEmpty();
        }
    }
}
