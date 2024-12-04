package com.example.music.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.music.R;
import com.example.music.model.SongModel;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.Locale;

public class UploadFragment extends Fragment {
    private EditText songTitleEdit, artistNameEdit;
    private Button selectAudioBtn, selectImageBtn, uploadSongBtn;
    private ImageView songImageView;
    private TextView displaySongInfo;
    private ProgressBar uploadProgressBar;

    private Uri audioUri;
    private Uri imageUri;
    private FirebaseStorage storage;
    private FirebaseFirestore firestore;

    private ActivityResultLauncher<Intent> audioPickerLauncher;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload, container, false);
        initializeViews(view);
        initializeFirebase();
        setupActivityLaunchers();
        setupClickListeners();
        return view;
    }

    private void initializeViews(View view) {
        songTitleEdit = view.findViewById(R.id.songTitle);
        artistNameEdit = view.findViewById(R.id.artistName);
        selectAudioBtn = view.findViewById(R.id.selectAudioBtn);
        selectImageBtn = view.findViewById(R.id.selectImageBtn);
        uploadSongBtn = view.findViewById(R.id.uploadSongBtn);
        songImageView = view.findViewById(R.id.songImageView);
        displaySongInfo = view.findViewById(R.id.displaySongInfo);
        uploadProgressBar = view.findViewById(R.id.uploadProgressBar);
    }

    private void initializeFirebase() {
        storage = FirebaseStorage.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    private void setupActivityLaunchers() {
        audioPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        audioUri = result.getData().getData();
                        try {
                            extractAudioMetadata(audioUri);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        Glide.with(requireContext())
                                .load(imageUri)
                                .centerCrop()
                                .into(songImageView);
                    }
                });
    }

    private void setupClickListeners() {
        selectAudioBtn.setOnClickListener(v -> selectAudio());
        selectImageBtn.setOnClickListener(v -> selectImage());
        uploadSongBtn.setOnClickListener(v -> getLastDocumentId());
    }

    private void selectAudio() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        audioPickerLauncher.launch(intent);
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void extractAudioMetadata(Uri audioUri) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(requireContext(), audioUri);

            String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

            if (title != null) songTitleEdit.setText(title);
            if (artist != null) artistNameEdit.setText(artist);

            displaySongInfo.setText("Selected: " + getFileName(audioUri));

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error reading audio metadata", Toast.LENGTH_SHORT).show();
        } finally {
            retriever.release();
        }
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void getLastDocumentId() {
        firestore.collection("song")
                .orderBy("id", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    String lastId = "0";
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        lastId = doc.getId();
                    }
                    int nextId = Integer.parseInt(lastId.split("_")[1]) + 1;
                    uploadSong(String.valueOf(nextId));
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to fetch last ID", Toast.LENGTH_SHORT).show());
    }

    private void uploadSong(String songId) {
        if (audioUri == null) {
            Toast.makeText(requireContext(), "Please select an audio file", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = songTitleEdit.getText().toString().trim();
        String artist = artistNameEdit.getText().toString().trim();

        if (title.isEmpty() || artist.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadProgressBar.setVisibility(View.VISIBLE);
        uploadSongBtn.setEnabled(false);

        String fileNameBase = title.toLowerCase(Locale.ROOT).replace(" ", "");
        uploadAudioFile(songId, title, artist, fileNameBase);
    }

    private void uploadAudioFile(String songId, String title, String artist, String fileNameBase) {
        String audioExtension = getFileExtension(audioUri);
        if (audioExtension == null) {
            Toast.makeText(requireContext(), "Unable to determine audio file extension", Toast.LENGTH_SHORT).show();
            return;
        }
        StorageReference songRef = storage.getReference().child("songs/" + fileNameBase + "." + audioExtension);

        songRef.putFile(audioUri)
                .addOnProgressListener(snapshot -> {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    uploadProgressBar.setProgress((int) progress);
                })
                .addOnSuccessListener(taskSnapshot -> {
                    songRef.getDownloadUrl().addOnSuccessListener(songUrl -> {
                        if (imageUri != null) {
                            uploadImage(songId, title, artist, songUrl.toString(), fileNameBase);
                        } else {
                            saveSongToFirestore(songId, title, artist, songUrl.toString(), null);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    uploadProgressBar.setVisibility(View.GONE);
                    uploadSongBtn.setEnabled(true);
                    Toast.makeText(requireContext(), "Failed to upload audio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadImage(String songId, String title, String artist, String songUrl, String fileNameBase) {
        String imageExtension = getFileExtension(imageUri);
        if (imageExtension == null) {
            Toast.makeText(requireContext(), "Unable to determine image file extension", Toast.LENGTH_SHORT).show();
            return;
        }
        StorageReference imageRef = storage.getReference().child("songs_image/" + fileNameBase + "." + imageExtension);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(imageUrl ->
                            saveSongToFirestore(songId, title, artist, songUrl, imageUrl.toString()));
                })
                .addOnFailureListener(e ->
                        saveSongToFirestore(songId, title, artist, songUrl, null));
    }

    private void saveSongToFirestore(String songId, String title, String artist, String songUrl, String imageUrl) {
        SongModel song = new SongModel(songId, title, artist, imageUrl, songUrl, "1");

        firestore.collection("song")
                .document("song_" + songId)
                .set(song)
                .addOnSuccessListener(aVoid -> {
                    // Remove the 'stability' field if it exists
                    firestore.collection("song").document("song_" + songId)
                            .update("stability", FieldValue.delete())
                            .addOnSuccessListener(unused -> {
                                uploadProgressBar.setVisibility(View.GONE);
                                uploadSongBtn.setEnabled(true);
                                Toast.makeText(requireContext(), "Song uploaded successfully and stability field cleared", Toast.LENGTH_SHORT).show();
                                clearForm();
                            })
                            .addOnFailureListener(e -> {
                                uploadProgressBar.setVisibility(View.GONE);
                                uploadSongBtn.setEnabled(true);
                                Toast.makeText(requireContext(), "Failed to clear stability field: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    uploadProgressBar.setVisibility(View.GONE);
                    uploadSongBtn.setEnabled(true);
                    Toast.makeText(requireContext(), "Failed to save song data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void clearForm() {
        songTitleEdit.setText("");
        artistNameEdit.setText("");
        songImageView.setImageResource(0);
        displaySongInfo.setText("");
        audioUri = null;
        imageUri = null;
        uploadProgressBar.setProgress(0);
    }

    private String getFileExtension(Uri uri) {
        String extension = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(requireContext().getContentResolver().getType(uri));
        } else {
            extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        }
        return extension;
    }
}
