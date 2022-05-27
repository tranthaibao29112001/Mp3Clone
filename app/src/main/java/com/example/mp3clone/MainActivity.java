package com.example.mp3clone;

import static com.example.mp3clone.MyService.ACTION_NEXT;
import static com.example.mp3clone.MyService.ACTION_PAUSE;
import static com.example.mp3clone.MyService.ACTION_PREVIOUS;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements SongItemClickListener {
    private ArrayList<Song> songArrayList = new ArrayList<>();
    private RecyclerView songRecyclerView;
    private SongRecyclerViewAdapter songRecyclerViewAdapter;
    private MyViewModel myViewModel;
    private SongPlayer songPlayer ;
    private ImageView play_pauseBtn, nextBtn, thumbnail;
    private TextView titleTxt, singerTxt;
    private RelativeLayout relativeLayout;
    private ProgressBar progressBar;
    static boolean active = true;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int action = intent.getIntExtra("action",3);
            handleAction(action);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        active = true;
        songPlayer = new SongPlayer();
        songRecyclerView = findViewById(R.id.songRecyclerView);
        play_pauseBtn = findViewById(R.id.play_pauseBtn);
        nextBtn = findViewById(R.id.nextBtn);
        titleTxt = findViewById(R.id.titleTxt);
        singerTxt = findViewById(R.id.singerTxt);
        thumbnail =findViewById(R.id.thumbnailImage);
        relativeLayout = findViewById(R.id.bottomRelativeLayout);
        progressBar = findViewById(R.id.progressBar);

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,new IntentFilter("send_action_to_activity"));

        songRecyclerViewAdapter = new SongRecyclerViewAdapter(this,songArrayList);
        songRecyclerView.setAdapter(songRecyclerViewAdapter);
        songRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        myViewModel = new ViewModelProvider(this).get(MyViewModel.class);
        myViewModel.getListSongFromDatabase().observe(this, new Observer<ArrayList<Song>>() {
            @Override
            public void onChanged(ArrayList<Song> songs) {
                songPlayer.setSongArrayList(songs);
                songArrayList = (ArrayList<Song>) songs.clone();
                songRecyclerViewAdapter.setData(songs);
            }
        });

        play_pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(songPlayer.isPlaying()){
                    play_pauseBtn.setImageDrawable(getDrawable(R.drawable.ic_play));
                }
                else{
                    play_pauseBtn.setImageDrawable(getDrawable(R.drawable.ic_pause));
                }
                sendActionToService(ACTION_PAUSE);
            }
        });
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play_pauseBtn.setImageDrawable(getDrawable(R.drawable.ic_pause));
                sendActionToService(ACTION_NEXT);
                updateBottomSongLayout(songPlayer.getSong());
            }
        });

        Handler handler = new Handler(){
            @SuppressLint("HandlerLeak")
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what){
                    case 1000:{
                        progressBar.setMax(SongPlayer.myMedia.getDuration());
                        progressBar.setProgress(msg.arg1);
                        if(msg.arg1 >= SongPlayer.myMedia.getDuration() - 1000){
                            songPlayer.nextSong();
                            updateBottomSongLayout(songPlayer.getSong());
                        }
                        break;
                    }
                }
            }
        };
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Message message = new Message();
                    message.what = 1000;
                    if(SongPlayer.myMedia !=null){
                        message.arg1 = SongPlayer.myMedia.getCurrentPosition();
                        handler.sendMessage(message);
                    }

                }
            }
        });
        thread.start();

        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SongActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("song", songPlayer.getSong());
                intent.putExtra("song_bundle", bundle);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up,R.anim.fade_out);
            }
        });
    }
    private void handleAction(int action) {
        switch (action){
            case ACTION_PAUSE:{
                if(songPlayer.isPlaying()){
                    play_pauseBtn.setImageDrawable(getDrawable(R.drawable.ic_pause));
                }
                else{
                    play_pauseBtn.setImageDrawable(getDrawable(R.drawable.ic_play));
                }
                break;
            }
            default:{
                updateBottomSongLayout(songPlayer.getSong());
                break;
            }
        }
    }
    @Override
    public void songItemClicked(Song song) {
        songPlayer.playSong(song);
        onStartService(song);
        updateBottomSongLayout(song);
    }
    public void updateBottomSongLayout(Song song){
        if(!active){
            return;
        }
        if(titleTxt == null || singerTxt == null || thumbnail == null || SongPlayer.myMedia == null){
            return;
        }
        if(song == null){
            return;
        }

        if(relativeLayout.getVisibility() == View.GONE){
            relativeLayout.setVisibility(View.VISIBLE);
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_up);
            relativeLayout.startAnimation(animation);
            progressBar.setVisibility(View.VISIBLE);
        }
        progressBar.setMax(SongPlayer.myMedia.getDuration());
        titleTxt.setText(song.getTitle());
        singerTxt.setText(song.getSinger());
        if(!MainActivity.this.isDestroyed()){
            Glide.with(this).load(song.getThumbnail()).apply(RequestOptions.circleCropTransform()).into(thumbnail);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        active = true;
        if(songPlayer!=null){
            updateBottomSongLayout(songPlayer.getSong());
        }
    }
    public void onStartService(Song song){
        Intent intent = new Intent(this,MyService.class);
        startService(intent);
    }
    public void sendActionToService(int action){
        Intent intent = new Intent(this,MyService.class);
        intent.putExtra("action", action);
        startService(intent);
    }

    @Override
    protected void onPause() {
        active = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("TAG", "onPause: " );
        active = false;
    }
}