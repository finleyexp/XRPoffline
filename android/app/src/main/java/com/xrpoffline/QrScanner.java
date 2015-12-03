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
import android.hardware.Camera;
import android.widget.FrameLayout;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

@SuppressWarnings("deprecation") // Using deprecated Camera class to support API < 21 devices
public class QrScanner extends FrameLayout implements Camera.PreviewCallback {

    /**
     * Interface definition for a callback to be invoked when a valid QR-Code has been captured.
     */
    public interface OnCaptureListener {
        /**
         * Called when a QR-Code was successfully captured.
         *
         * @param content text represented by QR-Code
         */
        void onCapture(String content);
    }

    // Load necessary libraries for converting strings
    static {
        System.loadLibrary("iconv");
    }

    private Camera camera;
    private final CameraView cameraView;

    private final ImageScanner scanner;

    private String filter = "";

    private OnCaptureListener onCaptureListener;

    public QrScanner(Context context) {
        super(context);

        cameraView = new CameraView(context);
        addView(cameraView);

        final ViewfinderView viewfinder = new ViewfinderView(context);
        addView(viewfinder);

        scanner = new ImageScanner();

        // Decode QR-Codes only
        scanner.setConfig(0, Config.ENABLE, 0);
        scanner.setConfig(Symbol.QRCODE, Config.ENABLE, 1);

        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);
    }

    /**
     * Register a callback to be invoked when a valid QR-Code has been captured.
     *
     * @param listener the callback that will run
     */
    public void setOnCaptureListener(OnCaptureListener listener) {
        onCaptureListener = listener;
    }

    /**
     * Sets the filter key which QR-Code must contain to be captured by this scanner.
     *
     * @param filter {@code String} to be used as filter key
     */
    public void setFilter(String filter) {
        this.filter = filter;
    }

    /**
     * Starts camera preview and initialize scanning process.
     * <p/>
     * This method should be called when the {@code Activity} or {@code Fragment} is being resumed.
     */
    public void start() {
        camera = CameraUtils.getCameraInstance();
        if (camera != null) {
            cameraView.setCamera(camera, this);
        }
    }

    /**
     * Stops scanning process and release camera resource.
     * <p/>
     * Because the {@code Camera} object is a shared resource, it's a good practice
     * to call this method when the {@code Activity} or {@code Fragment} is being paused.
     */
    public void stop() {
        if (camera != null) {
            cameraView.setCamera(null, null);

            camera.release();
            camera = null;
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (onCaptureListener != null) {
            final Camera.Size size = camera.getParameters().getPreviewSize();

            final Image image = new Image(size.width, size.height, "Y800");
            image.setData(data);

            // Scan image for QR-Codes
            if (scanner.scanImage(image) != 0) {
                final SymbolSet symbols = scanner.getResults();
                for (Symbol symbol : symbols) {
                    final String content = symbol.getData();
                    // Notify a valid QR-Code containing filter key has been detected
                    if (!content.isEmpty() && content.contains(filter)) {
                        onCaptureListener.onCapture(content);

                        break;
                    }
                }
            } else {
                // No QR-Code was detected, catch next preview frame
                camera.setOneShotPreviewCallback(this);
            }
        }
    }
}