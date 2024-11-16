package com.example.music;

import static java.security.AccessController.getContext;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.adapter.CommentAdapter;
import com.example.music.adapter.SongAdapter;
import com.example.music.model.CommentModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class CommentActivity extends AppCompatActivity {
    private String songID;
    private ArrayList<CommentModel> listComments = new ArrayList<>();
    private CommentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        songID=getIntent().getStringExtra("songID");
        adapter=new CommentAdapter(this);
        RecyclerView recyclerView = findViewById(R.id.rcvComments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        try {
            FirebaseFirestore.getInstance().collection("song")
                    .whereEqualTo("id", songID).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                            QueryDocumentSnapshot docFirst = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);
                            docFirst.getReference().collection("comment").get().addOnCompleteListener(commentTask -> {
                                if (commentTask.isSuccessful() && commentTask.getResult() != null) {
                                    listComments.clear(); // Clear previous comments if necessary
                                    for (QueryDocumentSnapshot commentDoc : commentTask.getResult()) {
                                        CommentModel cmt = commentDoc.toObject(CommentModel.class);
                                        if (cmt != null) {
                                            listComments.add(cmt);
                                        }
                                    }

                                    adapter.setData(listComments);
                                } else {
                                    // Handle the case where comment retrieval failed
                                    Toast.makeText(this, "Failed to load comments.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            // Handle the case where the song retrieval failed or no documents found
                            Toast.makeText(this, "Song not found or error loading song.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(this, "Có lỗi khi load comment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }




    }
}