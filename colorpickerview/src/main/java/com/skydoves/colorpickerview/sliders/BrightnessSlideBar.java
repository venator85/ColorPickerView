/*
 * Designed and developed by 2017 skydoves (Jaewoong Eum)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.skydoves.colorpickerview.sliders;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.ColorUtils;
import com.skydoves.colorpickerview.R;
import com.skydoves.colorpickerview.listeners.BrightnessListener;

public final class BrightnessSlideBar extends FrameLayout {
	private Paint colorPaint;
	private Paint borderPaint;
	private float selectorPosition = 0.5f;
	private Drawable selectorDrawable;
	private int borderSize = 2;
	private int borderColor = Color.BLACK;
	private int color = Color.WHITE;
	private ImageView selector;
	private boolean roundCorners;

	private Rect insets = new Rect();

	private BrightnessListener brightnessListener;

	public BrightnessSlideBar(Context context) {
		super(context);
		onCreate();
	}

	public BrightnessSlideBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		getAttrs(attrs);
		onCreate();
	}

	public BrightnessSlideBar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		getAttrs(attrs);
		onCreate();
	}

	public BrightnessSlideBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		getAttrs(attrs);
		onCreate();
	}

	private void getAttrs(AttributeSet attrs) {
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.BrightnessSlideBar);
		try {
			if (a.hasValue(R.styleable.BrightnessSlideBar_selector_BrightnessSlider)) {
				int resourceId = a.getResourceId(R.styleable.BrightnessSlideBar_selector_BrightnessSlider, -1);
				if (resourceId != -1) {
					selectorDrawable = AppCompatResources.getDrawable(getContext(), resourceId);
				}
			}
			if (a.hasValue(R.styleable.BrightnessSlideBar_borderColor_BrightnessSlider)) {
				borderColor = a.getColor(R.styleable.BrightnessSlideBar_borderColor_BrightnessSlider, borderColor);
			}
			if (a.hasValue(R.styleable.BrightnessSlideBar_borderSize_BrightnessSlider)) {
				borderSize = a.getDimensionPixelSize(R.styleable.BrightnessSlideBar_borderSize_BrightnessSlider, borderSize);
			}
		} finally {
			a.recycle();
		}
	}

	private void onCreate() {
		this.colorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.borderPaint.setStyle(Paint.Style.STROKE);
		this.borderPaint.setStrokeWidth(borderSize);
		this.borderPaint.setColor(borderColor);
		setWillNotDraw(false);

		selector = new ImageView(getContext());
		if (selectorDrawable != null) {
			setSelectorDrawable(selectorDrawable);
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		selector.setVisibility(enabled ? VISIBLE : INVISIBLE);
		this.setClickable(enabled);
	}

	public void setBrightnessListener(BrightnessListener brightnessListener) {
		this.brightnessListener = brightnessListener;
	}

	public BrightnessListener getBrightnessListener() {
		return brightnessListener;
	}

	/**
	 * update paint color whenever the triggered colors are changed.
	 */
	private void updatePaint(Paint colorPaint) {
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] = 0;
		int startColor = Color.HSVToColor(hsv);
		hsv[2] = 1;
		int endColor = Color.HSVToColor(hsv);
		Shader shader = new LinearGradient(0, 0, getWidth(), getHeight(), startColor, endColor, Shader.TileMode.CLAMP);
		colorPaint.setShader(shader);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		float width = getWidth();
		float height = getHeight();

		float left = insets.left;
		float top = insets.top;
		float right = width - insets.right;
		float bottom = height - insets.bottom;

		if (roundCorners) {
			float radius = (bottom - top) / 2f;
			canvas.drawRoundRect(left, top, right, bottom, radius, radius, colorPaint);
			canvas.drawRoundRect(left, top, right, bottom, radius, radius, borderPaint);
		} else {
			canvas.drawRect(left, top, right, bottom, colorPaint);
			canvas.drawRect(left, top, right, bottom, borderPaint);
		}
	}

	/**
	 * called by {@link ColorPickerView} whenever {@link ColorPickerView} is triggered.
	 */
	public void setColor(int color) {
		this.color = ColorUtils.getPureColor(color);
		updatePaint(colorPaint);
		invalidate();
	}

	@ColorInt
	public int getColor() {
		return color;
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!this.isEnabled()) {
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
			if (brightnessListener != null) {
				brightnessListener.onUserStartedDragging();
			}

			updateValue(x);

		} else if (action == MotionEvent.ACTION_MOVE) {
			updateValue(x);

		} else if (action == MotionEvent.ACTION_UP) {
			updateValue(x);

			if (brightnessListener != null) {
				brightnessListener.onUserStoppedDragging();
			}
		}
	}

	private void updateValue(float x) {
		selectorPosition = computeSelectorPositionFromTouch(x);
		selector.setX(getAvailableWidth() * selectorPosition);
		fireListener(selectorPosition, true);
	}

	private float computeSelectorPositionFromTouch(float x) {
		float w = getAvailableWidth();
		float newVal;
		if (x <= 0) {
			newVal = 0f;
		} else if (x > w) {
			newVal = 1f;
		} else {
			newVal = x / w;
		}
		return newVal;
	}

	private float getAvailableWidth() {
		return getWidth() - getSelectorSize();
	}

	public void setSelectorPosition(@FloatRange(from = 0.0, to = 1.0) float selectorPosition) {
		this.selectorPosition = Math.min(selectorPosition, 1.0f);
		selector.setX(getAvailableWidth() * selectorPosition);
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
	 * sets a color resource of the slider border.
	 *
	 * @param resource color resource of the slider border.
	 */
	public void setBorderColorRes(@ColorRes int resource) {
		int color = ContextCompat.getColor(getContext(), resource);
		setBorderColor(color);
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
		if (brightnessListener != null) {
			brightnessListener.onBrightnessSelected(selectorPosition, fromUser);
		}
	}

	@ColorInt
	public int assembleColor() {
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] = selectorPosition;
		return Color.HSVToColor(hsv);
	}

	/**
	 * gets selector's position ratio.
	 *
	 * @return selector's position ratio.
	 */
	public float getSelectorPosition() {
		return this.selectorPosition;
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
