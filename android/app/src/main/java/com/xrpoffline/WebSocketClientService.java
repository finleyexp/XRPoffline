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

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Background service that handles WebSocket connection.
 * <p/>
 * This service is also used to send/receive messages to/from the server.
 */
public class WebSocketClientService extends Service {

    private static final String TAG = WebSocketClientService.class.getName();

    // Broadcast actions to be called on various WebSocket events
    public static final String ACTION_CONNECTION_ESTABLISHED = "com.xrpoffline.action.CONNECTION_ESTABLISHED";
    public static final String ACTION_MESSAGE = "com.xrpoffline.action.MESSAGE";
    public static final String ACTION_CONNECTION_CLOSED = "com.xrpoffline.action.CONNECTION_CLOSED";
    public static final String ACTION_CONNECTION_ERROR = "com.xrpoffline.action.CONNECTION_ERROR";

    private WebSocketClient client;

    @Override
    public IBinder onBind(Intent intent) {
        return new WebSocketClientBinder(this);
    }

    public static class WebSocketClientBinder extends Binder {
        private final WeakReference<WebSocketClientService> serviceRef;

        public WebSocketClientBinder(WebSocketClientService service) {
            serviceRef = new WeakReference<>(service);
        }

        public WebSocketClientService getService() {
            return serviceRef.get();
        }
    }

    /**
     * Creates a new connection to specific WebSocket server.
     *
     * @param url web address of server
     */
    public void connect(String url) {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            Log.e(TAG, "Invalid URL", e);

            return;
        }

        client = new WebSocketClient(uri) {

            private final LocalBroadcastManager local =
                    LocalBroadcastManager.getInstance(getApplicationContext());

            @Override
            public void onOpen(ServerHandshake handshake) {
                final Intent broadcast = new Intent(ACTION_CONNECTION_ESTABLISHED);

                local.sendBroadcast(broadcast);
            }

            @Override
            public void onMessage(String message) {
                final Intent broadcast = new Intent(ACTION_MESSAGE);
                broadcast.putExtra("message", message);

                local.sendBroadcast(broadcast);
            }

            @Override
            public void onClose(int code, final String reason, boolean remote) {
                final Intent broadcast = new Intent(ACTION_CONNECTION_CLOSED);
                broadcast.putExtra("reason", reason);

                local.sendBroadcast(broadcast);
            }

            @Override
            public void onError(Exception e) {
                final Intent broadcast = new Intent(ACTION_CONNECTION_ERROR);
                broadcast.putExtra("error_message", e.getMessage());

                local.sendBroadcast(broadcast);
            }
        };

        client.connect();
    }

    /**
     * Checks if connection to a WebSocket server is open.
     *
     * @return {@code true} if the connection is open, {@code false} otherwise
     */
    public boolean isConnected() {
        return client != null && client.getConnection().isOpen();
    }

    /**
     * Disconnects client from a WebSocket server
     */
    public void disconnect() {
        if (isConnected()) {
            client.close();
        }
    }

    /**
     * Sends request to a WebSocket Server.
     *
     * @param request message to be sent
     */
    public void send(String request) {
        if (isConnected()) {
            client.send(request);
        } else {
            Log.e(TAG, "Unable to send request, client not connected");
        }
    }
}