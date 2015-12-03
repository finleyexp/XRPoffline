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

import android.graphics.Canvas;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * An implementation of {@code ItemTouchHelper.Callback} that enables swipe-to-dismiss
 * functionality.
 */
public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final ItemTouchHelperAdapter mItemTouchHelperAdapter;

    public SimpleItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
        mItemTouchHelperAdapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        // This callback does not support drag & drop functionality
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder holder) {
        // Ask the adapter for permission to swipe this item
        final int swipeFlags = mItemTouchHelperAdapter.canItemSwipe(holder.getAdapterPosition()) ?
                // RecyclerView item can be swiped out in both directions
                ItemTouchHelper.START | ItemTouchHelper.END : 0;

        return makeMovementFlags(0, swipeFlags);
    }

    @Override
    public void onChildDraw(Canvas canvas, RecyclerView recyclerView,
                            RecyclerView.ViewHolder holder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {

        // Fade out the view as it is swiped out of the parent's bounds
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            final float alpha = 1f - Math.abs(dX) / (float) holder.itemView.getWidth();

            ViewCompat.setAlpha(holder.itemView, alpha);
            ViewCompat.setTranslationX(holder.itemView, dX);
        } else {
            super.onChildDraw(canvas, recyclerView, holder, dX, dY, actionState, isCurrentlyActive);
        }
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder holder) {
        super.clearView(recyclerView, holder);

        // User interaction with a view is over, restore its full alpha
        ViewCompat.setAlpha(holder.itemView, 1f);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder holder,
                          RecyclerView.ViewHolder target) {

        // This callback does not support drag & drop functionality
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder holder, int direction) {
        if (mItemTouchHelperAdapter != null) {
            // Notify the adapter of the dismissal
            mItemTouchHelperAdapter.onItemSwipe(holder.getAdapterPosition());
        }
    }
}