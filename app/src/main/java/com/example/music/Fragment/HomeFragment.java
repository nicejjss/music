package com.example.music.Fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.music.R;
import com.example.music.adapter.SongAdapter;
import com.example.music.model.SongModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private SongAdapter songAdapter;
    private ArrayList<SongModel> songList;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Set up RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        songList = new ArrayList<>();
        songAdapter = new SongAdapter(getContext(), songList);
        recyclerView.setAdapter(songAdapter);

        // Load songs from Firestore
        loadSongsFromFirestore();

        return view;
    }

    // Load songs from Firestore
    @SuppressLint("NotifyDataSetChanged")
    private void loadSongsFromFirestore() {
        CollectionReference songsRef = db.collection("song");
        songsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                songList.clear(); // Clear the old data
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Get the song object from Firestore and add it to the list
                    SongModel song = document.toObject(SongModel.class);
                    songList.add(song);
                    Log.d("HomeFragment", "Song loaded: " + song.getTitle());
                }

                // Notify the adapter that the data has changed
                songAdapter.notifyDataSetChanged();
            } else {
                Log.w("HomeFragment", "Error getting documents.", task.getException());
                Toast.makeText(getContext(), "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}