package com.skydoves.colorpickerview.sliders;

import androidx.annotation.FloatRange;

public class IntInterpolation {
    public final int min;
    public final int max;
    public final int step;

    public IntInterpolation(int min, int max, int step) {
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public int interpolate(@FloatRange(from = 0f, to = 1f) float value) {
        float x = (int) (min + (max - min) * value);
        return Math.round(step * (x / step));
    }

    @FloatRange(from = 0f, to = 1f)
    public float reverseInterpolate(int value) {
        return (value - min) / (float) (max - min);
    }
}
