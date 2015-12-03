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

import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * Interface used to obtain permission to swipe an item and listen for a dismissal event
 * from {@link ItemTouchHelper.Callback}.
 */
public interface ItemTouchHelperAdapter {

    /**
     * Called by {@link ItemTouchHelper.Callback} to check whether an item can be swiped out.
     *
     * @param position the position of the item to check
     * @return {@code true} if the item can be swiped out, {@code false} otherwise
     */
    boolean canItemSwipe(int position);

    /**
     * Called when an item has been dismissed by a swipe.
     *
     * @param position the position of the item dismissed
     */
    void onItemSwipe(int position);
}