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

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Background service used to parse JSON object received from Ripple server.
 * <p/>
 * JSON will be parsed to {@link ContentValues} that can be inserted into SQLite database.
 */
public class ParserService extends IntentService {

    private static final String TAG = ParserService.class.getName();

    // Broadcast action to be called on parsing finished
    public static final String ACTION_PARSE_FINISHED = "com.xrpoffline.action.PARSE_FINISHED";

    public ParserService() {
        super(TAG);
    }

    @Override
    public void onHandleIntent(Intent intent) {
        String errorMessage = null;
        ContentValues values = null;

        final String text = intent.getStringExtra("text");
        if (text != null) {
            try {
                try {
                    final JSONObject json = new JSONObject(text);

                    // Check for an possible error first
                    final String error = json.optString("error_message", null);
                    if (error != null) {
                        throw new RuntimeException(error);
                    } else {
                        values = new ContentValues();
                    }

                    final JSONObject result = json.getJSONObject("result");

                    // Response type: Info
                    final JSONObject accountData = result.optJSONObject("account_data");
                    if (accountData != null) {
                        final String account = accountData.getString("Account");
                        final String balance = accountData.getString("Balance");
                        final String sequence = accountData.getString("Sequence");

                        values.put(DatabaseHelper.TYPE, ItemTypes.TYPE_INFO);
                        values.put(DatabaseHelper.ACCOUNT, account);
                        values.put(DatabaseHelper.BALANCE, balance);
                        values.put(DatabaseHelper.SEQUENCE, sequence);
                    } else {
                        // Response type: Transaction
                        final String engineResult = result.optString("engine_result", null);
                        if (engineResult != null) {
                            final String message = result.getString("engine_result_message");
                            final JSONObject tx = result.getJSONObject("tx_json");

                            final String account = tx.getString("Account");
                            final String destination = tx.getString("Destination");
                            final int amount = tx.getInt("Amount");
                            final int fee = tx.getInt("Fee");
                            final int sequence = tx.getInt("Sequence");

                            values.put(DatabaseHelper.TYPE, ItemTypes.TYPE_TRANSACTION);
                            values.put(DatabaseHelper.ACCOUNT, account);
                            values.put(DatabaseHelper.DESTINATION, destination);
                            values.put(DatabaseHelper.AMOUNT, amount);
                            values.put(DatabaseHelper.FEE, fee);
                            values.put(DatabaseHelper.SEQUENCE, sequence);
                            values.put(DatabaseHelper.MESSAGE, message);
                        } else {
                            // Response type: State
                            final JSONObject state = result.optJSONObject("state");
                            if (state != null) {
                                final int loadBase = state.getInt("load_base");
                                final int loadFactor = state.getInt("load_factor");
                                final int peers = state.getInt("peers");

                                final JSONObject validatedLedger =
                                        state.getJSONObject("validated_ledger");

                                final int baseFee = validatedLedger.getInt("base_fee");

                                final double fee = baseFee * loadFactor / loadBase;

                                values.put(DatabaseHelper.TYPE, ItemTypes.TYPE_STATE);
                                values.put(DatabaseHelper.PEERS, peers);
                                values.put(DatabaseHelper.FEE, fee);
                            } else {
                                throw new RuntimeException("Command unsupported");
                            }
                        }
                    }
                } catch (JSONException e) {
                    throw new RuntimeException("Invalid response", e);
                }
            } catch (RuntimeException e) {
                errorMessage = e.getMessage();
            }

            if (values != null) {
                values.put(DatabaseHelper.TIME_CREATED, System.currentTimeMillis());
            }
        }

        final Intent broadcast = new Intent(ACTION_PARSE_FINISHED);

        if (errorMessage != null) {
            // Notify error occurred
            broadcast.putExtra("error_message", errorMessage);
        } else if (values != null) {
            // Broadcast parser result
            broadcast.putExtra("values", values);
        }

        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcast);
    }
}