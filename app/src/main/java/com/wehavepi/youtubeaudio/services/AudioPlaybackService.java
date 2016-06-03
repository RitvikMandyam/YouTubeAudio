package com.wehavepi.youtubeaudio.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.wehavepi.youtubeaudio.R;
import com.wehavepi.youtubeaudio.activities.MainActivity;
import com.wehavepi.youtubeaudio.interfaces.ItemCallback;
import com.wehavepi.youtubeaudio.interfaces.StringCallback;
import com.wehavepi.youtubeaudio.objects.YouTubeItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.huber.youtubeExtractor.YouTubeUriExtractor;
import at.huber.youtubeExtractor.YtFile;

/**
 * Created by ritvik on 30/5/16.
 */
public class AudioPlaybackService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    public MediaPlayer mediaPlayer;
    private final IBinder musicBind = new MusicBinder();
    Pattern next = Pattern.compile("<ul class=\"video\\-list\">\\n\\s+.+\\n+\\s." +
            "+\\n\\s+.+\\n\\s+<a href=\"\\/watch\\?v=([^\"]+)\"[^>]+>[^>]+>\\n" +
            "(.+)\\n\\s+<\\/span>\\n\\s+.+\\n\\s+.+(?=:\\s):\\s(.+)\\n\\s+.+\\n\\s+[^>]+>[^>]+>([^<]+)");
    public String songName;
    public String videoId;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        initMusicPlayer();
        MainActivity.running = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    public void initMusicPlayer() {
        mediaPlayer.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    public void playSong(final String youtubeLink, final StringCallback callback) {
        YouTubeUriExtractor ytEx = new YouTubeUriExtractor(this) {
            @Override
            public void onUrisAvailable(String videoId, String videoTitle, SparseArray<YtFile> ytFiles) {
                if (ytFiles != null) {
                    songName = videoTitle;
                    AudioPlaybackService.this.videoId = videoId;
                    final int itag = 140;
                    mediaPlayer.reset();
                    callback.onCompleted(videoId);
                    try {
                        mediaPlayer.setDataSource(ytFiles.get(itag).getUrl());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mediaPlayer.prepareAsync();
                }
            }
        };

        ytEx.execute(youtubeLink);

        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setTicker(songName)
                .setOngoing(true)
                .setContentTitle("Now playing: ")
                .setContentText(songName);

        Notification not = builder.build();
        startForeground(1, not);
    }

    public void playSong(final String youtubeLink) {
        MainActivity.running = true;
        YouTubeUriExtractor ytEx = new YouTubeUriExtractor(this) {
            @Override
            public void onUrisAvailable(String videoId, String videoTitle, SparseArray<YtFile> ytFiles) {
                if (ytFiles != null) {
                    songName = videoTitle;
                    AudioPlaybackService.this.videoId = videoId;
                    final int itag = 140;
                    mediaPlayer.reset();
                    try {
                        mediaPlayer.setDataSource(ytFiles.get(itag).getUrl());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mediaPlayer.prepareAsync();
                }
            }
        };

        ytEx.execute(youtubeLink);

        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setTicker(songName)
                .setOngoing(true)
                .setContentTitle("Now playing: ")
                .setContentText(songName);

        Notification not = builder.build();
        startForeground(1, not);
    }

    @Override
    public void onCompletion(final MediaPlayer mediaPlayer) {
        getNext(videoId, new ItemCallback() {
            @Override
            public void onReceivedItem(YouTubeItem item) {
                playSong("https://youtube.com/watch?v=" + item.id);
            }
        });
    }

    public class MusicBinder extends Binder {
        public AudioPlaybackService getService() {
            return AudioPlaybackService.this;
        }
    }

    public void getNext(String id, final ItemCallback callback) {
        String url = "https://youtube.com/watch?v=" + id;
        RequestQueue queue = Volley.newRequestQueue(this);
        final List<YouTubeItem> results = new ArrayList<>();

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Matcher mat = next.matcher(response);
                        if (mat.find()) {
                            YouTubeItem item = new YouTubeItem(mat.group(1), mat.group(2), mat.group(3), mat.group(4));
                            callback.onReceivedItem(item);
                        }
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
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public boolean onUnbind(Intent intent){
        MainActivity.running = false;
        mediaPlayer.stop();
        mediaPlayer.release();
        return false;
    }
}
