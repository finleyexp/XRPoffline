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

import android.hardware.Camera;
import android.util.Log;

/**
 * Camera related utilities
 */
public class CameraUtils {

    private static final String TAG = CameraUtils.class.getName();

    // Static methods only
    private CameraUtils() {
    }

    /**
     * Obtains an instance of {@code Camera}.
     *
     * @return a new {@code Camera} instance or {@code null} if no suitable camera found
     */
    @SuppressWarnings("deprecation") // Using deprecated Camera class to support API < 21 devices
    public static Camera getCameraInstance() {
        Camera camera = null;

        final Camera.CameraInfo info = new Camera.CameraInfo();

        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, info);

            // Try to instantiate back-facing camera first
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                try {
                    camera = Camera.open(i);

                    break;
                } catch (Exception e) {
                    Log.e(TAG, "Failed to initialize back-facing camera", e);
                }
            }
        }

        // No back-facing camera found, use first front-facing camera available
        if (camera == null) {
            try {
                camera = Camera.open();
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize default camera", e);
            }
        }

        return camera;
    }
}