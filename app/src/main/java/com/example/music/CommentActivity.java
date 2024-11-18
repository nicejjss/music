package com.example.music;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.adapter.CommentAdapter;
import com.example.music.model.CommentModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CommentActivity extends AppCompatActivity {

    private String songID;
    private ArrayList<CommentModel> listComments = new ArrayList<>();
    private CommentAdapter adapter;
    private EditText edtContent;
    private ImageButton btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        edtContent = findViewById(R.id.edtCommentBody);
        btnSubmit = findViewById(R.id.submitCommentButton);

        // Lấy songID từ intent
        songID = getIntent().getStringExtra("songID");
        adapter = new CommentAdapter(this);

        RecyclerView recyclerView = findViewById(R.id.rcvComments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Load comments từ Firestore
        loadComments();

        // Xử lý khi người dùng bấm nút gửi comment
        btnSubmit.setOnClickListener(view -> {
            String content = edtContent.getText().toString();
            if (content.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập nội dung bình luận!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lấy username và gửi comment
            getUsernameAndSubmitComment(content);
        });
    }

    private void getUsernameAndSubmitComment(final String content) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để bình luận!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy userId từ FirebaseAuth
        String userId = user.getUid();

        // Lấy thông tin người dùng từ Firestore
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(userId);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null && task.getResult().exists()) {
                    // Lấy username từ document của người dùng
                    String username = task.getResult().getString("username");

                    // Sau khi lấy được username, thêm comment vào Firestore
                    submitComment(content, username);

                } else {
                    Toast.makeText(this, "Không tìm thấy tài khoản người dùng trong Firestore!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Lỗi khi lấy tên người dùng: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitComment(String content, String username) {
        FirebaseFirestore.getInstance().collection("song")
                .whereEqualTo("id", songID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        // Lấy document bài hát
                        QueryDocumentSnapshot docFirst = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);
                        CollectionReference commentRef = docFirst.getReference().collection("comment");

                        // Tạo bình luận mới
                        Map<String, Object> newComment = new HashMap<>();
                        newComment.put("content", content);
                        newComment.put("timestamp", FieldValue.serverTimestamp());
                        newComment.put("username", username);

                        // Thêm bình luận vào Firestore
                        commentRef.add(newComment)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(this, "Thêm bình luận thành công!", Toast.LENGTH_SHORT).show();
                                        edtContent.setText(""); // Xóa nội dung trong EditText sau khi gửi

                                        // Tải lại danh sách bình luận để hiển thị
                                        loadComments();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Thêm bình luận thất bại!", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Không tìm thấy bài hát!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadComments() {
        FirebaseFirestore.getInstance().collection("song")
                .whereEqualTo("id", songID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        // Lấy document bài hát
                        QueryDocumentSnapshot docFirst = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);
                        docFirst.getReference().collection("comment")
                                .orderBy("timestamp", Query.Direction.ASCENDING) // Sắp xếp theo thời gian, cũ nhất trước
                                .get()
                                .addOnCompleteListener(commentTask -> {
                                    if (commentTask.isSuccessful() && commentTask.getResult() != null) {
                                        listComments.clear(); // Xóa các bình luận trước đí

                                        for (QueryDocumentSnapshot commentDoc : commentTask.getResult()) {
                                            CommentModel cmt = commentDoc.toObject(CommentModel.class);
                                            if (cmt != null) {
                                                listComments.add(cmt);
                                            }
                                        }

                                        // Cập nhật RecyclerView
                                        adapter.setData(listComments);
                                    } else {
                                        Toast.makeText(this, "Không thể tải bình luận.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Không tìm thấy bài hát hoặc lỗi khi tải bài hát.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
