/*
 * Copyright 2015 The XRPoffline Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xrpoffline;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * Display related utilities
 */
public class DisplayUtils {

    // Static methods only
    private DisplayUtils() {
    }

    /**
     * Returns the rotation of default display.
     *
     * @param context the context to get system service from
     * @return rotation of the screen from its natural orientation
     */
    public static int getDisplayRotation(Context context) {
        final WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        return wm.getDefaultDisplay().getRotation();
    }

    /**
     * Returns the resolution of default display.
     *
     * @param context the context to get system service from
     * @return {@code Point} holding absolute display resolution in pixels
     */
    public static Point getDisplayResolution(Context context) {
        final WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        final DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);

        return new Point(metrics.widthPixels, metrics.heightPixels);
    }
}