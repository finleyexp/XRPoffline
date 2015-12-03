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
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;

/**
 * Simple and yet working implementation of {@code CoordinatorLayout.Behavior} for
 * {@code FloatingActionButton}
 * <p/>
 * Original implementation included in Support library (i.a.) does not support API < 11 devices
 */
@SuppressWarnings("unused") // Used in XML layout only
public class FloatingActionButtonBehavior
        extends CoordinatorLayout.Behavior<FloatingActionButton> {

    public FloatingActionButtonBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton child,
                                   View dependency) {

        // Depend on SnackbarLayout only
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton child,
                                          View dependency) {

        // Calculate and set FloatingActionButton vertical translation based
        // on the actual SnackbarLayout vertical translation and height
        final float translationY = Math.min(0, ViewCompat.getTranslationY(dependency)
                - dependency.getHeight());

        ViewCompat.setTranslationY(child, translationY);

        return true;
    }

    @Override
    public void onDependentViewRemoved(CoordinatorLayout parent, FloatingActionButton child,
                                       View dependency) {

        // Snackbar was removed, animate the FloatingActionButton to its default position
        if (ViewCompat.getTranslationY(child) != 0f) {
            ViewCompat.animate(child).translationY(0f)
                    .setInterpolator(new FastOutSlowInInterpolator());
        }
    }
}