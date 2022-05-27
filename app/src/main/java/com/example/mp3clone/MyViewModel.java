package com.example.mp3clone;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MyViewModel extends AndroidViewModel {
    private static MutableLiveData<ArrayList<Song>> listSong = new MutableLiveData<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    public MyViewModel(@NonNull Application application) {
        super(application);
        loadSongFromFirebase();
    }

    public LiveData<ArrayList<Song>> getListSongFromDatabase(){
        if(listSong !=null) {
            listSong = new MutableLiveData<>();
            loadSongFromFirebase();
        }
        return listSong;
    }
    public void loadSongFromFirebase(){
        db.collection("Song")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        ArrayList<Song> tempList = listSong.getValue();
                        if(tempList == null){
                            tempList = new ArrayList<>();
                        }
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Song song = document.toObject(Song.class);
                                if(!tempList.contains(song)){
                                    tempList.add(song);
                                }
                            }
                            listSong.setValue(tempList);
                        } else {
                            Log.e("TAG", "onComplete: " );
                            Log.e("TAG", "Error getting documents.", task.getException());
                        }

                    }
                });
    }
}
