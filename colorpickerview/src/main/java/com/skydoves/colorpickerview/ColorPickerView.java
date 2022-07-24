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

package com.skydoves.colorpickerview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.FloatRange;
import androidx.annotation.MainThread;
import androidx.annotation.Px;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;
import com.skydoves.colorpickerview.listeners.ColorListener;
import com.skydoves.colorpickerview.listeners.ColorPickerViewListener;

/**
 * ColorPickerView implements getting HSV colors, ARGB values, Hex color codes from any image
 * drawables.
 *
 * <p>{@link ColorPickerViewListener} will be invoked whenever ColorPickerView is triggered by
 * {@link ActionMode} rules.
 */
@SuppressWarnings("unused")
public class ColorPickerView extends FrameLayout {

  @ColorInt
  private int selectedPureColor;
  @ColorInt
  private int selectedColor;
  private Point selectedPoint;
  private ImageView palette;
  private ImageView selector;
  private Drawable paletteDrawable;
  private Drawable selectorDrawable;
  public ColorListener colorListener;

  @Px
  private int selectorSize = 0;

  public ColorPickerView(Context context) {
    super(context);
  }

  public ColorPickerView(Context context, AttributeSet attrs) {
    super(context, attrs);
    getAttrs(attrs);
    onCreate();
  }

