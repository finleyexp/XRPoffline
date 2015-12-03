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
import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An implementation of {@code RecyclerView.Adapter} that exposes data from a {@link Cursor}.
 * <p/>
 * This adapter also manages adding, selecting and removing items in an intuitive way.
 */
public class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements ItemTouchHelperAdapter, ClickableViewHolder.OnViewHolderClickListener {

    /**
     * Interface definition for a callback to be invoked when a touch or a gesture was performed
     * on adapter item.
     */
    public interface OnItemGestureListener {

        /**
         * Called when the item was clicked.
         *
         * @param position the position of the item clicked
         */
        void onItemClick(int position);

        /**
         * Called when the item was long-clicked.
         *
         * @param position the position of the item long-clicked
         */
        void onItemLongClick(int position);

        /**
         * Called when the item was swiped out.
         *
         * @param position the position of the item swiped
         */
        void onItemSwipe(int position);
    }

    private final Context context;

    private Cursor cursor;

    private SparseBooleanArray selectedPositions = new SparseBooleanArray();
    private final List<Integer> hiddenPositions = new ArrayList<>();

    private final OnItemGestureListener onItemGestureListener;

    public ItemAdapter(Context context, OnItemGestureListener listener) {
        this(context, null, listener);
    }

    public ItemAdapter(Context context, Cursor cursor, OnItemGestureListener listener) {
        this.context = context;

        if (cursor != null) {
            this.cursor = cursor;
        }

        onItemGestureListener = listener;

        // All items in the data set have unique ROW_ID
        setHasStableIds(true);
    }

    @Override
    public int getItemCount() {
        return cursor != null ? cursor.getCount() - getHiddenItemCount() : 0;
    }

    @Override
    public long getItemId(int position) {
        // Consider hidden items
        return getRawItemId(adjustPosition(position));
    }

    private long getRawItemId(int position) {
        // Include hidden items
        return cursor != null && cursor.moveToPosition(position) ?
                cursor.getLong(cursor.getColumnIndex(BaseColumns._ID)) : -1;
    }

    @Override
    public int getItemViewType(int position) {
        return cursor != null && cursor.moveToPosition(adjustPosition(position)) ?
                cursor.getInt(cursor.getColumnIndex(DatabaseHelper.TYPE)) : ItemTypes.TYPE_INVALID;
    }

    /**
     * Changes the underlying cursor to a new cursor.
     *
     * @param newCursor the new cursor to be used
     */
    public void swapCursor(Cursor newCursor) {
        if (cursor != null) {
            shiftSelectedItems();
        }

        cursor = newCursor;

        notifyDataSetChanged();
    }

    @Override
    public boolean canItemSwipe(int position) {
        // Do not allow swiping in action mode
        return getSelectedItemCount() == 0;
    }

    @Override
    public void onItemSwipe(int position) {
        // Hide the item on swipe gesture
        hideItem(position);

        if (onItemGestureListener != null) {
            onItemGestureListener.onItemSwipe(position);
        }
    }

    /**
     * Toggles a selection of an item.
     *
     * @param position the position of a item toggled
     */
    public void toggleItemSelection(int position) {
        final int adjustedPosition = adjustPosition(position);

        if (selectedPositions.get(adjustedPosition, false)) {
            selectedPositions.delete(adjustedPosition);
        } else {
            selectedPositions.put(adjustedPosition, true);
        }

        notifyItemChanged(position);
    }

    /**
     * Selects listed items.
     *
     * @param positions the list of items to be selected
     */
    public void selectItems(List<Integer> positions) {
        for (int position : positions) {
            selectedPositions.put(adjustPosition(position), true);
        }

        notifyDataSetChanged();
    }

    /**
     * Selects all of the items.
     */
    public void selectAllItems() {
        for (int i = 0; i < getItemCount(); i++) {
            selectedPositions.put(adjustPosition(i), true);
        }

        notifyDataSetChanged();
    }

    /**
     * Checks whether an item is selected.
     *
     * @param position the position of the item to be checked
     * @return {@code true} if the item is selected, {@code false} otherwise
     */
    public boolean isItemSelected(int position) {
        return selectedPositions.get(position);
    }

    /**
     * Returns the count of selected items.
     *
     * @return number of items selected
     */
    public int getSelectedItemCount() {
        return selectedPositions.size();
    }

    /**
     * Returns a list of selected item positions.
     *
     * @return list of selected item positions
     */
    public List<Integer> getSelectedItems() {
        final List<Integer> positions = new ArrayList<>(getSelectedItemCount());

        for (int i = 0; i < getSelectedItemCount(); i++) {
            positions.add(selectedPositions.keyAt(i));
        }

        return positions;
    }

