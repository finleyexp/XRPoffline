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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that manages the main log list.
 */
public class MainFragment extends Fragment implements ItemAdapter.OnItemGestureListener,
        LoaderManager.LoaderCallbacks<Cursor>, ActionMode.Callback {

    /**
     * Interface definition for a callback to be invoked on some UI interactions.
     */
    public interface Callback {

        /**
         * Called when the menu item was clicked by user.
         *
         * @param id menu item id
         * @return {@code false} to allow normal menu processing to proceed,
         * {@code true} to consume event
         */
        boolean onMenuItemClick(int id);

        /**
         * Called when the {@link FloatingActionButton} was clicked.
         */
        void onFabClick();

        /**
         * Checks if the WebSocket client is connected to server.
         *
         * @return {@code true} if the connection is open, {@code false} otherwise
         */
        boolean isWebSocketClientConnected();
    }

    private ItemAdapter adapter;

    private ActionMode actionMode;

    private FloatingActionButton fab;
    private Snackbar snackbar;

    private Callback callback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            callback = (Callback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.getClass().getName() + " must implement "
                    + Callback.class.getName());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_main, container, false);

        fab = (FloatingActionButton) root.findViewById(R.id.main_fab);
        ViewCompat.setAlpha(fab, 0f);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                callback.onFabClick();
            }
        });

        adapter = new ItemAdapter(getContext(), this);

        if (savedInstanceState != null) {
            final List<Integer> hiddenItems =
                    savedInstanceState.getIntegerArrayList("hidden_items");

            if (hiddenItems != null) {
                adapter.hideItems(hiddenItems);
            }

            final List<Integer> selectedItems =
                    savedInstanceState.getIntegerArrayList("selected_items");

            if (selectedItems != null) {
                adapter.selectItems(selectedItems);

                actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(this);
                notifySelectionChanged(adapter.getSelectedItemCount());
            }
        }

        final RecyclerView list = (LazyRecyclerView) root.findViewById(R.id.main_list);
        list.setLayoutManager(new ExtraLayoutManager(getContext()));
        list.getItemAnimator().setRemoveDuration(0);
        list.setAdapter(adapter);

        final ItemTouchHelper itemTouchHelper =
                new ItemTouchHelper(new SimpleItemTouchHelperCallback(adapter));

        itemTouchHelper.attachToRecyclerView(list);

        final Bundle args = new Bundle();
        // Sort logs by date
        args.putString("sort_order", DatabaseHelper.TIME_CREATED + " desc");

        getActivity().getSupportLoaderManager().initLoader(0, args, this);

        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (adapter != null) {
            if (adapter.getHiddenItemCount() > 0) {
                outState.putIntegerArrayList("hidden_items",
                        (ArrayList<Integer>) adapter.getHiddenItems());
            }

            if (adapter.getSelectedItemCount() > 0) {
                outState.putIntegerArrayList("selected_items",
                        (ArrayList<Integer>) adapter.getSelectedItems());
            }
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        if (snackbar != null) {
            snackbar.setCallback(null);
        }

        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        final MenuItem item = menu.findItem(R.id.action_connect);

        if (callback.isWebSocketClientConnected()) {
            item.setIcon(R.drawable.ic_status_connected);
            item.setTitle(getString(R.string.action_disconnect));

            if (actionMode == null) {
                ViewCompat.setAlpha(fab, 1f);
                fab.show();

                if (snackbar != null && snackbar.isShown()) {
                    ViewCompat.setTranslationY(fab, -snackbar.getView().getHeight());
                }
            }
        } else {
            item.setIcon(R.drawable.ic_status_disconnected);
            item.setTitle(getString(R.string.action_connect));

            fab.hide();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return callback.onMenuItemClick(item.getItemId());
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.menu_action, menu);

        // FloatingActionButton will be disabled in action mode
        fab.hide();

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select_all: {
                adapter.selectAllItems();

                notifySelectionChanged(adapter.getSelectedItemCount());

                return true;
            }
            case R.id.action_delete: {
                adapter.hideItems(adapter.getSelectedItems());

                mode.finish();

                return true;
            }
            case android.R.id.home: {
                mode.finish();

                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        adapter.clearItemSelections();

        if (callback.isWebSocketClientConnected()) {
            fab.show();
        }

        actionMode = null;
    }

    @Override
    public void onItemClick(int position) {
        if (actionMode != null) {
            adapter.toggleItemSelection(position);

            final int count = adapter.getSelectedItemCount();
            if (count == 0) {
                actionMode.finish();
            } else {
                notifySelectionChanged(count);
            }
        }
    }

    @Override
    public void onItemLongClick(int position) {
        if (actionMode == null) {
            actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(this);
        }

        onItemClick(position);
    }

    @Override
    public void onItemSwipe(int position) {
        notifyPendingDismisses(adapter.getHiddenItemCount());
    }

    /**
     * Inserts a new log (item) to the main list.
     *
     * @param values log attributes
     */
    public void insertLog(ContentValues values) {
        final Uri uri = Uri.withAppendedPath(DataProvider.CONTENT_URI, DatabaseHelper.TABLE_LOGS);
        getContext().getContentResolver().insert(uri, values);
    }

    /**
     * Delete logs (items) from main list.
     *
     * @param ids row ids o items to be deleted
     */
    public void deleteLogs(List<Long> ids) {
        final String selection = TextUtils.makeWhereClause(ids);

        final Uri uri = Uri.withAppendedPath(DataProvider.CONTENT_URI, DatabaseHelper.TABLE_LOGS);
        getContext().getContentResolver().delete(uri, selection, null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final Uri uri = Uri.withAppendedPath(DataProvider.CONTENT_URI, DatabaseHelper.TABLE_LOGS);

        if (args != null) {
            return new CursorLoader(getContext(), uri, args.getStringArray("projection"),
                    args.getString("selection"), args.getStringArray("selection_args"),
                    args.getString("sort_order"));
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    /**
     * Sets action mode title to number that represents a count of selected items.
     *
     * @param count number of items selected
     */
    private void notifySelectionChanged(int count) {
        actionMode.setTitle(String.valueOf(count));
    }

    /**
     * Shows a {@link Snackbar} with removed items count.
     * <p/>
     * Items will be deleted from database on {@code Snackbar} dismiss.
     *
     * @param count number of items deleted
     */
    private void notifyPendingDismisses(int count) {
        if (snackbar == null || !snackbar.isShown()) {
            snackbar = Snackbar.make(fab,
                    getResources().getQuantityString(R.plurals.notify_log_deleted, count, count),
                    Snackbar.LENGTH_LONG)

                    .setAction(R.string.action_undo, new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            adapter.restoreItems();
                        }
                    })
                    .setCallback(new Snackbar.Callback() {

                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            super.onDismissed(snackbar, event);

                            deleteLogs(adapter.getIdsForItems(adapter.getHiddenItems()));
                        }
                    });
        } else {
            final View text = snackbar.getView().findViewById(R.id.snackbar_text);
            ViewCompat.setAlpha(text, 0f);

            snackbar.setText(getResources().getQuantityString(R.plurals.notify_log_deleted,
                    count, count));

            ViewCompat.animate(text).alpha(1f).setDuration(250);
        }

        snackbar.show();
    }
}