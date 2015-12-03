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

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements
        WebSocketClientFragment.OnWebSocketListener,
        QrScannerFragment.OnScanFinishedListener,
        MainFragment.Callback {

    /**
     * URL of the Ripple WebSocket server.
     */
    public static final String URL = "wss://s1.ripple.com:51233";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        final FragmentManager fm = getSupportFragmentManager();
        fm.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {

            @Override
            public void onBackStackChanged() {
                adjustActionBar();
            }
        });

        if (savedInstanceState == null) {
            final MainFragment main = new MainFragment();

            fm.beginTransaction()
                    .add(R.id.main_layout, main, "main")
                    .commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        final IntentFilter globalFilter = new IntentFilter();
        globalFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        registerReceiver(globalReceiver, globalFilter);

        final IntentFilter localFilter = new IntentFilter();
        localFilter.addAction(ParserService.ACTION_PARSE_FINISHED);

        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(localReceiver, localFilter);

        adjustActionBar();
    }

    @Override
    public void onPause() {
        super.onPause();

        unregisterReceiver(globalReceiver);

        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(localReceiver);
    }

    private final BroadcastReceiver globalReceiver = new BroadcastReceiver() {

        boolean isConnectedToNetwork;

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case ConnectivityManager.CONNECTIVITY_ACTION: {
                    final boolean connected = NetUtils
                            .isConnectedToNetwork(getApplicationContext());

                    if (connected != isConnectedToNetwork) {
                        isConnectedToNetwork = connected;
                        if (isConnectedToNetwork) {
                            WebSocketClientFragment client = getWebSocketClientFragment();
                            if (client == null) {
                                createWebSocketClientFragment(URL);
                            } else if (!client.isConnected() && !client.isForceDisconnected()) {
                                client.connect(URL);
                            }
                        }
                    }

                    break;
                }
            }
        }
    };

    private final BroadcastReceiver localReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case ParserService.ACTION_PARSE_FINISHED: {
                    final String errorMessage = intent.getStringExtra("error_message");
                    if (errorMessage != null) {
                        Toast.makeText(getApplicationContext(), errorMessage,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        final ContentValues values = intent.getParcelableExtra("values");
                        if (values != null) {
                            final MainFragment main = (MainFragment) getSupportFragmentManager()
                                    .findFragmentByTag("main");

                            if (main != null) {
                                main.insertLog(values);
                            }
                        }
                    }

                    break;
                }
            }
        }
    };

    private void createWebSocketClientFragment(String url) {
        final WebSocketClientFragment client = WebSocketClientFragment.newInstance(url);

        getSupportFragmentManager().beginTransaction()
                .add(client, "client")
                .commit();
    }

    private WebSocketClientFragment getWebSocketClientFragment() {
        return (WebSocketClientFragment) getSupportFragmentManager().findFragmentByTag("client");
    }

    @Override
    public boolean isWebSocketClientConnected() {
        final WebSocketClientFragment client = getWebSocketClientFragment();

        return client != null && client.isConnected();
    }

    @Override
    public void onConnectionEstablished() {
        invalidateOptionsMenu();
    }

    @Override
    public void onMessage(String message) {
        if (message != null) {
            final Intent parser = new Intent(getApplicationContext(), ParserService.class);
            parser.putExtra("text", message);

            getApplicationContext().startService(parser);
        }
    }

    @Override
    public void onConnectionClosed(String reason) {
        invalidateOptionsMenu();
    }

    @Override
    public void onConnectionError(String errorMessage) {
    }

    @Override
    public void onScanFinished(String content) {
        getSupportFragmentManager().popBackStack();

        final WebSocketClientFragment client = getWebSocketClientFragment();
        if (client != null) {
            client.send(content);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        getSupportFragmentManager().popBackStack();

        return true;
    }

    @Override
    public boolean onMenuItemClick(int id) {
        switch (id) {
            case R.id.action_connect: {
                WebSocketClientFragment client = getWebSocketClientFragment();
                if (client != null && client.isConnected()) {
                    client.disconnect();
                } else {
                    final boolean connected = NetUtils
                            .isConnectedToNetwork(getApplicationContext());

                    if (connected) {
                        if (client == null) {
                            createWebSocketClientFragment(URL);
                        } else {
                            client.connect(URL);
                        }
                    }
                }

                return true;
            }
            case R.id.action_about: {
                final HtmlReaderFragment html = HtmlReaderFragment.newInstance("about.html");

                // BackStack name will be displayed as ActionBar title when Fragment becomes visible
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_layout, html)
                        .addToBackStack(getString(R.string.action_about))
                        .commit();

                return true;
            }
            case R.id.action_licenses: {
                final HtmlReaderFragment html = HtmlReaderFragment.newInstance("licenses.html");

                // BackStack name will be displayed as ActionBar title when Fragment becomes visible
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_layout, html)
                        .addToBackStack(getString(R.string.action_licenses))
                        .commit();

                return true;
            }
        }

        return false;
    }

    @Override
    public void onFabClick() {
        final QrScannerFragment scanner = QrScannerFragment.newInstance("command");

        // BackStack name will be displayed as ActionBar title when Fragment becomes visible
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_layout, scanner, "scanner")
                .addToBackStack(getString(R.string.action_scanner))
                .commit();
    }

    private void adjustActionBar() {
        final ActionBar bar = getSupportActionBar();
        if (bar != null) {
            final int count = getSupportFragmentManager().getBackStackEntryCount();
            if (count > 0) {
                bar.setTitle(getSupportFragmentManager().getBackStackEntryAt(count - 1).getName());
                bar.setDisplayHomeAsUpEnabled(true);
            } else {
                bar.setTitle(getString(R.string.app_name));
                bar.setDisplayHomeAsUpEnabled(false);
            }
        }
    }
}