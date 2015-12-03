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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.xrpoffline.R;

/**
 * Simple overlay on top of the camera view. It adds the viewfinder square
 * and partial transparency outside it, as well as the "laser line" animation
 * to show scanning is active.
 */
public class ViewfinderView extends View {

    private static final int[] LASER_ALPHAS = {0, 64, 128, 192, 255, 192, 128, 64};
    private int laserAlphaIndex;

    private static final long LASER_ANIMATION_DELAY = 80;

    private Paint paint;

    public ViewfinderView(Context context) {
        super(context);

        paint = new Paint();
    }

    @Override
    public void onDraw(Canvas canvas) {
        final int width = canvas.getWidth();
        final int height = canvas.getHeight();

        // Calculate appropriate viewfinder square size with screen offset
        int diameter = width;
        if (height < width) {
            diameter = height;
        }

        final float OFFSET_RATIO = 0.1f;

        final int offset = (int) (diameter * OFFSET_RATIO);
        diameter -= offset;

        // Calculate viewfinder square corners
        final int left = width / 2 - diameter / 2;
        final int top = height / 2 - diameter / 2;
        final int right = width / 2 + diameter / 2;
        final int bottom = height / 2 + diameter / 2;

        // Draw the mask surrounding viewfinder square
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.viewfinder_mask));

        canvas.drawRect(0, 0, width, top, paint);
        canvas.drawRect(0, bottom, width, height, paint);
        canvas.drawRect(0, top, left, bottom, paint);
        canvas.drawRect(right, top, width, bottom, paint);

        // Draw viewfinder borders
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.viewfinder_border));

        canvas.drawRect(left, top, right, bottom, paint);

        // Draw pulsing "laser line" through the middle of viewfinder square
        paint.setColor(ContextCompat.getColor(getContext(), R.color.viewfinder_laser));
        paint.setAlpha(LASER_ALPHAS[laserAlphaIndex]);

        laserAlphaIndex = (laserAlphaIndex + 1) % LASER_ALPHAS.length;

        canvas.drawLine(left + 4, height / 2, right - 4, height / 2, paint);

        // Animate "laser line" only
        postInvalidateDelayed(LASER_ANIMATION_DELAY,
                left + 4, height / 2 - 2, right - 4, height / 2 + 2);
    }
}