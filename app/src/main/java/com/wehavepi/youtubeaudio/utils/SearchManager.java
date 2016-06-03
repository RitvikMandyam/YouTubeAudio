package com.wehavepi.youtubeaudio.utils;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.wehavepi.youtubeaudio.interfaces.ResultsCallback;
import com.wehavepi.youtubeaudio.objects.YouTubeItem;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ritvik on 31/5/16.
 */
public class SearchManager {
    Context context;
    ResultsCallback callback;
    Pattern ids = Pattern.compile("<h3[^>]+><a href=\"\\/watch\\?v=([^\"]+)\"[^>]+>([^>]+)<\\/a>[^>]+>.+(?=:\\s):\\s([^<]+)<\\/span><\\/h3><div[^>]+><[^>]+>([^<]+)");

    public SearchManager (Context context, ResultsCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    public List<YouTubeItem> search(String query) {
        String url = "";
        try {
            url = "https://www.youtube.com/results"
                    + "?search_query=" + URLEncoder.encode(query, "UTF-8")
                    + "&page=" + Integer.toString(1)
                    + "&filters=" + "video";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        RequestQueue queue = Volley.newRequestQueue(context);
        final List<YouTubeItem> results = new ArrayList<>();

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Matcher mIds = ids.matcher(response);
                        while (mIds.find()) {
                            results.add(new YouTubeItem(mIds.group(1), mIds.group(2), mIds.group(3), mIds.group(4)));
                        }
                        callback.onReceivedResults(results);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            public Map<String, String> getHeaders(){
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.115 Safari/537.36");
                return headers;
            }
        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
        return results;
    }
}
