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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Network related utilities
 */
public class NetUtils {

    // Static methods only
    private NetUtils() {
    }

    /**
     * Checks if network connectivity exists.
     *
     * @param context the context to get system service from
     * @return {@code true} if network connectivity exists, {@code false} otherwise
     */
    public static boolean isConnectedToNetwork(Context context) {
        final ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        final NetworkInfo info = cm.getActiveNetworkInfo();

        return info != null && info.isConnected();
    }
}