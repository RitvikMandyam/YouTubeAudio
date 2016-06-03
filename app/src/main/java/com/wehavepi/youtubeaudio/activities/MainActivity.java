package com.wehavepi.youtubeaudio.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.wehavepi.youtubeaudio.BR;
import com.wehavepi.youtubeaudio.R;
import com.wehavepi.youtubeaudio.adapters.DataBindingAdapter;
import com.wehavepi.youtubeaudio.interfaces.ItemCallback;
import com.wehavepi.youtubeaudio.interfaces.ResultsCallback;
import com.wehavepi.youtubeaudio.interfaces.StringCallback;
import com.wehavepi.youtubeaudio.objects.YouTubeItem;
import com.wehavepi.youtubeaudio.services.AudioPlaybackService;
import com.wehavepi.youtubeaudio.utils.SearchManager;

import java.util.ArrayList;
import java.util.List;

import at.huber.youtubeExtractor.YouTubeUriExtractor;
import at.huber.youtubeExtractor.YtFile;

public class MainActivity extends AppCompatActivity {
    private AudioPlaybackService aps;
    private Intent playIntent;
    private boolean bound = false;
    public static boolean running = false;

    private List<YouTubeItem> results;
    private RecyclerView view;

    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, AudioPlaybackService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        view = (RecyclerView) findViewById(R.id.results_list);

        results = new ArrayList<>();

        DataBindingAdapter adapter = new DataBindingAdapter(results, R.layout.result_layout, BR.result);

        view.setLayoutManager(new LinearLayoutManager(this));
        view.setAdapter(adapter);

        if (running) {
            findViewById(R.id.play_button).setVisibility(View.VISIBLE);
            findViewById(R.id.skip_button).setVisibility(View.VISIBLE);
        }
    }

    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioPlaybackService.MusicBinder binder = (AudioPlaybackService.MusicBinder)service;
            //get service
            aps = binder.getService();
            findViewById(R.id.get_url_button).setEnabled(true);
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    public void getURL(final View v) {
        String youtubeLink = "http://youtube.com/watch?v=" + v.getTag().toString();

        findViewById(R.id.play_prog).setVisibility(View.VISIBLE);
        findViewById(R.id.play_button).setVisibility(View.INVISIBLE);
        findViewById(R.id.skip_button).setVisibility(View.INVISIBLE);

        aps.playSong(youtubeLink, new StringCallback() {
            @Override
            public void onCompleted(final String result) {
                findViewById(R.id.play_prog).setVisibility(View.GONE);
                findViewById(R.id.play_button).setVisibility(View.VISIBLE);
                findViewById(R.id.skip_button).setVisibility(View.VISIBLE);
            }
        });
    }

    public void search(View v) {
        findViewById(R.id.spinny).setVisibility(View.VISIBLE);
        SearchManager searchManager = new SearchManager(this, new ResultsCallback() {
            @Override
            public void onReceivedResults(List<YouTubeItem> response) {
                results = response;
                DataBindingAdapter adapter = new DataBindingAdapter(response, R.layout.result_layout, BR.result);
                findViewById(R.id.spinny).setVisibility(View.GONE);
                view.setAdapter(adapter);
            }
        });
        searchManager.search(((EditText) findViewById(R.id.url_edit_text)).getText().toString());
    }

    public void skip(View v) {
        findViewById(R.id.play_prog).setVisibility(View.VISIBLE);
        findViewById(R.id.play_button).setVisibility(View.INVISIBLE);
        findViewById(R.id.skip_button).setVisibility(View.INVISIBLE);
        aps.getNext(aps.videoId, new ItemCallback() {
            @Override
            public void onReceivedItem(YouTubeItem item) {
                aps.playSong("https://youtube.com/watch?v=" + item.id, new StringCallback() {
                    @Override
                    public void onCompleted(String result) {
                        findViewById(R.id.play_prog).setVisibility(View.GONE);
                        findViewById(R.id.play_button).setVisibility(View.VISIBLE);
                        findViewById(R.id.skip_button).setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    public void toggle(View v) {
        ImageButton button = (ImageButton) v;
        if (aps.mediaPlayer.isPlaying()) {
            aps.mediaPlayer.pause();
            button.setImageResource(android.R.drawable.ic_media_play);
        } else {
            aps.mediaPlayer.start();
            button.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        aps = null;
        super.onDestroy();
    }
}
