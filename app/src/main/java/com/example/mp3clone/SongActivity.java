package com.example.mp3clone;

import static com.example.mp3clone.MyService.ACTION_NEXT;
import static com.example.mp3clone.MyService.ACTION_PAUSE;
import static com.example.mp3clone.MyService.ACTION_PREVIOUS;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.palette.graphics.Palette;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutionException;

public class SongActivity extends AppCompatActivity {
    private View thumbView;
    private SeekBar seekBar;
    private Song currentSong;
    private ImageView shuffleBtn, previousBtn, playPauseBtn, nextBtn, repeatBtn, thumbnail;
    private Toolbar toolbar;
    private SongPlayer songPlayer = new SongPlayer();
    public Handler handler;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int action = intent.getIntExtra("action",3);
            Log.e("TAG", "onReceive: "+action );
            handleAction(action);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);


        toolbar = findViewById(R.id.toolbar);
        thumbnail = findViewById(R.id.thumbnailImage);
        shuffleBtn = findViewById(R.id.shuffle);
        previousBtn = findViewById(R.id.previous);
        playPauseBtn = findViewById(R.id.play_pauseBtn);
        nextBtn = findViewById(R.id.nextBtn);
        repeatBtn = findViewById(R.id.repeat);
        seekBar = findViewById(R.id.seekbar);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,new IntentFilter("send_action_to_activity"));


        Bundle bundle = getIntent().getExtras().getBundle("song_bundle");
        if(bundle!=null){
            currentSong = bundle.getParcelable("song");
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_downward);
        updateUI(currentSong);

        thumbView = LayoutInflater.from(SongActivity.this).inflate(R.layout.layout_seekbar_thumb, null, false);

        seekBar.setMax(SongPlayer.myMedia.getDuration());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBar.setThumb(getThumb(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                songPlayer.setSecond(seekBar.getProgress());
            }
        });
        updateProgressThread();

        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendActionToService(ACTION_PREVIOUS);
                updateUI(songPlayer.getSong());
            }
        });
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendActionToService(ACTION_NEXT);
                updateUI(songPlayer.getSong());
            }
        });
        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(songPlayer.isPlaying()){
                    playPauseBtn.setImageDrawable(getDrawable(R.drawable.ic_play_circle));
                }
                else{
                    playPauseBtn.setImageDrawable(getDrawable(R.drawable.ic_pause_circle));
                }
                sendActionToService(ACTION_PAUSE);
            }
        });
        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(songPlayer.isShuffle()){
                    songPlayer.shuffle = false;
                    shuffleBtn.setImageDrawable(getDrawable(R.drawable.ic_shuffle));
                }
                else {
                    songPlayer.shuffle = true;
                    shuffleBtn.setImageDrawable(getDrawable(R.drawable.ic_selected_shuffle));
                }
            }
        });
        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SongPlayer.isRepeat()){
                    SongPlayer.repeat  = false;
                    SongPlayer.repeatOne = true;
                    repeatBtn.setImageDrawable(getDrawable(R.drawable.ic_repeat_one_));
                }
                else if(SongPlayer.isRepeatOne()){
                    SongPlayer.repeatOne = false;
                    repeatBtn.setImageDrawable(getDrawable(R.drawable.ic_repeat));
                }
                else{
                    SongPlayer.repeat  = true;
                    repeatBtn.setImageDrawable(getDrawable(R.drawable.ic_selected_repeat));
                }
            }
        });

    }
    public void updateProgressThread(){
        handler = new Handler(){
            @SuppressLint("HandlerLeak")
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what){
                    case 1000:{
                        seekBar.setMax(SongPlayer.myMedia.getDuration());
                        seekBar.setProgress(msg.arg1);
                        if(msg.arg1 >= SongPlayer.myMedia.getDuration() - 1000){
                            sendActionToService(ACTION_NEXT);
                            updateUI(songPlayer.getSong());
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
                    message.arg1 = SongPlayer.myMedia.getCurrentPosition();
                    handler.sendMessage(message);
                }
            }
        });
        thread.start();
    }

    public Drawable getThumb(int progress) {
        String maxDuration = convertIntToTime(SongPlayer.myMedia.getDuration()/1000);
        String current = convertIntToTime(progress/1000);
        ((TextView) thumbView.findViewById(R.id.tvProgress)).setText(current + "/" + maxDuration);
        thumbView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Bitmap bitmap = Bitmap.createBitmap(thumbView.getMeasuredWidth(), thumbView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        thumbView.layout(0, 0, thumbView.getMeasuredWidth(), thumbView.getMeasuredHeight());
        thumbView.draw(canvas);
        return new BitmapDrawable(getResources(), bitmap);
    }
    public String convertIntToTime(int duration){
        int minute = duration/60;
        int second = duration - minute*60;
        return  minute+":"+second;
    }
    public void updateUI(Song song){
        Log.e("TAG", "updateUI: "+song.getTitle() );
        if(SongPlayer.isRepeat()){
            repeatBtn.setImageDrawable(getDrawable(R.drawable.ic_selected_repeat));
        }
        else if(SongPlayer.isRepeatOne()){
            repeatBtn.setImageDrawable(getDrawable(R.drawable.ic_repeat_one_));
        }
        if(SongPlayer.isShuffle()){
            SongPlayer.repeat  = true;
            shuffleBtn.setImageDrawable(getDrawable(R.drawable.ic_selected_shuffle));
        }
        if(!songPlayer.isPlaying()){
            playPauseBtn.setImageDrawable(getDrawable(R.drawable.ic_play_circle));
        }
        RelativeLayout relativeLayout = findViewById(R.id.relativeLayout);
        Animation fadeIn = AnimationUtils.loadAnimation(SongActivity.this, R.anim.fade_in);
        Animation fadeOut = AnimationUtils.loadAnimation(SongActivity.this, R.anim.fade_out);
        Animation rotate = AnimationUtils.loadAnimation(SongActivity.this, R.anim.rotate);
        thumbnail.startAnimation(fadeOut);
        relativeLayout.startAnimation(fadeOut);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(SongActivity.this).load(song.getThumbnail()).apply(RequestOptions.circleCropTransform()).into(thumbnail);
                thumbnail.startAnimation(fadeIn);
                Thread thread = new Thread(){
                    @Override
                    public void run() {
                        Bitmap bitmap = null;
                        try {
                            bitmap = Glide
                                    .with(SongActivity.this)
                                    .asBitmap()
                                    .load(song.getThumbnail())
                                    .submit()
                                    .get();
                            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(@Nullable Palette palette) {
                                    Palette.Swatch swatch = palette.getDominantSwatch();
                                    if(swatch!=null){
                                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{swatch.getRgb(),swatch.getRgb()});
                                        relativeLayout.setBackground(gradientDrawable);
                                    }
                                    else{
                                        RelativeLayout relativeLayout = findViewById(R.id.relativeLayout);
                                        relativeLayout.setBackgroundResource(R.drawable.gradient_bg);
                                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{R.color.primaryColor,R.color.primaryLightColor});
                                        //relativeLayout.setBackground(gradientDrawable);
                                    }
                                }
                            });
                            Animation animation = AnimationUtils.loadAnimation(SongActivity.this, R.anim.fade_in);
                            relativeLayout.startAnimation(animation);
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                };
                thread.start();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                rotate.setRepeatCount(Animation.INFINITE);
                rotate.setRepeatMode(Animation.RESTART);
                thumbnail.startAnimation(rotate);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        rotate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animation.reset();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        toolbar.setTitle(song.getTitle());
        toolbar.setSubtitle(song.getSinger());
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        getSupportActionBar().setTitle(song.getTitle());

    }
    private void handleAction(int action) {
        Log.e("TAG", "handleAction: "+action );
        switch (action){
            case ACTION_PAUSE:{
                if(songPlayer.isPlaying()){
                    playPauseBtn.setImageDrawable(getDrawable(R.drawable.ic_pause_circle));
                }
                else{
                    playPauseBtn.setImageDrawable(getDrawable(R.drawable.ic_play_circle));
                }
                break;
            }
            default:{
                updateUI(songPlayer.getSong());
                break;
            }
        }
    }
    public void sendActionToService(int action){
        Intent intent = new Intent(this,MyService.class);
        intent.putExtra("action", action);
        startService(intent);
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in,R.anim.slide_down);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}