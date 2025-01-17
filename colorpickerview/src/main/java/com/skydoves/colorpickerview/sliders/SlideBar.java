package com.skydoves.colorpickerview.sliders;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewKt;

import com.skydoves.colorpickerview.R;
import com.skydoves.colorpickerview.SizeUtils;

import java.util.ArrayList;
import java.util.List;

public final class SlideBar extends FrameLayout {
    public interface Listener {
        default void onUserStartedDragging() {
        }

        default void onUserStoppedDragging() {
        }

        void onValueChanged(@FloatRange(from = 0f, to = 1f) float value, boolean fromUser);
    }

    private final Paint colorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float selectorPositionIn01;

    private float touchDownX;
    private float touchDownSelectorPositionIn01;

    private Drawable selectorDrawable;
    private final ImageView selector = new ImageView(getContext());

    private int borderSize;
    private int borderColor;
    private boolean roundCorners;

    private int[] bgColors;
    private boolean bgDirty;

    private int trackColor;
    private int trackProgressColor;

    private final Path path = new Path();
    private float[] cornersLeft;
    private float[] cornersRight;

    private Rect insets = new Rect();

    private final List<Rect> systemGestureExclusionRects = new ArrayList<>(1);
    private final Rect systemGestureExclusionRect = new Rect(0, 0, 0, 0);

    private Listener listener;

    public SlideBar(Context context) {
        super(context);
        init(null);
    }