    /**
     * Shifts selected item positions in regard to hidden items.
     */
    private void shiftSelectedItems() {
        for (int i = 0; i < getSelectedItemCount(); i++) {
            int position = selectedPositions.keyAt(i);
            int newPosition = position;

            for (int j = 0; j < position; j++) {
                if (hiddenPositions.contains(j)) {
                    newPosition--;
                }
            }

            if (newPosition != position) {
                selectedPositions.delete(position);
                selectedPositions.put(newPosition, true);
            }
        }

        restoreItems();
    }

    /**
     * Removes all selections.
     */
    public void clearItemSelections() {
        selectedPositions.clear();

        notifyDataSetChanged();
    }

    /**
     * Hides an item from the adapter.
     *
     * @param position the position of an item to be hidden
     */
    public void hideItem(int position) {
        final int adjustedPosition = adjustPosition(position);

        int index = Collections.binarySearch(hiddenPositions, adjustedPosition);

        hiddenPositions.add(-index - 1, adjustedPosition);

        notifyItemRemoved(position);
    }

    /**
     * Hides listed items.
     *
     * @param positions the list of items to be hidden
     */
    public void hideItems(List<Integer> positions) {
        for (int position : positions) {
            int index = Collections.binarySearch(hiddenPositions, position);

            hiddenPositions.add(-index - 1, position);
        }

        notifyDataSetChanged();

        if (onItemGestureListener != null) {
            //
            onItemGestureListener.onItemSwipe(RecyclerView.NO_POSITION);
        }
    }

    /**
     * Returns the count of hidden items.
     *
     * @return number of items hidden
     */
    public int getHiddenItemCount() {
        return hiddenPositions.size();
    }

    /**
     * Returns a list of hidden item positions.
     *
     * @return list of hidden item positions
     */
    public List<Integer> getHiddenItems() {
        return hiddenPositions;
    }

    /**
     * Shows all hidden items.
     */
    public void restoreItems() {
        hiddenPositions.clear();

        notifyDataSetChanged();
    }

    /**
     * Returns row ids for positions.
     *
     * @param positions positions to be included
     * @return list of row ids
     */
    public List<Long> getIdsForItems(List<Integer> positions) {
        final List<Long> ids = new ArrayList<>(positions.size());

        for (int position : positions) {
            ids.add(getRawItemId(position));
        }

        return ids;
    }

    /**
     * Adjusts item position by excluding hidden items.
     *
     * @param position raw position of an item to be adjusted
     * @return new item position in adapter
     */
    private int adjustPosition(int position) {
        int index = Collections.binarySearch(hiddenPositions, position);
        if (index < 0) {
            index = -index - 1;
        } else {
            index++;
        }

        int newPosition;
        int lastHiddenPosition;

        do {
            newPosition = position + index;
            lastHiddenPosition = getHiddenItemCount() == index ?
                    -1 : hiddenPositions.get(index);

            index++;
        }
        while (lastHiddenPosition >= 0 && newPosition >= lastHiddenPosition);

        return newPosition;
    }

    @Override
    public void onViewHolderClick(RecyclerView.ViewHolder holder) {
        if (onItemGestureListener != null) {
            onItemGestureListener.onItemClick(holder.getAdapterPosition());
        }
    }

    @Override
    public void onViewHolderLongClick(RecyclerView.ViewHolder holder) {
        if (onItemGestureListener != null) {
            onItemGestureListener.onItemLongClick(holder.getAdapterPosition());
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new ViewHolder of the given type
        switch (viewType) {
            case ItemTypes.TYPE_INFO: {
                final View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_info, parent, false);

                return new InfoViewHolder(view, this);
            }
            case ItemTypes.TYPE_TRANSACTION: {
                final View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_transaction, parent, false);

                return new TransactionViewHolder(view, this);
            }
            case ItemTypes.TYPE_STATE: {
                final View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_state, parent, false);

                return new StateViewHolder(view, this);
            }
            default: {
                // Should never happen
                throw new IllegalArgumentException("Invalid item type: " + viewType);
            }
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // Consider hidden items
        final int adjustedPosition = adjustPosition(position);

