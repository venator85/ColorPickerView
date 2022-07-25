package com.skydoves.colorpickerview.sliders;

import android.graphics.Color;

import androidx.annotation.ColorInt;

public class BrightnessUtils {

	public static int[] colorsForBrightnessGradient(@ColorInt int color) {
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] = 0;
		int startColor = Color.HSVToColor(hsv);
		hsv[2] = 1;
		int endColor = Color.HSVToColor(hsv);
		return new int[]{startColor, endColor};
	}

}
