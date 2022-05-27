package com.example.mp3clone;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class SongPlayer {

    private static Song song;
    public static MediaPlayer myMedia;
    public static ArrayList<Song> songArrayList = new ArrayList<>();
    private Context mContext;
    public static boolean repeat = false;
    public static boolean repeatOne = false;
    public static boolean shuffle = false;

    public SongPlayer() {
    }

    public static ArrayList<Song> getSongArrayList() {
        return songArrayList;
    }

    public static void setSongArrayList(ArrayList<Song> songArrayList) {
        SongPlayer.songArrayList = songArrayList;
    }

    public static boolean isRepeat() {
        return repeat;
    }

    public static void setRepeat(boolean repeat) {
        SongPlayer.repeat = repeat;
    }

    public static boolean isRepeatOne() {
        return repeatOne;
    }

    public static void setRepeatOne(boolean repeatOne) {
        SongPlayer.repeatOne = repeatOne;
    }

    public static boolean isShuffle() {
        return shuffle;
    }

    public static void setShuffle(boolean shuffle) {
        SongPlayer.shuffle = shuffle;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public Song getSong() {
        return song;
    }
    public void playSong(Song song){
        if(myMedia == null){
            myMedia = new MediaPlayer();
        }
        if(this.getSong() !=null && this.getSong().equals(song)){
            if(myMedia.getCurrentPosition() < myMedia.getDuration() -2000){
                return;
            }
        }
        this.song = song;
        Log.e("TAG", "playSong: "+song.getTitle() );
        try {
            myMedia.reset();
            myMedia.setDataSource(song.getUrl());
            myMedia.prepare();
            myMedia.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void pauseSong(){
        Log.e("TAG", "pauseSong: " );
        myMedia.pause();
    }
    public void resumeSong(){
        myMedia.start();
    }
    public boolean isPlaying(){
        return myMedia.isPlaying();
    }
    public void setSecond(int miliSecond){
        myMedia.seekTo(miliSecond);
    }
    public void nextSong(){
        if(repeatOne){
            playSong(song);
            return;
        }
        int index = songArrayList.indexOf(song) + 1;
        if(shuffle){
            Random random = new Random();
            index = random.nextInt(songArrayList.size());
        }
        if(index >= songArrayList.size()){
            index = 0;
            if(repeat){
                return;
            }
        }
        playSong(songArrayList.get(index));
    }
    public void previousSong(){
        if(repeatOne){
            playSong(song);
            return;
        }
        int index = songArrayList.indexOf(song) - 1;
        if(shuffle){
            Random random = new Random();
            index = random.nextInt()/songArrayList.size();
        }
        if(index < 0){
            index = songArrayList.size() - 1;
        }
        playSong(songArrayList.get(index));
    }
}
