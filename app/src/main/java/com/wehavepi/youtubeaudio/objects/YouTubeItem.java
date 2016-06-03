package com.wehavepi.youtubeaudio.objects;

import android.text.Html;

/**
 * Created by ritvik on 1/6/16.
 */
public class YouTubeItem {
    public String title;
    public String id;
    public String duration;
    public String uploader;
    public String thumbnail;

    public YouTubeItem(String id, String title, String duration, String uploader) {
        this.id = id;
        this.title = Html.fromHtml(title).toString();
        this.duration = duration.substring(0, duration.length() - 1);
        this.uploader = uploader;
        this.thumbnail = "https://i.ytimg.com/vi/" + id + "/hqdefault.jpg?custom=true&w=196&h=110&jpgq=80";
    }
}