    public SlideBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public SlideBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public SlideBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        final int defaultBorderSize = SizeUtils.dp2Px(getContext(), 1);
        final int defaultSelectorDrawable = R.drawable.brightness_slider_thumb;
        final int defaultBorderColor = Color.BLACK;
        final boolean defaultRoundCorners = true;
        final int defaultInsetTop = SizeUtils.dp2Px(getContext(), 8);
        final int defaultInsetBottom = SizeUtils.dp2Px(getContext(), 8);
        final int defaultInsetLeft = SizeUtils.dp2Px(getContext(), 8);
        final int defaultInsetRight = SizeUtils.dp2Px(getContext(), 8);

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SlideBar);
            try {
                selectorDrawable = AppCompatResources.getDrawable(getContext(), a.getResourceId(R.styleable.SlideBar_selector, defaultSelectorDrawable));
                borderColor = a.getColor(R.styleable.SlideBar_borderColor, defaultBorderColor);
                borderSize = a.getDimensionPixelSize(R.styleable.SlideBar_borderSize, defaultBorderSize);
                roundCorners = a.getBoolean(R.styleable.SlideBar_roundCorners, defaultRoundCorners);
                int insetTop = a.getDimensionPixelSize(R.styleable.SlideBar_bgInsetTop, defaultInsetTop);
                int insetBottom = a.getDimensionPixelSize(R.styleable.SlideBar_bgInsetBottom, defaultInsetBottom);
                int insetLeft = a.getDimensionPixelSize(R.styleable.SlideBar_bgInsetLeft, defaultInsetLeft);
                int insetRight = a.getDimensionPixelSize(R.styleable.SlideBar_bgInsetRight, defaultInsetRight);
                insets = new Rect(insetLeft, insetTop, insetRight, insetBottom);
            } finally {
                a.recycle();
            }
        } else {
            selectorDrawable = AppCompatResources.getDrawable(getContext(), defaultSelectorDrawable);
            borderColor = defaultBorderColor;
            borderSize = defaultBorderSize;
            roundCorners = defaultRoundCorners;
            insets = new Rect(defaultInsetLeft, defaultInsetTop, defaultInsetRight, defaultInsetBottom);
        }

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderSize);
        borderPaint.setColor(borderColor);

        setWillNotDraw(false);

        setSelectorDrawable(selectorDrawable);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        selector.setVisibility(enabled ? VISIBLE : INVISIBLE);
        setClickable(enabled);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public Listener getListener() {
        return listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isInEditMode()) {
            return;
        }

        float width = getWidth();
        float height = getHeight();

        float left = insets.left;
        float top = insets.top;
        float right = width - insets.right;
        float bottom = height - insets.bottom;

        if (bgColors != null) {
            if (bgDirty) {
                Shader shader = new LinearGradient(0, 0, getWidth(), getHeight(), bgColors, null, Shader.TileMode.CLAMP);
                colorPaint.setShader(shader);
                bgDirty = false;
            }

            if (roundCorners) {
                float radius = (bottom - top) / 2f;
                canvas.drawRoundRect(left, top, right, bottom, radius, radius, colorPaint);
                canvas.drawRoundRect(left, top, right, bottom, radius, radius, borderPaint);
            } else {
                canvas.drawRect(left, top, right, bottom, colorPaint);
                canvas.drawRect(left, top, right, bottom, borderPaint);
            }

        } else {
            int progressEnd = (int) (selector.getX() + selector.getWidth() / 2f);

            if (roundCorners) {
                float radius = (bottom - top) / 2f;

                if (bgDirty) {
                    cornersLeft = new float[]{
                            radius, radius, // Top, left in px
                            0, 0, // Top, right in px
                            0, 0, // Bottom, right in px
                            radius, radius // Bottom,left in px
                    };
                    cornersRight = new float[]{
                            0, 0, // Top, left in px
                            radius, radius, // Top, right in px
                            radius, radius, // Bottom, right in px
                            0, 0 // Bottom,left in px
                    };
                    bgDirty = false;
                }

                path.reset();
                path.addRoundRect(left, top, progressEnd, bottom, cornersLeft, Path.Direction.CW);
                colorPaint.setColor(trackProgressColor);
                canvas.drawPath(path, colorPaint);

                path.reset();
                path.addRoundRect(progressEnd, top, right, bottom, cornersRight, Path.Direction.CW);
                colorPaint.setColor(trackColor);
                canvas.drawPath(path, colorPaint);

                canvas.drawRoundRect(left, top, right, bottom, radius, radius, borderPaint);

            } else {
                colorPaint.setColor(trackProgressColor);
                canvas.drawRect(left, top, progressEnd, bottom, colorPaint);

                colorPaint.setColor(trackColor);
                canvas.drawRect(progressEnd, top, right, bottom, colorPaint);

                canvas.drawRect(left, top, right, bottom, borderPaint);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            systemGestureExclusionRect.set(0, 0, getWidth(), getHeight());
            systemGestureExclusionRects.clear();
            systemGestureExclusionRects.add(systemGestureExclusionRect);
            setSystemGestureExclusionRects(systemGestureExclusionRects);
        }
    }

    public void setBgColors(int[] colors) {
        bgColors = colors;
        bgDirty = true;
        invalidate();
    }

    public void setBgColorsWithTrackProgress(@ColorInt int trackProgressColor, @ColorInt int trackColor) {
        bgColors = null;
        bgDirty = true;
        this.trackColor = trackColor;
        this.trackProgressColor = trackProgressColor;
        invalidate();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        int actionMasked = event.getActionMasked();
        if (actionMasked == MotionEvent.ACTION_UP || actionMasked == MotionEvent.ACTION_DOWN || actionMasked == MotionEvent.ACTION_MOVE) {
            selector.setPressed(true);
            onTouchReceived(event);
            return true;
        }
        selector.setPressed(false);
        return false;
    }

    private void onTouchReceived(MotionEvent e) {
        float x = e.getX();

        int action = e.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            if (listener != null) {
                listener.onUserStartedDragging();
            }
            touchDownX = x;
            touchDownSelectorPositionIn01 = selectorPositionIn01;

        } else if (action == MotionEvent.ACTION_MOVE) {
            updateValue(x);

        } else if (action == MotionEvent.ACTION_UP) {
            if (listener != null) {
                listener.onUserStoppedDragging();
            }
        }
    }

    private void updateValue(float touchX) {
        float delta = touchX - touchDownX;
        float w = getAvailableWidth();

        float newVal = touchDownSelectorPositionIn01 + delta / w;
        newVal = Math.min(Math.max(newVal, 0f), 1f);
        selectorPositionIn01 = newVal;

        float selectorX = w * selectorPositionIn01;
        selector.setX(selectorX);
        if (bgColors == null) {
            invalidate();
        }
        fireListener(selectorPositionIn01, true);
    }

    private float getAvailableWidth() {
        return getWidth() - getSelectorSize();
    }

    public void setSelectorPosition(@FloatRange(from = 0.0, to = 1.0) float selectorPosition) {
        this.selectorPositionIn01 = Math.min(Math.max(selectorPosition, 0f), 1f);
        ViewKt.doOnLayout(this, view -> {
            selector.setX(getAvailableWidth() * this.selectorPositionIn01);
            if (bgColors == null) {
                invalidate();
            }
            return null;
        });
        fireListener(selectorPosition, false);
    }

    private int getSelectorSize() {
        return selector.getWidth();
    }

    /**
     * sets a drawable of the selector.
     *
     * @param drawable drawable of the selector.
     */
    public void setSelectorDrawable(Drawable drawable) {
        removeView(selector);
        this.selectorDrawable = drawable;
        this.selector.setImageDrawable(drawable);
        FrameLayout.LayoutParams thumbParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        thumbParams.gravity = Gravity.CENTER_VERTICAL;
        addView(selector, thumbParams);
    }

    /**
     * sets a drawable resource of the selector.
     *
     * @param resource a drawable resource of the selector.
     */
    public void setSelectorDrawableRes(@DrawableRes int resource) {
        Drawable drawable = ResourcesCompat.getDrawable(getContext().getResources(), resource, null);
        setSelectorDrawable(drawable);
    }

    /**
     * sets a color of the slider border.
     *
     * @param color color of the slider border.
     */
    public void setBorderColor(@ColorInt int color) {
        this.borderColor = color;
        this.borderPaint.setColor(color);
        invalidate();
    }

    /**
     * sets a size of the slide border.
     *
     * @param borderSize ize of the slide border.
     */
    public void setBorderSize(int borderSize) {
        this.borderSize = borderSize;
        this.borderPaint.setStrokeWidth(borderSize);
        invalidate();
    }

    /**
     * sets a size of the slide border using dimension resource.
     *
     * @param resource a size of the slide border.
     */
    public void setBorderSizeRes(@DimenRes int resource) {
        int borderSize = (int) getContext().getResources().getDimension(resource);
        setBorderSize(borderSize);
    }

    private void fireListener(@FloatRange(from = 0f, to = 1f) float selectorPosition, boolean fromUser) {
        if (listener != null) {
            listener.onValueChanged(selectorPosition, fromUser);
        }
    }

    /**
     * gets selector's position ratio.
     *
     * @return selector's position ratio.
     */
    @FloatRange(from = 0f, to = 1f)
    public float getSelectorPosition() {
        return this.selectorPositionIn01;
    }

    public void setRoundCorners(boolean roundCorners) {
        this.roundCorners = roundCorners;
        invalidate();
    }

    public boolean isRoundCorners() {
        return roundCorners;
    }

    public void setInsets(@NonNull Rect insets) {
        this.insets = insets;
    }

    public Rect getInsets() {
        return insets;
    }
}
