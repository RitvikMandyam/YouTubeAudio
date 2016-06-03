package com.wehavepi.youtubeaudio.interfaces;

import com.wehavepi.youtubeaudio.objects.YouTubeItem;

import java.util.List;

/**
 * Created by ritvik on 1/6/16.
 */
public interface ResultsCallback {
    void onReceivedResults(List<YouTubeItem> results);
}
