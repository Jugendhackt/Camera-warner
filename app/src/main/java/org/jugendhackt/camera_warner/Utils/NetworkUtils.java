/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jugendhackt.camera_warner.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * These utilities will be used to communicate with the network.
 * They also contain the URL the data is stored at.
 */
public class NetworkUtils {

    public static final MediaType GeoJSON = MediaType.parse("application/vnd.geo+json");
    public static final MediaType OverpassQL = MediaType.parse("application/overpassql");

    public static String getResponseWithPost(String url, MediaType mediaType, String requestBody)
    {
        RequestBody body = RequestBody.create(mediaType, requestBody);

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(body)
                .build();

        return fetchDataFromRequest(request);
    }

    private static String fetchDataFromRequest(Request request)
    {
        OkHttpClient client = new OkHttpClient();

        String returnVale = null;
        try {
            returnVale =  client.newCall(request).execute().body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnVale;
    }

    public static String getResponseFromURL(String url)
    {
        return getResponseFromURL(url, null);
    }

    public static String getResponseFromURL(String url, MediaType accept)
    {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder()
                .url(url);

        if(accept!=null)
        {
            builder.header("Accept", accept.toString());
        }

        return fetchDataFromRequest(builder.build());
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param urlStr The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}