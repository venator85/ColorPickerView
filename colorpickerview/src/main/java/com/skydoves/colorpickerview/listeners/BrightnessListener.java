package com.skydoves.colorpickerview.listeners;

import androidx.annotation.FloatRange;

public interface BrightnessListener {
  default void onUserStartedDragging() {}
  default void onUserStoppedDragging() {}

  void onBrightnessSelected(@FloatRange(from = 0f, to=1f) float brightness, boolean fromUser);
}
