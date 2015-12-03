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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Fragment that displays a HTML file to user.
 * <p/>
 * HTML file should be placed in the assets folder.
 */
public class HtmlReaderFragment extends Fragment {

    private static final String TAG = HtmlReaderFragment.class.getName();

    private TextView htmlView;

    /**
     * Creates new instance of {@code HtmlReaderFragment} by specifying the name
     * of the HTML file to be displayed.
     *
     * @param filename the name of the file to be displayed.
     * @return a new instance of {@code HtmlReaderFragment}
     */
    public static HtmlReaderFragment newInstance(String filename) {
        final HtmlReaderFragment fragment = new HtmlReaderFragment();

        if (filename != null && !filename.isEmpty()) {
            final Bundle bundle = new Bundle();
            bundle.putString("filename", filename);

            fragment.setArguments(bundle);
        }

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_htmlreader, container, false);

        htmlView = (TextView) root.findViewById(R.id.htmlreader_html);

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Bundle arguments = getArguments();
        if (arguments != null) {

            String html = null;
            BufferedReader reader = null;

            // Try to open and read the file specified
            try {
                final StringBuilder buffer = new StringBuilder();

                final InputStream is = getActivity().getAssets()
                        .open(arguments.getString("filename"));

                reader = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                html = buffer.toString();
            } catch (IOException e) {
                Log.e(TAG, "Error reading file", e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error closing file", e);
                    }
                }
            }

            // Display the result to user
            if (html != null) {
                htmlView.setText(Html.fromHtml(html));
            }
        }
    }
}