  public ColorPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    getAttrs(attrs);
    onCreate();
  }

  public ColorPickerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    getAttrs(attrs);
    onCreate();
  }

  private void getAttrs(AttributeSet attrs) {
    TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ColorPickerView);
    try {
      if (a.hasValue(R.styleable.ColorPickerView_palette)) {
        this.paletteDrawable = a.getDrawable(R.styleable.ColorPickerView_palette);
      }
      if (a.hasValue(R.styleable.ColorPickerView_selector)) {
        int resourceId = a.getResourceId(R.styleable.ColorPickerView_selector, -1);
        if (resourceId != -1) {
          this.selectorDrawable = AppCompatResources.getDrawable(getContext(), resourceId);
        }
      }
      if (a.hasValue(R.styleable.ColorPickerView_selector_size)) {
        this.selectorSize = a.getDimensionPixelSize(R.styleable.ColorPickerView_selector_size, selectorSize);
      }
      if (a.hasValue(R.styleable.ColorPickerView_initialColor)) {
        setInitialColor(a.getColor(R.styleable.ColorPickerView_initialColor, Color.WHITE));
      }
    } finally {
      a.recycle();
    }
  }

  private void onCreate() {
    setPadding(0, 0, 0, 0);
    palette = new ImageView(getContext());
    if (paletteDrawable != null) {
      palette.setImageDrawable(paletteDrawable);
    }

    LayoutParams paletteParam = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    paletteParam.gravity = Gravity.CENTER;
    addView(palette, paletteParam);

    selector = new ImageView(getContext());
    if (selectorDrawable != null) {
      selector.setImageDrawable(selectorDrawable);
    } else {
      selector.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.wheel));
    }
    LayoutParams selectorParam = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    if (selectorSize != 0) {
      selectorParam.width = SizeUtils.dp2Px(getContext(), selectorSize);
      selectorParam.height = SizeUtils.dp2Px(getContext(), selectorSize);
    }
    selectorParam.gravity = Gravity.CENTER;
    addView(selector, selectorParam);

    getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
        onFinishInflated();
      }
    });
  }

  @Override
  protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
    super.onSizeChanged(width, height, oldWidth, oldHeight);

    if (palette.getDrawable() == null) {
      Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
      palette.setImageDrawable(new ColorHsvPalette(getResources(), bitmap));
    }
  }

  private void onFinishInflated() {
    if (getParent() != null && getParent() instanceof ViewGroup) {
      ((ViewGroup) getParent()).setClipChildren(false);
    }

    selectCenter();
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (!this.isEnabled()) {
      return false;
    }
    int actionMasked = event.getActionMasked();
    if (actionMasked == MotionEvent.ACTION_DOWN || actionMasked == MotionEvent.ACTION_MOVE || actionMasked == MotionEvent.ACTION_UP) {
      selector.setPressed(true);
      return onTouchReceived(event);
    }
    selector.setPressed(false);
    return false;
  }

  /**
   * notify to the other views by the onTouchEvent.
   *
   * @param event {@link MotionEvent}.
   * @return notified or not.
   */
  @MainThread
  private boolean onTouchReceived(final MotionEvent event) {
    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
      if (colorListener != null) {
        colorListener.onUserStartedDragging();
      }
    }

    Point snapPoint = getColorPoint(new Point((int) event.getX(), (int) event.getY()));
    int pixelColor = getColorFromBitmap(snapPoint.x, snapPoint.y);

    this.selectedPureColor = pixelColor;
    this.selectedColor = pixelColor;
    this.selectedPoint = getColorPoint(new Point(snapPoint.x, snapPoint.y));
    setCoordinate(snapPoint.x, snapPoint.y);

    notifyColorChanged();

    if (event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
      if (colorListener != null) {
        colorListener.onUserStoppedDragging();
      }
    }

    return true;
  }

  public boolean isHuePalette() {
    return palette.getDrawable() != null && palette.getDrawable() instanceof ColorHsvPalette;
  }

  private void notifyColorChanged() {
    fireColorListener(getColor(), true);
  }

  /**
   * gets a pixel color on the specific coordinate from the bitmap.
   *
   * @param x coordinate x.
   * @param y coordinate y.
   * @return selected color.
   */
  protected int getColorFromBitmap(float x, float y) {
    Matrix invertMatrix = new Matrix();
    palette.getImageMatrix().invert(invertMatrix);

    float[] mappedPoints = new float[]{x, y};
    invertMatrix.mapPoints(mappedPoints);

    if (palette.getDrawable() != null
            && palette.getDrawable() instanceof BitmapDrawable
            && mappedPoints[0] >= 0
            && mappedPoints[1] >= 0
            && mappedPoints[0] < palette.getDrawable().getIntrinsicWidth()
            && mappedPoints[1] < palette.getDrawable().getIntrinsicHeight()) {

      invalidate();

      if (palette.getDrawable() instanceof ColorHsvPalette) {
        x = x - getWidth() * 0.5f;
        y = y - getHeight() * 0.5f;
        double r = Math.sqrt(x * x + y * y);
        float radius = Math.min(getWidth(), getHeight()) * 0.5f;
        float[] hsv = {0, 0, 1};
        hsv[0] = (float) (Math.atan2(y, -x) / Math.PI * 180f) + 180;
        hsv[1] = Math.max(0f, Math.min(1f, (float) (r / radius)));
        return Color.HSVToColor(hsv);
      } else {
        Rect rect = palette.getDrawable().getBounds();
        float scaleX = mappedPoints[0] / rect.width();
        int x1 = (int) (scaleX * ((BitmapDrawable) palette.getDrawable()).getBitmap().getWidth());
        float scaleY = mappedPoints[1] / rect.height();
        int y1 = (int) (scaleY * ((BitmapDrawable) palette.getDrawable()).getBitmap().getHeight());
        return ((BitmapDrawable) palette.getDrawable()).getBitmap().getPixel(x1, y1);
      }
    }
    return 0;
  }

  public void setColorListener(ColorListener colorListener) {
    this.colorListener = colorListener;
  }

  /**
   * invokes {@link ColorListener} or {@link ColorEnvelopeListener} with a color value.
   *
   * @param color    color.
   * @param fromUser triggered by user or not.
   */
  public void fireColorListener(@ColorInt int color, final boolean fromUser) {
    if (this.colorListener != null) {
      this.selectedColor = color;
      colorListener.onColorSelected(selectedColor, fromUser);
    }
  }

  /**
   * gets the selected color.
   *
   * @return the selected color.
   */
  public @ColorInt
  int getColor() {
    return selectedColor;
  }

  /**
   * gets an alpha value from the selected color.
   *
   * @return alpha from the selected color.
   */
  public @FloatRange(from = 0.0, to = 1.0)
  float getAlpha() {
    return Color.alpha(getColor()) / 255f;
  }

  /**
   * gets the selected pure color without alpha and brightness.
   *
   * @return the selected pure color.
   */
  public @ColorInt
  int getPureColor() {
    return selectedPureColor;
  }

  /**
   * sets the pure color.
   *
   * @param color the pure color.
   */
  public void setPureColor(@ColorInt int color) {
    this.selectedPureColor = color;
  }

  /**
   * gets the {@link ColorEnvelope} of the selected color.
   *
   * @return {@link ColorEnvelope}.
   */
  public ColorEnvelope getColorEnvelope() {
    return new ColorEnvelope(getColor());
  }

  /**
   * gets center coordinate of the selector.
   *
   * @param x coordinate x.
   * @param y coordinate y.
   * @return the center coordinate of the selector.
   */
  private Point getCenterPoint(int x, int y) {
    return new Point(x - (selector.getMeasuredWidth() / 2), y - (selector.getMeasuredHeight() / 2));
  }

  /**
   * gets a selector.
   *
   * @return selector.
   */
  public ImageView getSelector() {
    return this.selector;
  }

  /**
   * gets a selector's selected coordinate x.
   *
   * @return a selected coordinate x.
   */
  public float getSelectorX() {
    return selector.getX() - (selector.getMeasuredWidth() * 0.5f);
  }

  /**
   * gets a selector's selected coordinate y.
   *
   * @return a selected coordinate y.
   */
  public float getSelectorY() {
    return selector.getY() - (selector.getMeasuredHeight() * 0.5f);
  }

  /**
   * gets a selector's selected coordinate.
   *
   * @return a selected coordinate {@link Point}.
   */
  public Point getSelectedPoint() {
    return selectedPoint;
  }

  /**
   * changes selector's selected point with notifies about changes manually.
   *
   * @param x coordinate x of the selector.
   * @param y coordinate y of the selector.
   */
  public void setSelectorPoint(int x, int y) {
    Point mappedPoint = getColorPoint(new Point(x, y));
    int color = getColorFromBitmap(mappedPoint.x, mappedPoint.y);
    selectedPureColor = color;
    selectedColor = color;
    selectedPoint = new Point(mappedPoint.x, mappedPoint.y);
    setCoordinate(mappedPoint.x, mappedPoint.y);
    fireColorListener(getColor(), false);
  }

  /**
   * moves selector's selected point with notifies about changes manually.
   *
   * @param x coordinate x of the selector.
   * @param y coordinate y of the selector.
   */
  public void moveSelectorPoint(int x, int y, @ColorInt int color) {
    selectedPureColor = color;
    selectedColor = color;
    selectedPoint = new Point(x, y);
    setCoordinate(x, y);
    fireColorListener(getColor(), false);
  }

  /**
   * changes selector's selected point without notifies.
   *
   * @param x coordinate x of the selector.
   * @param y coordinate y of the selector.
   */
  public void setCoordinate(int x, int y) {
    selector.setX(x - (selector.getMeasuredWidth() * 0.5f));
    selector.setY(y - (selector.getMeasuredHeight() * 0.5f));
  }

  /**
   * select a point by a specific color. this method will not work if the default palette drawable
   * is not {@link ColorHsvPalette}.
   *
   * @param color a starting color.
   */
  public void setInitialColor(@ColorInt final int color) {
    post(() -> {
      try {
        selectByHsvColor(color);
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    });
  }

  /**
   * select a point by a specific color resource. this method will not work if the default palette
   * drawable is not {@link ColorHsvPalette}.
   *
   * @param colorRes a starting color resource.
   */
  public void setInitialColorRes(@ColorRes final int colorRes) {
    setInitialColor(ContextCompat.getColor(getContext(), colorRes));
  }

  /**
   * changes selector's selected point by a specific color.
   *
   * <p>It will throw an exception if the default palette drawable is not {@link ColorHsvPalette}.
   *
   * @param color color.
   */
  public void selectByHsvColor(@ColorInt int color) throws IllegalAccessException {
    if (palette.getDrawable() instanceof ColorHsvPalette) {
      float[] hsv = new float[3];
      Color.colorToHSV(color, hsv);

      float centerX = getWidth() * 0.5f;
      float centerY = getHeight() * 0.5f;
      float radius = hsv[1] * Math.min(centerX, centerY);
      int pointX = (int) (radius * Math.cos(Math.toRadians(hsv[0])) + centerX);
      int pointY = (int) (-radius * Math.sin(Math.toRadians(hsv[0])) + centerY);

      Point mappedPoint = getColorPoint(new Point(pointX, pointY));
      selectedPureColor = color;
      selectedColor = color;
      selectedPoint = new Point(mappedPoint.x, mappedPoint.y);

      setCoordinate(mappedPoint.x, mappedPoint.y);
      fireColorListener(getColor(), false);
    } else {
      throw new IllegalAccessException("selectByHsvColor(@ColorInt int color) can be called only when the palette is an instance of ColorHsvPalette. Use setHsvPaletteDrawable();");
    }
  }

  /**
   * changes selector's selected point by a specific color resource.
   *
   * <p>It may not work properly if change the default palette drawable.
   *
   * @param resource a color resource.
   */
  public void selectByHsvColorRes(@ColorRes int resource) throws IllegalAccessException {
    selectByHsvColor(ContextCompat.getColor(getContext(), resource));
  }

  /**
   * changes selector drawable manually.
   *
   * @param drawable selector drawable.
   */
  public void setSelectorDrawable(Drawable drawable) {
    selector.setImageDrawable(drawable);
  }

  /**
   * selects the center of the palette manually.
   */
  public void selectCenter() {
    setSelectorPoint(getMeasuredWidth() / 2, getMeasuredHeight() / 2);
  }

  /**
   * sets enabling or not the ColorPickerView and slide bars.
   *
   * @param enabled true/false flag for making enable or not.
   */
  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);

    selector.setVisibility(enabled ? VISIBLE : INVISIBLE);

    if (enabled) {
      palette.clearColorFilter();
    } else {
      int color = Color.argb(70, 255, 255, 255);
      palette.setColorFilter(color);
    }
  }

  protected Point getColorPoint(Point point) {
    if (isHuePalette()) {
      return getHuePoint(point);
    }
    Point center = new Point(getMeasuredWidth() / 2, getMeasuredHeight() / 2);
    return approximatedPoint(point, center);
  }

  private Point approximatedPoint(Point start, Point end) {
    if (getDistance(start, end) <= 3) {
      return end;
    }
    Point center = getCenterPoint(start, end);
    int color = getColorFromBitmap(center.x, center.y);
    if (color == Color.TRANSPARENT) {
      return approximatedPoint(center, end);
    } else {
      return approximatedPoint(start, center);
    }
  }

  private Point getHuePoint(Point point) {
    float centerX = getWidth() * 0.5f;
    float centerY = getHeight() * 0.5f;
    float x = point.x - centerX;
    float y = point.y - centerY;
    float radius = Math.min(centerX, centerY);
    double r = Math.sqrt(x * x + y * y);
    if (r > radius) {
      x *= radius / r;
      y *= radius / r;
    }
    return new Point((int) (x + centerX), (int) (y + centerY));
  }

  private static Point getCenterPoint(Point start, Point end) {
    return new Point((end.x + start.x) / 2, (end.y + start.y) / 2);
  }

  private static int getDistance(Point start, Point end) {
    return (int) Math.sqrt(Math.abs(end.x - start.x) * Math.abs(end.x - start.x) + Math.abs(end.y - start.y) * Math.abs(end.y - start.y));
  }
}
