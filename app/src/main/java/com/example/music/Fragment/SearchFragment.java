package com.example.music.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.music.R;
import com.example.music.adapter.SongAdapter;
import com.example.music.model.SongModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Locale;

public class SearchFragment extends Fragment {
    private AutoCompleteTextView searchBar;
    private ImageButton btnVoiceSearch;
    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private ArrayList<SongModel> songList;
    private FirebaseFirestore firestore;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchBar = view.findViewById(R.id.search_bar);
        btnVoiceSearch = view.findViewById(R.id.btn_Voice_Search);
        recyclerView = view.findViewById(R.id.recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        songList = new ArrayList<>();
        songAdapter = new SongAdapter(getContext(), songList);
        recyclerView.setAdapter(songAdapter);

        firestore = FirebaseFirestore.getInstance();

        // Lấy tất cả bài hát ban đầu
        fetchAllSongs();

        // Xử lý tìm kiếm khi nhấn Enter
        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    String query = searchBar.getText().toString().trim();
                    if (!TextUtils.isEmpty(query)) {
                        searchSongs(query);
                    }
                    return true; // Ngăn xử lý mặc định của Enter
                }
                return false;
            }
        });

        //Xử lý tìm kiếm bằng giọng nói
        btnVoiceSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Nói tên bài hát bạn muốn tìm...");

                try {
                    startActivityForResult(intent, 1000);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Thiết bị của bạn không hỗ trợ tìm kiếm bằng giọng nói!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    //Lấy tất cả bài hát từ firestore
    private void fetchAllSongs() {
        firestore.collection("song").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        songList.clear();
                        for (DocumentSnapshot document : task.getResult().getDocuments()) {
                            SongModel song = document.toObject(SongModel.class);
                            if (song != null) {
                                songList.add(song);
                            }
                        }
                        songAdapter.updateData(songList);
                    } else {
                        Toast.makeText(getContext(), "Đã có lỗi xảy ra!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //Hàm tìm kiếm bài hát bằng từ khóa
    private void searchSongs(String query) {
        String queryLower = query.toLowerCase(); // Chuyển từ khóa về chữ thường

        firestore.collection("song").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        ArrayList<SongModel> filteredList = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult().getDocuments()) {
                            SongModel song = document.toObject(SongModel.class);
                            if (song != null && song.getTitle().toLowerCase().contains(queryLower)) {
                                filteredList.add(song);
                            }
                        }
                        songAdapter.updateData(filteredList);

                        if (filteredList.isEmpty()) {
                            Toast.makeText(getContext(), "Không tìm thấy bài hát nào!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Đã có lỗi xảy ra!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //Xử lý kết quả tìm kiếm bằng giọng nói
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000 && resultCode == getActivity().RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String spokenText = result.get(0); // Lấy kết quả nhận dạng đầu tiên
                searchBar.setText(spokenText); // Hiển thị từ khóa trong thanh tìm kiếm
                searchSongs(spokenText); // Tìm kiếm bài hát với từ khóa
            }
        }
    }

}
