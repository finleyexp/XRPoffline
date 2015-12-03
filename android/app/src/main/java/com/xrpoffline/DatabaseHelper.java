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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * SQLiteOpenHelper that manages table creation for this application
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "xrpoffline";
    private static final int DATABASE_VERSION = 1;

    // Table name
    public static final String TABLE_LOGS = "logs";

    // Table columns
    public static final String TYPE = "type";
    public static final String ACCOUNT = "account";
    public static final String DESTINATION = "destination";
    public static final String BALANCE = "balance";
    public static final String AMOUNT = "amount";
    public static final String PEERS = "peers";
    public static final String FEE = "fee";
    public static final String SEQUENCE = "sequence";
    public static final String MESSAGE = "message";
    public static final String TIME_CREATED = "time_created";

    // SQLite statement used to create table
    private static final String CREATE_TABLE_LOGS =
            "create table " + TABLE_LOGS + " ("
                    + BaseColumns._ID + " integer primary key autoincrement, "
                    + TYPE + " integer, "
                    + ACCOUNT + " text, "
                    + DESTINATION + " text, "
                    + BALANCE + " real, "
                    + AMOUNT + " real, "
                    + PEERS + " integer, "
                    + FEE + " real, "
                    + SEQUENCE + " integer, "
                    + MESSAGE + " text, "
                    + TIME_CREATED + " integer);";

    // SQLite statement used to drop existing table
    private static final String DROP_TABLE_LOGS = "drop table if exists " + TABLE_LOGS;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Database is created for the first time, create table here
        db.execSQL(CREATE_TABLE_LOGS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Database needs to be upgraded, drop existing table and create a new one
        db.execSQL(DROP_TABLE_LOGS);

        onCreate(db);
    }
}