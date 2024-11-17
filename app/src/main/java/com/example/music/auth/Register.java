package com.example.music.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.music.MainActivity;
import com.example.music.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    private EditText edtUsername, edtEmail, edtPassword, edtRePassword;
    private Button btnRegister, btnLoginInRegister;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // EdgeToEdge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initUi();
        initListener();
    }

    private void initUi() {
        edtUsername = findViewById(R.id.edtRegisterUsername);
        edtEmail = findViewById(R.id.edtRegisterEmail);
        edtPassword = findViewById(R.id.edtRegisterPassword);
        edtRePassword = findViewById(R.id.edtRegisterPassword2);
        btnRegister = findViewById(R.id.btnRegister);
        btnLoginInRegister = findViewById(R.id.btnLoginInRegister);
        db = FirebaseFirestore.getInstance(); // Khởi tạo Firestore
    }

    private void initListener() {
        btnRegister.setOnClickListener(view -> onClickRegister());

        btnLoginInRegister.setOnClickListener(view -> {
            Intent intent = new Intent(Register.this, Login.class);
            startActivity(intent);
        });
    }

    private void onClickRegister() {
        String strUsername = edtUsername.getText().toString().trim();
        String strEmail = edtEmail.getText().toString().trim();
        String strPassword = edtPassword.getText().toString().trim();
        String strRePassword = edtRePassword.getText().toString().trim();

        // Kiểm tra thông tin nhập đầy đủ
        if (strUsername.isEmpty() || strEmail.isEmpty() || strPassword.isEmpty() || strRePassword.isEmpty()) {
            Toast.makeText(Register.this, "Vui lòng nhập đầy đủ thông tin.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!strPassword.equals(strRePassword)) {
            Toast.makeText(Register.this, "Mật khẩu xác nhận không khớp.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Tạo tài khoản Firebase
        auth.createUserWithEmailAndPassword(strEmail, strPassword)
                .addOnCompleteListener(Register.this, task -> {
                    if (task.isSuccessful()) {
                        // Lưu thông tin người dùng vào Firestore
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("username", strUsername);
                            userMap.put("email", strEmail);

                            db.collection("users").document(user.getUid()) // Dùng UID của người dùng làm document ID
                                    .set(userMap)
                                    .addOnSuccessListener(aVoid -> {
                                        // Đăng ký thành công, chuyển sang màn hình chính
                                        Intent intent = new Intent(Register.this, MainActivity.class);
                                        startActivity(intent);
                                        finishAffinity();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(Register.this, "Lưu thông tin người dùng thất bại.", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        // Nếu thất bại, hiển thị thông báo lỗi
                        Toast.makeText(Register.this, "Đăng ký thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
