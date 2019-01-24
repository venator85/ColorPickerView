
package com.skydoves.colorpickerview.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;

import com.skydoves.colorpickerview.ColorPickerView;

/**
 * ColorPickerPreferenceManager implements {@link SharedPreferences}
 * <p>for {@link com.skydoves.colorpickerview.ColorPickerView}.
 */
@SuppressWarnings({"WeakerAccess", "unused", "UnusedReturnValue"})
public class ColorPickerPreferenceManager {

    private static ColorPickerPreferenceManager colorPickerPreferenceManager;
    private SharedPreferences sharedPreferences;

    protected static final String COLOR = "_COLOR";
    protected static final String SelectorX = "_SELECTOR_X";
    protected static final String SelectorY = "_SELECTOR_Y";
    protected static final String AlphaSlider = "_SLIDER_ALPHA";
    protected static final String BrightnessSlider = "_SLIDER_BRIGHTNESS";

    private ColorPickerPreferenceManager(Context context) {
        sharedPreferences =
                context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
    }

    /**
     * gets an instance of the {@link ColorPickerPreferenceManager}.
     * @param context context.
     * @return {@link ColorPickerPreferenceManager}.
     */
    public static ColorPickerPreferenceManager getInstance(Context context) {
        if (colorPickerPreferenceManager == null) colorPickerPreferenceManager = new ColorPickerPreferenceManager(context);
        return colorPickerPreferenceManager;
    }

    /**
     * saves a color on preference.
     * @param name preference name.
     * @param color preference color.
     * @return {@link ColorPickerPreferenceManager}.
     */
    public ColorPickerPreferenceManager setColor(String name, int color) {
        sharedPreferences.edit().putInt(getColorName(name), color).apply();
        return colorPickerPreferenceManager;
    }

    /**
     * gets the saved color from preference.
     * @param name preference name.
     * @param defaultColor default preference color.
     * @return the saved color.
     */
    public int getColor(String name, int defaultColor) {
        return sharedPreferences.getInt(getColorName(name), defaultColor);
    }

    /**
     * clears the saved color from preference.
     * @param name preference name.
     * @return {@link ColorPickerPreferenceManager}.
     */
    public ColorPickerPreferenceManager clearSavedColor(String name) {
        sharedPreferences.edit().remove(getColorName(name)).apply();
        return colorPickerPreferenceManager;
    }

    /**
     * saves a selector position on preference.
     * @param name preference name.
     * @param position position of the selector.
     * @return {@link ColorPickerPreferenceManager}.
     */
    public ColorPickerPreferenceManager setSelectorPosition(String name, Point position) {
        sharedPreferences.edit().putInt(getSelectorXName(name), position.x).apply();
        sharedPreferences.edit().putInt(getSelectorYName(name), position.y).apply();
        return colorPickerPreferenceManager;
    }

    /**
     * gets the saved selector position on preference.
     * @param name preference name.
     * @param defaultPoint default position of the selector.
     * @return the saved selector position.
     */
    public Point getSelectorPosition(String name, Point defaultPoint) {
        return new Point(sharedPreferences.getInt(getSelectorXName(name), defaultPoint.x),
                sharedPreferences.getInt(getSelectorYName(name), defaultPoint.y));
    }

    /**
     * clears the saved selector position from preference.
     * @param name preference name.
     * @return {@link ColorPickerPreferenceManager}.
     */
    public ColorPickerPreferenceManager clearSavedSelectorPosition(String name) {
        sharedPreferences.edit().remove(getSelectorXName(name)).apply();
        sharedPreferences.edit().remove(getSelectorYName(name)).apply();
        return colorPickerPreferenceManager;
    }

    /**
     * sets an alpha slider position.
     * @param name preference name.
     * @param position position of the {@link com.skydoves.colorpickerview.sliders.AlphaSlideBar}.
     * @return {@link ColorPickerPreferenceManager}.
     */
    public ColorPickerPreferenceManager setAlphaSliderPosition(String name, float position) {
        sharedPreferences.edit().putFloat(getAlphaSliderName(name), position).apply();
        return colorPickerPreferenceManager;
    }

    /**
     * gets the alpha slider position.
     * @param name preference name.
     * @param defaultPosition default position of alpha slider position.
     * @return {@link ColorPickerPreferenceManager}.
     */
    public float getAlphaSliderPosition(String name, float defaultPosition) {
        return sharedPreferences.getFloat(getAlphaSliderName(name), defaultPosition);
    }

    /**
     * clears the saved alpha slider position from preference.
     * @param name preference name.
     * @return {@link ColorPickerPreferenceManager}.
     */
    public ColorPickerPreferenceManager clearSavedAlphaSliderPosition(String name) {
        sharedPreferences.edit().remove(getAlphaSliderName(name)).apply();
        return colorPickerPreferenceManager;
    }

    /**
     * sets an brightness slider position.
     * @param name preference name.
     * @param position position of the {@link com.skydoves.colorpickerview.sliders.BrightnessSlideBar}.
     * @return {@link ColorPickerPreferenceManager}.
     */
    public ColorPickerPreferenceManager setBrightnessSliderPosition(String name, float position) {
        sharedPreferences.edit().putFloat(getBrightnessSliderName(name), position).apply();
        return colorPickerPreferenceManager;
    }

    /**
     * gets the brightness slider position.
     * @param name preference name.
     * @param defaultPosition default position of brightness slider position.
     * @return {@link ColorPickerPreferenceManager}.
     */
    public float getBrightnessSliderPosition(String name, float defaultPosition) {
        return sharedPreferences.getFloat(getBrightnessSliderName(name),defaultPosition);
    }

    /**
     * clears the saved brightness slider position from preference.
     * @param name preference name.
     * @return {@link ColorPickerPreferenceManager}.
     */
    public ColorPickerPreferenceManager clearSavedBrightnessSlider(String name) {
        sharedPreferences.edit().remove(getBrightnessSliderName(name)).apply();
        return colorPickerPreferenceManager;
    }

    /**
     * saves all data of the {@link ColorPickerView} on the preference.
     * @param colorPickerView {@link ColorPickerView}.
     */
    public void saveColorPickerData(ColorPickerView colorPickerView) {
        if (colorPickerView != null && colorPickerView.getPreferenceName() != null) {
            String name = colorPickerView.getPreferenceName();
            setColor(name, colorPickerView.getColor());
            setSelectorPosition(name, colorPickerView.getSelectedPoint());
            setAlphaSliderPosition(name, colorPickerView.getAlphaSlideBar().getSelectorPosition());
            setBrightnessSliderPosition(name, colorPickerView.getBrightnessSlider().getSelectorPosition());
        }
    }

    /**
     * restores all data from the preference.
     * @param colorPickerView {@link ColorPickerView}.
     */
    public void restoreColorPickerData(ColorPickerView colorPickerView) {
        if (colorPickerView != null && colorPickerView.getPreferenceName() != null) {
            String name = colorPickerView.getPreferenceName();

        }
    }

    /**
     * clears all saved preference data.
     * @return {@link ColorPickerPreferenceManager}.
     */
    public ColorPickerPreferenceManager clearSavedAllData() {
        sharedPreferences.edit().clear().apply();
        return colorPickerPreferenceManager;
    }

    protected String getColorName(String name) {
        return name + COLOR;
    }

    protected String getSelectorXName(String name) {
        return name + SelectorX;
    }

    protected String getSelectorYName(String name) {
        return name + SelectorY;
    }

    protected String getAlphaSliderName(String name) {
        return name + AlphaSlider;
    }

    protected String getBrightnessSliderName(String name) {
        return name + BrightnessSlider;
    }
}
