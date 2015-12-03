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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Fragment that manages QR-Code scanner.
 */
public class QrScannerFragment extends Fragment implements QrScanner.OnCaptureListener {

    /**
     * Interface definition for a callback to be invoked when scanner detects a valid QR-Code.
     */
    public interface OnScanFinishedListener {
        /**
         * Called when a QR-Code was successfully captured by scanner.
         *
         * @param content text represented by QR-Code
         */
        void onScanFinished(String content);
    }

    private QrScanner scanner;
    private OnScanFinishedListener onScanFinishedListener;

    /**
     * Creates new instance of {@code QrScannerFragment} with custom filter key.
     *
     * @param filter {@code String} to be used as filter key
     * @return a new instance of {@code QrScannerFragment}
     */
    public static QrScannerFragment newInstance(String filter) {
        final QrScannerFragment fragment = new QrScannerFragment();

        if (filter != null && !filter.isEmpty()) {
            final Bundle bundle = new Bundle();
            bundle.putString("filter", filter);

            fragment.setArguments(bundle);
        }

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            onScanFinishedListener = (OnScanFinishedListener) context;
        } catch (ClassCastException exception) {
            throw new ClassCastException(context.getClass().getName() + " must implement "
                    + OnScanFinishedListener.class.getName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        scanner = new QrScanner(getContext());

        final Bundle arguments = getArguments();
        if (arguments != null) {
            final String filter = arguments.getString("filter");

            scanner.setFilter(filter);
        }

        return scanner;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Fragment is being resumed, start the scanner
        scanner.setOnCaptureListener(this);
        scanner.start();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Fragment is being paused, stop the scanner to release camera resource
        scanner.setOnCaptureListener(null);
        scanner.stop();
    }

    @Override
    public void onCapture(String content) {
        // Pass result to activity
        onScanFinishedListener.onScanFinished(content);
    }
}