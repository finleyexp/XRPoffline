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
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/**
 * Shows live camera preview to the user and allows them to catch preview frames
 */
@SuppressWarnings("deprecation") // Using deprecated Camera class to support API < 21 devices
public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = CameraView.class.getName();

    private Camera camera;
    private Camera.PreviewCallback previewCallback;

    private Handler autoFocusHandler;

    private boolean isSurfaceCreated;

    public CameraView(Context context) {
        super(context);

        getHolder().addCallback(this);

        // Required on API < 11 devices
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /**
     * Sets the {@code Camera} instance to be used by this {@code CameraView} and starts
     * camera preview.
     *
     * @param camera   an opened {@code Camera} instance
     * @param callback {@code Camera.PreviewCallback} used to deliver preview frames
     */
    public void setCamera(Camera camera, Camera.PreviewCallback callback) {
        this.camera = camera;
        this.previewCallback = callback;

        autoFocusHandler = new Handler();

        // If the surface was already created, request a new layer pass to start camera preview
        if (isSurfaceCreated) {
            requestLayout();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (camera != null) {
            // Surface was created, use it as a preview display for camera
            try {
                camera.setPreviewDisplay(holder);
            } catch (IOException e) {
                Log.e(TAG, "Failed to set preview display: " + holder, e);
            }
        }

        isSurfaceCreated = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (camera == null || holder.getSurface() == null) {
            return;
        }

        // Calculate and set display orientation for camera based on default display rotation
        int degrees = 0;

        final int rotation = DisplayUtils.getDisplayRotation(getContext());
        switch (rotation) {
            case Surface.ROTATION_180: {
                degrees += 90;
            }
            case Surface.ROTATION_270: {
                degrees += 90;
            }
            case Surface.ROTATION_0: {
                degrees += 90;
            }
            case Surface.ROTATION_90: {
            }
        }

        camera.setDisplayOrientation(degrees);

        // Set optimal preview size for camera
        final Camera.Size previewSize = getOptimalPreviewSize();
        if (previewSize != null) {
            final Camera.Parameters params = camera.getParameters();
            params.setPreviewSize(previewSize.width, previewSize.height);

            camera.setParameters(params);
        }

        // Start camera preview
        camera.setOneShotPreviewCallback(previewCallback);
        camera.startPreview();
        camera.autoFocus(autoFocusCallback);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null) {
            // Surface is being destroyed, stop camera preview
            camera.cancelAutoFocus();
            camera.setOneShotPreviewCallback(null);
            camera.stopPreview();
        }

        isSurfaceCreated = false;
    }

    private Camera.Size getOptimalPreviewSize() {
        if (camera == null) {
            return null;
        }

        Camera.Size optimalSize = null;

        // Choose from supported sizes only
        final List<Camera.Size> sizes = camera.getParameters().getSupportedPreviewSizes();

        final Point resolution = DisplayUtils.getDisplayResolution(getContext());
        final double targetRatio = resolution.y / resolution.x;

        final double ASPECT_TOLERANCE = 0.1;

        double minDiff = Double.MAX_VALUE;

        // Try to find a size that matches the current display aspect ratio
        for (Camera.Size size : sizes) {
            final double ratio = size.height / size.width;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                continue;
            }

            final int diff = Math.abs(size.height - resolution.y);
            if (diff < minDiff) {
                optimalSize = size;

                minDiff = diff;
            }
        }

        // Cannot find a match, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;

            for (Camera.Size size : sizes) {
                final int diff = Math.abs(size.height - resolution.y);
                if (diff < minDiff) {
                    optimalSize = size;

                    minDiff = diff;
                }
            }
        }

        return optimalSize;
    }

    private final Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {

        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            // Run camera auto-focus constantly in one second intervals
            autoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };

    private final Runnable doAutoFocus = new Runnable() {

        @Override
        public void run() {
            if (camera != null && isSurfaceCreated) {
                camera.autoFocus(autoFocusCallback);
            }
        }
    };
}