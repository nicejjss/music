package com.example.music;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.music.model.LikeModel;
import com.example.music.model.SongModel;

import java.io.IOException;
import java.util.ArrayList;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SongActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private TextView txtMusicTitle, txtArtist, txtCurrentDuration, txtDuration;
    private ImageView imgSong, phoneRing, like;
    private SeekBar seekBar;
    private ImageButton playBtn, nextBtn, prevBtn, back;
    private boolean isPlaying = false;
    private ArrayList<SongModel> songList;
    private int currentPosition;

    // Add a Handler and a Runnable to update the SeekBar
    private Handler handler = new Handler();
    private Runnable updateSeekBar;
    private boolean isShakeEnabled = false;

    // Sensor variables for shake detection
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SensorEventListener shakeListener;
    private static final float SHAKE_THRESHOLD = 20.0f;  // Adjust threshold for shake sensitivity

    //get like count
    private DatabaseReference mDatabase;
    private TextView textLike;
    private boolean isLiked = false;

    private ImageView imageViewComment;
    private FrameLayout commentFragmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        txtMusicTitle = findViewById(R.id.txtMusicTitle);
        txtArtist = findViewById(R.id.txtartist);
        imgSong = findViewById(R.id.imgsong);
        seekBar = findViewById(R.id.seekbar);
        txtCurrentDuration = findViewById(R.id.txtCurrentDuration);
        txtDuration = findViewById(R.id.txtDuration);
        playBtn = findViewById(R.id.play);
        nextBtn = findViewById(R.id.next);
        prevBtn = findViewById(R.id.previous);
        phoneRing = findViewById(R.id.phonering);
        back = findViewById(R.id.imgback);
        textLike = findViewById(R.id.textlike);
        like = findViewById(R.id.like);

        initUiComment();
        initListenerComment();

        // Lấy thông tin từ Intent
        Intent intent = getIntent();
        songList = intent.getParcelableArrayListExtra("songList");
        currentPosition = intent.getIntExtra("currentPosition", 0);

        String songName = songList.get(currentPosition).getTitle();
        String artistName = songList.get(currentPosition).getSubtitle();
        String songUrl = songList.get(currentPosition).getUrl();
        String imageUrl = songList.get(currentPosition).getCoverUrl();

        // Hiển thị tên bài hát, tên ca sĩ và hình ảnh
        txtMusicTitle.setText(songName);
        txtArtist.setText(artistName);
        Glide.with(this).load(imageUrl).placeholder(R.drawable.logo).into(imgSong);

        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                    txtCurrentDuration.setText(formatDuration(currentPosition));
                    handler.postDelayed(this, 500);  // Update every 500 ms
                }
            }
        };


        playSong(songUrl);

        nextBtn.setOnClickListener(v -> {
            currentPosition = currentPosition + 1;
            if (currentPosition > songList.size() - 1) {
                currentPosition = 0;
            }
            SongModel song = songList.get(currentPosition);
            changeSong(song);
            playSong(song.getUrl());
        });

        prevBtn.setOnClickListener(v -> {
            currentPosition = currentPosition - 1;
            if (currentPosition < 0) {
                currentPosition = songList.size() - 1;
            }
            SongModel song = songList.get(currentPosition);
            changeSong(song);
            playSong(song.getUrl());
        });

        // Điều khiển Play/Pause
        playBtn.setOnClickListener(v -> {
            if (isPlaying) {
                mediaPlayer.pause();
                playBtn.setImageResource(R.drawable.play);  // Biểu tượng Play
                isPlaying = false;
            } else {
                mediaPlayer.start();
                playBtn.setImageResource(R.drawable.pause);  // Biểu tượng Pause
                isPlaying = true;
            }
        });

        // Cập nhật SeekBar khi người dùng kéo
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        ImageView btnDownload = findViewById(R.id.btnDownload);
        btnDownload.setOnClickListener(v -> downloadSong(songUrl, songName));

        phoneRing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShakeEnabled) {
                    isShakeEnabled = false;
                    Toast.makeText(SongActivity.this, "Tắt chức năng lắc để chuyển bài", Toast.LENGTH_SHORT).show();
                    phoneRing.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.black));
                } else {
                    isShakeEnabled = true;
                    Toast.makeText(SongActivity.this, "Bật chức năng lắc để chuyển bài", Toast.LENGTH_SHORT).show();
                    phoneRing.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.nav_item_selected));
                }
            }
        });

        // Set up the shake detection
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        shakeListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                float shakeForce = (float) Math.sqrt(x * x + y * y + z * z);

                if (shakeForce > SHAKE_THRESHOLD) {
                    if (isShakeEnabled) {
                        currentPosition = currentPosition + 1;
                        if (currentPosition > songList.size() - 1) {
                            currentPosition = 0;
                        }
                        SongModel song = songList.get(currentPosition);
                        changeSong(song);
                        playSong(song.getUrl());
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };
        sensorManager.registerListener(shakeListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        back.setOnClickListener(v -> {
            finish();
        });


        // Khởi tạo DatabaseReference
        mDatabase = FirebaseDatabase.getInstance().getReference();
        getLikesCount(Integer.parseInt(songList.get(currentPosition).getId()));


        like = findViewById(R.id.like);
        getLikeStatus(Integer.parseInt(songList.get(currentPosition).getId()));  // Kiểm tra trạng thái like khi mở bài hát

        like.setOnClickListener(v -> {
            if (isLiked) {
                // Nếu đã like, xóa like khỏi Firebase
                removeLike(Integer.parseInt(songList.get(currentPosition).getId()));
            } else {
                // Nếu chưa like, thêm like vào Firebase
                addLike(Integer.parseInt(songList.get(currentPosition).getId()));
            }
        });
    }

    // Hàm để định dạng thời gian
    private String formatDuration(int duration) {
        int minutes = duration / 1000 / 60;
        int seconds = duration / 1000 % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        if (sensorManager != null) {
            sensorManager.unregisterListener(shakeListener);  // Unregister sensor listener
        }
        handler.removeCallbacks(updateSeekBar);
    }

    private void downloadSong(String url, String title) {
        if (url == null || url.isEmpty()) {
            Toast.makeText(this, "URL không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle("Downloading " + title);
        request.setDescription("Đang tải bài hát xuống...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, title + ".mp3");

        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            downloadManager.enqueue(request);
            Toast.makeText(this, "Đã bắt đầu tải xuống", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "DownloadManager không khả dụng", Toast.LENGTH_SHORT).show();
        }
    }


    private void changeSong(SongModel song) {
        txtMusicTitle.setText(song.getTitle());
        txtArtist.setText(song.getSubtitle());
        Glide.with(this).load(song.getCoverUrl()).placeholder(R.drawable.logo).into(imgSong);
        getLikesCount(Integer.parseInt(song.getId()));
        getLikeStatus(Integer.parseInt(song.getId()));
        isLiked = false;
    }

    private void playSong(String songUrl) {

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }

        mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(songUrl);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                seekBar.setMax(mediaPlayer.getDuration());
                txtDuration.setText(formatDuration(mediaPlayer.getDuration()));
                mediaPlayer.start();
                isPlaying = true;
                playBtn.setImageResource(R.drawable.pause);

                // Start updating the SeekBar
                handler.post(updateSeekBar);
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                playBtn.setImageResource(R.drawable.play);
                seekBar.setProgress(0);
                txtCurrentDuration.setText(formatDuration(0));

                // Stop SeekBar updates
                handler.removeCallbacks(updateSeekBar);
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getLikesCount(int id) {
        // Truy vấn vào "like" và lấy số lượng các lượt thích của bài hát
        mDatabase.child("like")
                .orderByChild("songId")
                .equalTo(id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Lấy số lượt thích
                        long likesCount = dataSnapshot.getChildrenCount();
                        textLike.setText(String.valueOf(likesCount));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(SongActivity.this, "Lỗi khi lấy dữ liệu lượt thích", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getLikeStatus(int songId) {
        // Lấy userId của người dùng hiện tại
        String userId = "1";

        // Truy vấn dữ liệu từ Firebase, tìm mục có songId và userId tương ứng
        mDatabase.child("like")
                .orderByChild("songId")
                .equalTo(songId)  // Tìm theo songId
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean isLikedByUser = false;

                        for (DataSnapshot likeSnapshot : dataSnapshot.getChildren()) {
                            // Kiểm tra nếu userId trong dữ liệu bằng userId của người dùng hiện tại
                            if (likeSnapshot.child("userId").getValue(Integer.class).equals(Integer.parseInt(userId))) {
                                isLikedByUser = true;  // Nếu có mục trùng với userId
                                break;  // Dừng vòng lặp vì đã tìm thấy
                            }
                        }

                        if (isLikedByUser) {
                            // Người dùng đã thích bài hát
                            like.setColorFilter(ContextCompat.getColor(SongActivity.this, R.color.nav_item_selected));
                            isLiked = true;
                        } else {
                            // Người dùng chưa thích bài hát
                            like.setColorFilter(ContextCompat.getColor(SongActivity.this, R.color.black));
                            isLiked = false;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(SongActivity.this, "Lỗi khi lấy dữ liệu lượt thích", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addLike(int songId) {
        String userId = "1"; // Lấy ID người dùng hiện tại (có thể lấy từ Firebase Authentication)
        mDatabase.child("like").push().setValue(new LikeModel(songId, Integer.parseInt(userId))).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                isLiked = true;
                textLike.setText(String.valueOf(Integer.parseInt(textLike.getText().toString()) + 1));  // Cập nhật số lượt like
                like.setColorFilter(ContextCompat.getColor(SongActivity.this, R.color.nav_item_selected));  // Đổi màu icon like
                Toast.makeText(SongActivity.this, "Đã thích bài hát", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeLike(int songId) {
        String userId = "1"; // Lấy ID người dùng hiện tại (có thể lấy từ Firebase Authentication)
        mDatabase.child("like")
                .orderByChild("songId")
                .equalTo(songId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            LikeModel likeModel = snapshot.getValue(LikeModel.class);
                            if (likeModel != null && (likeModel.getUserId() == Integer.parseInt(userId))) {
                                snapshot.getRef().removeValue().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        isLiked = false;
                                        textLike.setText(String.valueOf(Integer.parseInt(textLike.getText().toString()) - 1));  // Cập nhật số lượt like
                                        like.setColorFilter(ContextCompat.getColor(SongActivity.this, R.color.black));  // Đổi màu icon mặc định
                                        Toast.makeText(SongActivity.this, "Đã bỏ thích bài hát", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(SongActivity.this, "Lỗi khi xóa lượt thích", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initUiComment(){
        imageViewComment = findViewById(R.id.ic_comment);
        commentFragmentContainer = findViewById(R.id.comment_fragment_container);
    }

    private void initListenerComment(){
        imageViewComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SongActivity.this,CommentActivity.class);
                i.putExtra("songID",songList.get(currentPosition).getId());
                startActivity(i);
                // Khi người dùng nhấn vào icon bình luận, chuyển sang CommentFragment
//                CommentFragment commentFragment = new CommentFragment();
//                FragmentTransaction transaction =getSupportFragmentManager().beginTransaction();
//
//                transaction.replace(R.id.comment_fragment_container,commentFragment);
//                transaction.addToBackStack(null);
//                transaction.commit();
            }
        });
    }
}