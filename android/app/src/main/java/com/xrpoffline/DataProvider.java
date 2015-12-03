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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

/**
 * An implementation of {@code ContentProvider} that executes
 * basic (CRUD) functions on SQLite database tables.
 * <p/>
 * Table name (eventually a table row id) must be appended to the general {@code CONTENT_URI} path.
 */
@SuppressWarnings("ConstantConditions")
public class DataProvider extends ContentProvider {

    private static final String TAG = DataProvider.class.getName();

    /**
     * General content Uri to access this {@code ContentProvider}.
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + TAG);

    private static final int ITEMS = 10;
    private static final int ITEM_ID = 11;

    private static final UriMatcher matcher;

    static {
        matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(TAG, "*", ITEMS);
        matcher.addURI(TAG, "*/#", ITEM_ID);
    }

    private SQLiteDatabase db;

    @Override
    public boolean onCreate() {
        // Provider is being created, open database here
        final DatabaseHelper helper = new DatabaseHelper(getContext());
        db = helper.getWritableDatabase();

        return true;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        String table;

        switch (matcher.match(uri)) {
            case ITEMS: {
                table = uri.getLastPathSegment();

                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown URI: " + uri);
            }
        }

        final long rowId = db.replace(table, null, values);
        if (rowId > 0) {
            getContext().getContentResolver().notifyChange(uri, null);

            return ContentUris.withAppendedId(uri, rowId);
        }

        return null;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values,
                      String selection, String[] selectionArgs) {

        String table;

        switch (matcher.match(uri)) {
            case ITEMS: {
                table = uri.getLastPathSegment();

                break;
            }
            case ITEM_ID: {
                table = uri.getPathSegments().get(uri.getPathSegments().size() - 2);

                final String where = BaseColumns._ID + " = " + uri.getLastPathSegment();
                selection = appendWhere(selection, where);

                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown URI: " + uri);
            }
        }

        final int count = db.update(table, values, selection, selectionArgs);
        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return count;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        String table;

        switch (matcher.match(uri)) {
            case ITEMS: {
                table = uri.getLastPathSegment();

                break;
            }
            case ITEM_ID: {
                table = uri.getPathSegments().get(uri.getPathSegments().size() - 2);

                final String where = BaseColumns._ID + " = " + uri.getLastPathSegment();
                selection = appendWhere(selection, where);

                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown URI: " + uri);
            }
        }

        final int count = db.delete(table, selection, selectionArgs);
        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return count;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        String table;

        switch (matcher.match(uri)) {
            case ITEMS: {
                table = uri.getLastPathSegment();

                break;
            }
            case ITEM_ID: {
                table = uri.getPathSegments().get(uri.getPathSegments().size() - 2);

                final String where = BaseColumns._ID + " = " + uri.getLastPathSegment();
                selection = appendWhere(selection, where);

                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown URI: " + uri);
            }
        }

        final Cursor cursor =
                db.query(table, projection, selection, selectionArgs, null, null, sortOrder);

        if (cursor != null) {
            // Set notification Uri to inform loaders about the data changes
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return cursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        // Ignore MIME types
        return null;
    }

    /**
     * Appends WHERE clause to SQLite statement.
     *
     * @param selection SQLite selection
     * @param where     WHERE statement to append
     * @return SQLite statement with appended where clause
     */
    private String appendWhere(String selection, String where) {
        if (selection != null && !selection.isEmpty()) {
            where += " and " + selection;
        }

        return where;
    }
}