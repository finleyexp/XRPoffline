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

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * An implementation of {@code RecyclerView.ViewHolder} that supports simple user actions.
 * <p/>
 * Information about click and long-click can be delivered to adapter.
 */
public abstract class ClickableViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener, View.OnLongClickListener {

    /**
     * Interface definition for a callback to be invoked when the {@code RecyclerView.ViewHolder}
     * is clicked or long-clicked.
     */
    public interface OnViewHolderClickListener {

        /**
         * Called when {@code RecyclerView.ViewHolder} was clicked.
         *
         * @param holder {@code RecyclerView.ViewHolder} that was clicked
         */
        void onViewHolderClick(RecyclerView.ViewHolder holder);

        /**
         * Called when {@code RecyclerView.ViewHolder} was long-clicked.
         *
         * @param holder {@code RecyclerView.ViewHolder} that was long-clicked
         */
        void onViewHolderLongClick(RecyclerView.ViewHolder holder);
    }

    private final OnViewHolderClickListener listener;

    public ClickableViewHolder(View view, OnViewHolderClickListener listener) {
        super(view);

        this.listener = listener;
        if (listener != null) {
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        listener.onViewHolderClick(this);
    }

    @Override
    public boolean onLongClick(View view) {
        listener.onViewHolderLongClick(this);

        return true;
    }
}