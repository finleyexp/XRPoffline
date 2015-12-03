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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Retaining wrapper fragment that binds {@link WebSocketClientService}.
 * <p/>
 * Service will be unbound in {@code onDestroy()} method,
 * so it can be destroyed automatically.
 */
public class WebSocketClientFragment extends Fragment implements ServiceConnection {

    /**
     * Interface definition for a callback to be invoked on WebSocket events.
     */
    public interface OnWebSocketListener {

        /**
         * Called when the connection to a WebSocket server was successfully established.
         */
        void onConnectionEstablished();

        /**
         * Called when a WebSocket client received a response from server.
         *
         * @param message response from server
         */
        void onMessage(String message);

        /**
         * Called when the connection to a WebSocket server was closed.
         *
         * @param reason why was the connection closed
         */
        void onConnectionClosed(String reason);

        /**
         * Called when a WebSocket connection error occurred
         *
         * @param errorMessage description of error occurred
         */
        void onConnectionError(String errorMessage);
    }

    private WebSocketClientService client;
    private boolean isForceDisconnected;

    private OnWebSocketListener onWebSocketListener;

    /**
     * Creates new instance of {@code WebSocketClientFragment} by specifying web address
     * of WebSocket server.
     *
     * @param url web address of WebSocket server
     * @return a new instance of {@code WebSocketClientFragment}
     */
    public static WebSocketClientFragment newInstance(String url) {
        final WebSocketClientFragment fragment = new WebSocketClientFragment();

        if (url != null && !url.isEmpty()) {
            final Bundle arguments = new Bundle();
            arguments.putString("url", url);

            fragment.setArguments(arguments);
        }

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            onWebSocketListener = (OnWebSocketListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.getClass().getName() + " must implement "
                    + OnWebSocketListener.class.getName());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        // Fragment is being created, bind WebSocket client service
        final Intent client = new Intent(getContext().getApplicationContext(),
                WebSocketClientService.class);

        getContext().getApplicationContext().bindService(client, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Register receiver to be notified of WebSocket client service events
        final IntentFilter localFilter = new IntentFilter();

        localFilter.addAction(WebSocketClientService.ACTION_CONNECTION_ESTABLISHED);
        localFilter.addAction(WebSocketClientService.ACTION_MESSAGE);
        localFilter.addAction(WebSocketClientService.ACTION_CONNECTION_CLOSED);
        localFilter.addAction(WebSocketClientService.ACTION_CONNECTION_ERROR);

        LocalBroadcastManager.getInstance(getContext().getApplicationContext())
                .registerReceiver(localReceiver, localFilter);
    }

    @Override
    public void onPause() {
        super.onPause();

        // No need to be notified of WebSocket client service events while the fragment is paused
        LocalBroadcastManager.getInstance(getContext().getApplicationContext())
                .unregisterReceiver(localReceiver);
    }

    @Override
    public void onDestroy() {
        disconnect();

        // Unbind WebSocket client service here, so it can be destroyed properly
        getContext().getApplicationContext().unbindService(this);

        super.onDestroy();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        client = ((WebSocketClientService.WebSocketClientBinder) binder).getService();

        // WebSocket client service is bounded, create connection to specified server
        final Bundle arguments = getArguments();
        if (arguments != null) {
            final String url = arguments.getString("url");

            connect(url);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        client = null;
    }

    /**
     * Creates a new connection to specific WebSocket server.
     *
     * @param url web address of server
     */
    public void connect(String url) {
        if (client != null) {
            client.connect(url);

            isForceDisconnected = false;
        }
    }

    /**
     * Checks if the connection to a WebSocket server exists.
     *
     * @return {@code true} if the connection exists, {@code false} otherwise
     */
    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    /**
     * Disconnects client from a WebSocket server
     */
    public void disconnect() {
        if (client != null) {
            client.disconnect();

            isForceDisconnected = true;
        }
    }

    /**
     * Checks if the connection to a WebSocket server was closed by user action.
     *
     * @return {@code true} if the connection was closed by user, {@code false} otherwise
     */
    public boolean isForceDisconnected() {
        return isForceDisconnected;
    }

    /**
     * Sends request to a WebSocket Server.
     *
     * @param request message to be sent
     */
    public void send(String request) {
        if (client != null) {
            client.send(request);
        }
    }

    // Use this receiver to receive WebSocket client service events and pass them to activity
    private final BroadcastReceiver localReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case WebSocketClientService.ACTION_CONNECTION_ESTABLISHED: {
                    onWebSocketListener.onConnectionEstablished();
                }
                case WebSocketClientService.ACTION_MESSAGE: {
                    final String message = intent.getStringExtra("message");

                    onWebSocketListener.onMessage(message);
                }
                case WebSocketClientService.ACTION_CONNECTION_CLOSED: {
                    final String reason = intent.getStringExtra("reason");

                    onWebSocketListener.onConnectionClosed(reason);
                }
                case WebSocketClientService.ACTION_CONNECTION_ERROR: {
                    final String errorMessage = intent.getStringExtra("error_message");

                    onWebSocketListener.onConnectionError(errorMessage);
                }
            }
        }
    };
}