        // Choose what item type to bind
        if (cursor != null && cursor.moveToPosition(adjustedPosition)) {
            if (holder instanceof InfoViewHolder)
                onBindInfoViewHolder((InfoViewHolder) holder, adjustedPosition);
            else if (holder instanceof TransactionViewHolder)
                onBindTransactionViewHolder((TransactionViewHolder) holder, adjustedPosition);
            else if (holder instanceof StateViewHolder)
                onBindStateViewHolder((StateViewHolder) holder, adjustedPosition);
        }
    }

    private void onBindInfoViewHolder(InfoViewHolder holder, int position) {
        final boolean isSelected = isItemSelected(position);

        holder.card.setCardBackgroundColor(ContextCompat.getColor(context, isSelected ?
                R.color.selection : R.color.white));

        holder.header.setBackgroundColor(ContextCompat.getColor(context, isSelected ?
                R.color.selection_dark : R.color.primary_light));

        holder.time.setText(TextUtils.getTimeString(context.getApplicationContext(),
                cursor.getLong(cursor.getColumnIndex(DatabaseHelper.TIME_CREATED))));

        holder.account.setText(cursor
                .getString(cursor.getColumnIndex(DatabaseHelper.ACCOUNT)));

        holder.balance.setText(String.format("%.2f", cursor
                .getDouble(cursor.getColumnIndex(DatabaseHelper.BALANCE)) / 1000000));

        holder.sequence.setText(cursor
                .getString(cursor.getColumnIndex(DatabaseHelper.SEQUENCE)));
    }

    private void onBindTransactionViewHolder(TransactionViewHolder holder, int position) {
        final boolean isSelected = isItemSelected(position);

        holder.card.setCardBackgroundColor(ContextCompat.getColor(context, isSelected ?
                R.color.selection : R.color.white));

        holder.header.setBackgroundColor(ContextCompat.getColor(context, isSelected ?
                R.color.selection_dark : R.color.primary_light));

        holder.time.setText(TextUtils.getTimeString(context.getApplicationContext(),
                cursor.getLong(cursor.getColumnIndex(DatabaseHelper.TIME_CREATED))));

        holder.account.setText(cursor
                .getString(cursor.getColumnIndex(DatabaseHelper.ACCOUNT)));

        holder.destination.setText(cursor
                .getString(cursor.getColumnIndex(DatabaseHelper.DESTINATION)));

        holder.amount.setText(String.format("%.2f", cursor
                .getDouble(cursor.getColumnIndex(DatabaseHelper.AMOUNT)) / 1000000));

        holder.fee.setText(String.format("%.6f", cursor
                .getDouble(cursor.getColumnIndex(DatabaseHelper.FEE)) / 1000000));

        holder.sequence.setText(cursor
                .getString(cursor.getColumnIndex(DatabaseHelper.SEQUENCE)));

        holder.message.setText(cursor
                .getString(cursor.getColumnIndex(DatabaseHelper.MESSAGE)));
    }

    private void onBindStateViewHolder(StateViewHolder holder, int position) {
        final boolean isSelected = isItemSelected(position);

        holder.card.setCardBackgroundColor(ContextCompat.getColor(context, isSelected ?
                R.color.selection : R.color.white));

        holder.header.setBackgroundColor(ContextCompat.getColor(context, isSelected ?
                R.color.selection_dark : R.color.primary_light));

        holder.time.setText(TextUtils.getTimeString(context.getApplicationContext(),
                cursor.getLong(cursor.getColumnIndex(DatabaseHelper.TIME_CREATED))));

        holder.peers.setText(cursor
                .getString(cursor.getColumnIndex(DatabaseHelper.PEERS)));

        holder.fee.setText(String.format("%.6f", cursor
                .getDouble(cursor.getColumnIndex(DatabaseHelper.FEE)) / 1000000));
    }

    /**
     * A ViewHolder that describes an item of type: Info.
     */
    public static class InfoViewHolder extends ClickableViewHolder {

        public final CardView card;
        public final TextView header, time, account, balance, sequence;

        public InfoViewHolder(View view, OnViewHolderClickListener listener) {
            super(view, listener);

            card = (CardView) view.findViewById(R.id.item_card);

            header = (TextView) view.findViewById(R.id.item_header);

            time = (TextView) view.findViewById(R.id.item_time);
            account = (TextView) view.findViewById(R.id.item_account);
            balance = (TextView) view.findViewById(R.id.item_balance);
            sequence = (TextView) view.findViewById(R.id.item_sequence);
        }
    }

    /**
     * A ViewHolder that describes an item of type: Transaction.
     */
    public static class TransactionViewHolder extends ClickableViewHolder {

        public final CardView card;
        public final TextView header, time, account, destination, amount, fee, sequence, message;

        public TransactionViewHolder(View view, OnViewHolderClickListener listener) {
            super(view, listener);

            card = (CardView) view.findViewById(R.id.item_card);

            header = (TextView) view.findViewById(R.id.item_header);

            time = (TextView) view.findViewById(R.id.item_time);
            account = (TextView) view.findViewById(R.id.item_account);
            destination = (TextView) view.findViewById(R.id.item_destination);
            amount = (TextView) view.findViewById(R.id.item_amount);
            fee = (TextView) view.findViewById(R.id.item_fee);
            sequence = (TextView) view.findViewById(R.id.item_sequence);
            message = (TextView) view.findViewById(R.id.item_message);
        }
    }

    /**
     * A ViewHolder that describes an item of type: State.
     */
    public static class StateViewHolder extends ClickableViewHolder {

        public final CardView card;
        public final TextView header, time, peers, fee;

        public StateViewHolder(View view, OnViewHolderClickListener listener) {
            super(view, listener);

            card = (CardView) view.findViewById(R.id.item_card);

            header = (TextView) view.findViewById(R.id.item_header);

            time = (TextView) view.findViewById(R.id.item_time);
            peers = (TextView) view.findViewById(R.id.item_peers);
            fee = (TextView) view.findViewById(R.id.item_fee);
        }
    }
}