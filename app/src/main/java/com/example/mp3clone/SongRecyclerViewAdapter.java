package com.example.mp3clone;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;

public class SongRecyclerViewAdapter extends RecyclerView.Adapter<SongRecyclerViewAdapter.MyViewHolder> {
    private Context mContext;
    private ArrayList<Song> songArrayList = new ArrayList<>();
    private MainActivity mainActivity;

    public SongRecyclerViewAdapter(Context mContext, ArrayList<Song> songArrayList) {
        this.mContext = mContext;
        this.songArrayList = songArrayList;
    }
    public void setData(ArrayList<Song> songArrayList){
        this.songArrayList = songArrayList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SongRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_image,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongRecyclerViewAdapter.MyViewHolder holder, int position) {
        Song song = songArrayList.get(position);
        holder.singerTextView.setText(song.getSinger());
        holder.songNameTextView.setText(song.getTitle());
        holder.rankTextView.setText(String.valueOf(position+1));
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.songItemClicked(song);
//                Intent intent = new Intent(mContext,SongActivity.class);
//                Bundle bundle = new Bundle();
//                bundle.putParcelable("song", song);
//                intent.putExtra("song_bundle", bundle);
//                mContext.startActivity(intent);
//                mainActivity.overridePendingTransition(R.anim.slide_up,R.anim.fade_out);
            }
        });


        Glide.with(mContext).load(song.getThumbnail()).into(holder.thumbNail);
    }

    @Override
    public int getItemCount() {
        return songArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView rankTextView, songNameTextView, singerTextView;
        ImageView thumbNail;
        RelativeLayout layout;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            rankTextView = itemView.findViewById(R.id.rankText);
            songNameTextView = itemView.findViewById(R.id.name);
            singerTextView =  itemView.findViewById(R.id.singer);
            thumbNail = itemView.findViewById(R.id.thumbnail);
            layout = itemView.findViewById(R.id.relativeLayout);
            mainActivity = (MainActivity) mContext;
        }
    }
}
