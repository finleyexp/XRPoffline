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
import android.provider.BaseColumns;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

/**
 * String related utilities including time formatting
 */
public class TextUtils {

    /**
     * Default date template.
     */
    public static final String TEMPLATE_DATE_DEFAULT = "yyyy-MM-dd HH:mm:ss";

    // Static methods only
    private TextUtils() {
    }

    /**
     * Converts epoch to human readable format using default template.
     *
     * @param context the context with resources
     * @param time    time since epoch in milliseconds
     * @return human readable date
     */
    public static String getTimeString(Context context, long time) {
        return getTimeString(context, time, TEMPLATE_DATE_DEFAULT);
    }

    /**
     * Converts epoch to human readable format using custom template.
     *
     * @param context  the context with resources
     * @param time     time since epoch in milliseconds
     * @param template format template
     * @return human readable date
     */
    public static String getTimeString(Context context, long time, String template) {
        final SimpleDateFormat format =
                new SimpleDateFormat(template, context.getResources().getConfiguration().locale);

        return format.format(time);
    }

    /**
     * Creates criterion based on multiple SQLite column ids.
     *
     * @param ids SQLite database row ids
     * @return criterion usable with SQLite where clause
     */
    public static String makeWhereClause(List<Long> ids) {
        final Long[] idsArray = ids.toArray(new Long[ids.size()]);

        return BaseColumns._ID + " in " + Arrays.toString(idsArray)
                .replace("[", "(").replace("]", ")");
    }
}