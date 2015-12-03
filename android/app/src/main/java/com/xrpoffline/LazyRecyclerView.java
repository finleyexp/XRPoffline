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
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * An implementation of {@code RecyclerView} that fixes an "unwanted fling" issue.
 */
public class LazyRecyclerView extends RecyclerView {

    public LazyRecyclerView(Context context) {
        this(context, null);
    }

    public LazyRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LazyRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public int getMinFlingVelocity() {
        // Multiply velocity returned
        return super.getMinFlingVelocity() * 12; // Estimated value
    }
}